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
import com.alipay.remoting.CommandCode;
import com.alipay.remoting.CommandFactory;
import com.alipay.remoting.CommandHandler;
import com.alipay.remoting.CommonCommandCode;
import com.alipay.remoting.ProcessorManager;
import com.alipay.remoting.RemotingCommand;
import com.alipay.remoting.RemotingContext;
import com.alipay.remoting.RemotingProcessor;
import com.alipay.remoting.ResponseStatus;
import com.alipay.remoting.log.BoltLoggerFactory;
import com.alipay.remoting.rpc.RequestCommand;
import com.alipay.remoting.rpc.ResponseCommand;
import com.alipay.remoting.rpc.RpcCommand;
import com.alipay.remoting.rpc.RpcCommandType;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import org.slf4j.Logger;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;

/**
 * Rpc command handler.
 *
 * @author jiangping
 * @version $Id: RpcServerHandler.java, v 0.1 2015-8-31 PM7:43:06 tao Exp $
 */
// TODO: 2018/4/23 by zmyer
@Sharable
public class RpcCommandHandler implements CommandHandler {

    private static final Logger logger = BoltLoggerFactory.getLogger("RpcRemoting");
    /** All processors */
    //处理器管理器
    ProcessorManager            processorManager;

    //指令工厂
    CommandFactory              commandFactory;

    /**
     * Constructor. Initialize the processor manager and register processors.
     */
    // TODO: 2018/4/23 by zmyer
    public RpcCommandHandler(CommandFactory commandFactory) {
        this.commandFactory = commandFactory;
        //创建处理器管理器
        this.processorManager = new ProcessorManager();
        //process request
        //注册处理请求处理器
        this.processorManager.registerProcessor(RpcCommandCode.RPC_REQUEST,
            new RpcRequestProcessor(this.commandFactory));
        //process response
        //注册处理应答处理器
        this.processorManager.registerProcessor(RpcCommandCode.RPC_RESPONSE,
            new RpcResponseProcessor());

        //处理心跳处理器
        this.processorManager.registerProcessor(CommonCommandCode.HEARTBEAT,
            new RpcHeartBeatProcessor());

        //注册默认处理器
        this.processorManager
            .registerDefaultProcessor(new AbstractRemotingProcessor<RemotingCommand>() {
                @Override
                public void doProcess(RemotingContext ctx, RemotingCommand msg) throws Exception {
                    logger.error("No processor available for command code {}, msgId {}",
                        msg.getCmdCode(), msg.getId());
                }
            });
    }

    /**
     * @see CommandHandler#handleCommand(RemotingContext, Object)
     */
    // TODO: 2018/4/23 by zmyer
    @Override
    public void handleCommand(RemotingContext ctx, Object msg) throws Exception {
        this.handle(ctx, msg);
    }

    /*
     * Handle the request(s).
     */
    // TODO: 2018/4/23 by zmyer
    private void handle(final RemotingContext ctx, final Object msg) {
        try {
            // If msg is list ,then the batch submission to biz threadpool can save io thread.
            // See com.alipay.remoting.decoder.ProtocolDecoder
            if (msg instanceof List) {
                processorManager.getDefaultExecutor().execute(new Runnable() {
                    @Override
                    public void run() {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Batch message! size={}", ((List<?>) msg).size());
                        }
                        //批量处理消息
                        for (Object m : (List<?>) msg) {
                            RpcCommandHandler.this.process(ctx, m);
                        }
                    }
                });
            } else {
                //处理消息
                process(ctx, msg);
            }
        } catch (Throwable t) {
            processException(ctx, msg, t);
        }
    }

    // TODO: 2018/4/24 by zmyer
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void process(RemotingContext ctx, Object msg) {
        try {
            //获取具体的消息指令
            RpcCommand cmd = (RpcCommand) msg;
            //根据消息指令，获取具体的处理器
            RemotingProcessor processor = processorManager.getProcessor(cmd.getCmdCode());
            //开始在具体的处理器中处理消息
            processor.process(ctx, cmd, processorManager.getDefaultExecutor());
        } catch (Throwable t) {
            //处理异常
            processException(ctx, msg, t);
        }
    }

    // TODO: 2018/4/24 by zmyer
    private void processException(RemotingContext ctx, Object msg, Throwable t) {
        if (msg instanceof List) {
            for (Object m : (List<?>) msg) {
                //处理异常
                processExceptionForSingleCommand(ctx, m, t);
            }
        } else {
            //处理异常
            processExceptionForSingleCommand(ctx, msg, t);
        }
    }

    /*
     * Return error command if necessary.
     */
    // TODO: 2018/4/24 by zmyer
    private void processExceptionForSingleCommand(RemotingContext ctx, Object msg, Throwable t) {
        //获取消息id
        final int id = ((RpcCommand) msg).getId();
        String emsg = "Exception caught when processing "
                      + ((msg instanceof RequestCommand) ? "request, id=" : "response, id=");
        logger.warn(emsg + id, t);
        if (msg instanceof RequestCommand) {
            RequestCommand cmd = (RequestCommand) msg;
            if (cmd.getType() != RpcCommandType.REQUEST_ONEWAY) {
                if (t instanceof RejectedExecutionException) {
                    //创建异常应答消息
                    final ResponseCommand response = (ResponseCommand) this.commandFactory
                        .createExceptionResponse(id, ResponseStatus.SERVER_THREADPOOL_BUSY);
                    // RejectedExecutionException here assures no response has been sent back
                    // Other exceptions should be processed where exception was caught, because here we don't known whether ack had been sent back.  
                    //直接写回异常消息
                    ctx.getChannelContext().writeAndFlush(response)
                        .addListener(new ChannelFutureListener() {
                            @Override
                            public void operationComplete(ChannelFuture future) throws Exception {
                                if (future.isSuccess()) {
                                    if (logger.isInfoEnabled()) {
                                        logger
                                            .info(
                                                "Write back exception response done, requestId={}, status={}",
                                                id, response.getResponseStatus());
                                    }
                                } else {
                                    logger.error(
                                        "Write back exception response failed, requestId={}", id,
                                        future.cause());
                                }
                            }

                        });
                }
            }
        }
    }

    /**
     * @see CommandHandler#registerProcessor(com.alipay.remoting.CommandCode, RemotingProcessor)
     */
    // TODO: 2018/4/23 by zmyer
    @Override
    public void registerProcessor(CommandCode cmd,
                                  @SuppressWarnings("rawtypes") RemotingProcessor processor) {
        this.processorManager.registerProcessor(cmd, processor);
    }

    /**
     * @see CommandHandler#registerDefaultExecutor(java.util.concurrent.ExecutorService)
     */
    // TODO: 2018/4/23 by zmyer
    @Override
    public void registerDefaultExecutor(ExecutorService executor) {
        this.processorManager.registerDefaultExecutor(executor);
    }

    /**
     * @see CommandHandler#getDefaultExecutor()
     */
    @Override
    public ExecutorService getDefaultExecutor() {
        return this.processorManager.getDefaultExecutor();
    }
}
