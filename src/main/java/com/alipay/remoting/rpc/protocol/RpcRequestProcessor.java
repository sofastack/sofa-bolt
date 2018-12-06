/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alipay.remoting.rpc.protocol;

import com.alipay.remoting.AbstractRemotingProcessor;
import com.alipay.remoting.CommandFactory;
import com.alipay.remoting.InvokeContext;
import com.alipay.remoting.RemotingCommand;
import com.alipay.remoting.RemotingContext;
import com.alipay.remoting.ResponseStatus;
import com.alipay.remoting.exception.DeserializationException;
import com.alipay.remoting.exception.SerializationException;
import com.alipay.remoting.log.BoltLoggerFactory;
import com.alipay.remoting.rpc.RpcCommandType;
import com.alipay.remoting.util.RemotingUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;

/**
 * Process Rpc request.
 *
 * @author jiangping
 * @version $Id: RpcRequestProcessor.java, v 0.1 2015-10-1 PM10:56:10 tao Exp $
 */
// TODO: 2018/4/23 by zmyer
public class RpcRequestProcessor extends AbstractRemotingProcessor<RpcRequestCommand> {
    /** logger */
    private static final Logger logger = BoltLoggerFactory.getLogger("RpcRemoting");

    /**
     * Default constructor.
     */
    public RpcRequestProcessor() {
    }

    /**
     * Constructor.
     */
    // TODO: 2018/4/24 by zmyer
    public RpcRequestProcessor(CommandFactory commandFactory) {
        super(commandFactory);
    }

    /**
     * @param executor
     */
    // TODO: 2018/4/24 by zmyer
    public RpcRequestProcessor(ExecutorService executor) {
        super(executor);
    }

    /**
     * @see com.alipay.remoting.AbstractRemotingProcessor#process(com.alipay.remoting.RemotingContext, com.alipay.remoting.RemotingCommand, java.util.concurrent.ExecutorService)
     */
    // TODO: 2018/4/24 by zmyer
    @Override
    public void process(RemotingContext ctx, RpcRequestCommand cmd, ExecutorService defaultExecutor)
                                                                                                    throws Exception {
        //反序列化请求指令
        if (!deserializeRequestCommand(ctx, cmd, RpcDeserializeLevel.DESERIALIZE_CLAZZ)) {
            return;
        }
        //根据提供的类型信息，查找具体的处理器
        UserProcessor userProcessor = ctx.getUserProcessor(cmd.getRequestClass());
        if (userProcessor == null) {
            String errMsg = "No user processor found for request: " + cmd.getRequestClass();
            logger.error(errMsg);
            //返回应答
            sendResponseIfNecessary(ctx, cmd.getType(), this.getCommandFactory()
                .createExceptionResponse(cmd.getId(), errMsg));
            return;// must end process
        }

        // set timeout check state from user's processor
        //设置超时是否丢弃
        ctx.setTimeoutDiscard(userProcessor.timeoutDiscard());

        // to check whether to process in io thread
        if (userProcessor.processInIOThread()) {
            //全部反序列化
            if (!deserializeRequestCommand(ctx, cmd, RpcDeserializeLevel.DESERIALIZE_ALL)) {
                return;
            }
            // process in io thread
            //开始处理请求
            new ProcessTask(ctx, cmd).run();
            return;// end
        }

        Executor executor = null;
        // to check whether get executor using executor selector
        if (null == userProcessor.getExecutorSelector()) {
            //设置执行器
            executor = userProcessor.getExecutor();
        } else {
            // in case haven't deserialized in io thread
            // it need to deserialize clazz and header before using executor dispath strategy
            if (!deserializeRequestCommand(ctx, cmd, RpcDeserializeLevel.DESERIALIZE_HEADER)) {
                return;
            }
            //try get executor with strategy
            //根据策略选择具体的执行器
            executor = userProcessor.getExecutorSelector().select(cmd.getRequestClass(),
                cmd.getRequestHeader());
        }

        // Till now, if executor still null, then try default
        if (executor == null) {
            //采用默认
            executor = (this.getExecutor() == null ? defaultExecutor : this.getExecutor());
        }

        // use the final executor dispatch process task
        //开始执行任务
        executor.execute(new ProcessTask(ctx, cmd));
    }

    /**
     * @see com.alipay.remoting.AbstractRemotingProcessor#doProcess(com.alipay.remoting.RemotingContext, com.alipay.remoting.RemotingCommand)
     */
    // TODO: 2018/4/24 by zmyer
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void doProcess(final RemotingContext ctx, RpcRequestCommand cmd) throws Exception {
        long currenTimestamp = System.currentTimeMillis();
        //设置调用上下文对象
        preProcessRemotingContext(ctx, cmd, currenTimestamp);
        if (ctx.isTimeoutDiscard() && ctx.isRequestTimeout()) {
            timeoutLog(cmd, currenTimestamp, ctx);// do some log
            return;// then, discard this request
        }
        debugLog(ctx, cmd, currenTimestamp);
        // decode request all
        if (!deserializeRequestCommand(ctx, cmd, RpcDeserializeLevel.DESERIALIZE_ALL)) {
            return;
        }
        //开始处理用户请求
        dispatchToUserProcessor(ctx, cmd);
    }

    /**
     * Send response using remoting context if necessary.<br>
     * If request type is oneway, no need to send any response nor exception.
     *
     * @param ctx
     * @param response
     */
    // TODO: 2018/4/24 by zmyer
    public void sendResponseIfNecessary(final RemotingContext ctx, byte type,
                                        final RemotingCommand response) {
        final int id = response.getId();
        if (type != RpcCommandType.REQUEST_ONEWAY) {
            RemotingCommand serializedResponse = response;
            try {
                response.serialize();
            } catch (SerializationException e) {
                String errMsg = "SerializationException occurred when sendResponseIfNecessary in RpcRequestProcessor, id="
                                + id;
                logger.error(errMsg, e);
                serializedResponse = this.getCommandFactory().createExceptionResponse(id,
                    ResponseStatus.SERVER_SERIAL_EXCEPTION);
            } catch (Throwable t) {
                String errMsg = "Serialize RpcResponseCommand failed when sendResponseIfNecessary in RpcRequestProcessor, id="
                                + id;
                logger.error(errMsg, t);
                serializedResponse = this.getCommandFactory()
                    .createExceptionResponse(id, t, errMsg);
            }
            //发回应答消息
            ctx.writeAndFlush(serializedResponse).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Rpc response sent! requestId="
                                     + id
                                     + ". The address is "
                                     + RemotingUtil.parseRemoteAddress(ctx.getChannelContext()
                                         .channel()));
                    }
                    if (!future.isSuccess()) {
                        logger.error(
                            "Rpc response send failed! id="
                                    + id
                                    + ". The address is "
                                    + RemotingUtil.parseRemoteAddress(ctx.getChannelContext()
                                        .channel()), future.cause());
                    }
                }
            });
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Oneway rpc request received, do not send response, id=" + id
                             + ", the address is "
                             + RemotingUtil.parseRemoteAddress(ctx.getChannelContext().channel()));
            }
        }
    }

    /**
     * dispatch request command to user processor
     *
     * @param ctx
     * @param cmd
     */
    // TODO: 2018/4/24 by zmyer
    private void dispatchToUserProcessor(RemotingContext ctx, RpcRequestCommand cmd) {
        final int id = cmd.getId();
        final byte type = cmd.getType();
        // processor here must not be null, for it have been checked before
        //获取用户自定义处理器
        UserProcessor processor = ctx.getUserProcessor(cmd.getRequestClass());
        if (processor instanceof AsyncUserProcessor) {
            try {
                //开始异步处理
                processor.handleRequest(processor.preHandleRequest(ctx, cmd.getRequestObject()),
                    new RpcAsyncContext(ctx, cmd, this), cmd.getRequestObject());
            } catch (RejectedExecutionException e) {
                logger
                    .warn("RejectedExecutionException occurred when do ASYNC process in RpcRequestProcessor");
                sendResponseIfNecessary(ctx, type, this.getCommandFactory()
                    .createExceptionResponse(id, ResponseStatus.SERVER_THREADPOOL_BUSY));
            } catch (Throwable t) {
                String errMsg = "AYSNC process rpc request failed in RpcRequestProcessor, id=" + id;
                logger.error(errMsg, t);
                sendResponseIfNecessary(ctx, type, this.getCommandFactory()
                    .createExceptionResponse(id, t, errMsg));
            }
        } else {
            try {
                //开始处理请求
                Object responseObject = processor
                    .handleRequest(processor.preHandleRequest(ctx, cmd.getRequestObject()),
                        cmd.getRequestObject());
                //发送应答
                sendResponseIfNecessary(ctx, type,
                    this.getCommandFactory().createResponse(responseObject, cmd));
            } catch (RejectedExecutionException e) {
                logger
                    .warn("RejectedExecutionException occurred when do SYNC process in RpcRequestProcessor");
                sendResponseIfNecessary(ctx, type, this.getCommandFactory()
                    .createExceptionResponse(id, ResponseStatus.SERVER_THREADPOOL_BUSY));
            } catch (Throwable t) {
                String errMsg = "SYNC process rpc request failed in RpcRequestProcessor, id=" + id;
                logger.error(errMsg, t);
                sendResponseIfNecessary(ctx, type, this.getCommandFactory()
                    .createExceptionResponse(id, t, errMsg));
            }
        }
    }

    /**
     * deserialize request command
     *
     * @param ctx
     * @param cmd
     * @param level
     * @return true if deserialize success; false if exception catched
     */
    // TODO: 2018/4/24 by zmyer
    private boolean deserializeRequestCommand(RemotingContext ctx, RpcRequestCommand cmd, int level) {
        boolean result = false;
        try {
            cmd.deserialize(level);
            result = true;
        } catch (DeserializationException e) {
            logger
                .error(
                    "DeserializationException occurred when process in RpcRequestProcessor, id={}, deserializeLevel={}",
                    cmd.getId(), RpcDeserializeLevel.valueOf(level), e);
            sendResponseIfNecessary(ctx, cmd.getType(), this.getCommandFactory()
                .createExceptionResponse(cmd.getId(), ResponseStatus.SERVER_DESERIAL_EXCEPTION));
            result = false;
        } catch (Throwable t) {
            String errMsg = "Deserialize RpcRequestCommand failed in RpcRequestProcessor, id="
                            + cmd.getId() + ", deserializeLevel=" + level;
            logger.error(errMsg, t);
            sendResponseIfNecessary(ctx, cmd.getType(), this.getCommandFactory()
                .createExceptionResponse(cmd.getId(), t, errMsg));
            result = false;
        }
        return result;
    }

    /**
     * pre process remoting context, initial some useful infos and pass to biz
     *
     * @param ctx
     * @param cmd
     * @param currenTimestamp
     */
    // TODO: 2018/4/24 by zmyer
    private void preProcessRemotingContext(RemotingContext ctx, RpcRequestCommand cmd,
                                           long currenTimestamp) {
        ctx.setArriveTimestamp(cmd.getArriveTime());
        ctx.setTimeout(cmd.getTimeout());
        ctx.setRpcCommandType(cmd.getType());
        ctx.getInvokeContext().putIfAbsent(InvokeContext.BOLT_PROCESS_WAIT_TIME,
            currenTimestamp - cmd.getArriveTime());
    }

    /**
     * print some log when request timeout and discarded in io thread.
     *
     * @param cmd
     * @param currenTimestamp
     * @return true if request already timeout.
     */
    // TODO: 2018/4/24 by zmyer
    private void timeoutLog(final RpcRequestCommand cmd, long currenTimestamp, RemotingContext ctx) {
        if (logger.isDebugEnabled()) {
            logger
                .debug(
                    "request id [{}] currenTimestamp [{}] - arriveTime [{}] = server cost [{}] >= timeout value [{}].",
                    cmd.getId(), currenTimestamp, cmd.getArriveTime(),
                    (currenTimestamp - cmd.getArriveTime()), cmd.getTimeout());
        }

        String remoteAddr = "UNKNOWN";
        if (null != ctx) {
            ChannelHandlerContext channelCtx = ctx.getChannelContext();
            Channel channel = channelCtx.channel();
            if (null != channel) {
                remoteAddr = RemotingUtil.parseRemoteAddress(channel);
            }
        }
        logger
            .warn(
                "Rpc request id[{}], from remoteAddr[{}] stop process, total wait time in queue is [{}], client timeout setting is [{}].",
                cmd.getId(), remoteAddr, (currenTimestamp - cmd.getArriveTime()), cmd.getTimeout());
    }

    /**
     * print some debug log when receive request
     *
     * @param ctx
     * @param cmd
     * @param currenTimestamp
     */
    // TODO: 2018/4/24 by zmyer
    private void debugLog(RemotingContext ctx, RpcRequestCommand cmd, long currenTimestamp) {
        if (logger.isDebugEnabled()) {
            logger.debug("Rpc request received! requestId={}, from {}", cmd.getId(),
                RemotingUtil.parseRemoteAddress(ctx.getChannelContext().channel()));
            logger.debug(
                "request id {} currenTimestamp {} - arriveTime {} = server cost {} < timeout {}.",
                cmd.getId(), currenTimestamp, cmd.getArriveTime(),
                (currenTimestamp - cmd.getArriveTime()), cmd.getTimeout());
        }
    }

    /**
     * Inner process task
     *
     * @author xiaomin.cxm
     * @version $Id: RpcRequestProcessor.java, v 0.1 May 19, 2016 4:01:28 PM xiaomin.cxm Exp $
     */
    // TODO: 2018/4/24 by zmyer
    class ProcessTask implements Runnable {

        RemotingContext   ctx;
        RpcRequestCommand msg;

        public ProcessTask(RemotingContext ctx, RpcRequestCommand msg) {
            this.ctx = ctx;
            this.msg = msg;
        }

        /**
         * @see java.lang.Runnable#run()
         */
        // TODO: 2018/4/24 by zmyer
        @Override
        public void run() {
            try {
                RpcRequestProcessor.this.doProcess(ctx, msg);
            } catch (Throwable e) {
                //protect the thread running this task
                String remotingAddress = RemotingUtil.parseRemoteAddress(ctx.getChannelContext()
                    .channel());
                logger
                    .error(
                        "Exception caught when process rpc request command in RpcRequestProcessor, Id="
                                + msg.getId() + "! Invoke source address is [" + remotingAddress
                                + "].", e);
            }
        }

    }
}
