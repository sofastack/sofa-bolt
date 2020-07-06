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

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;

import org.slf4j.Logger;

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
import com.alipay.remoting.rpc.RpcConfigManager;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;

/**
 * Rpc command handler.
 * 
 * @author jiangping
 * @version $Id: RpcServerHandler.java, v 0.1 2015-8-31 PM7:43:06 tao Exp $
 */
@Sharable
public class RpcCommandHandler implements CommandHandler {

    private static final Logger logger = BoltLoggerFactory.getLogger("RpcRemoting");
    /** All processors */
    ProcessorManager            processorManager;

    CommandFactory              commandFactory;

    /**
     * Constructor. Initialize the processor manager and register processors.
     */
    public RpcCommandHandler(CommandFactory commandFactory) {
        this.commandFactory = commandFactory;
        this.processorManager = new ProcessorManager();
        //process request
        this.processorManager.registerProcessor(RpcCommandCode.RPC_REQUEST,
            new RpcRequestProcessor(this.commandFactory));
        //process response
        this.processorManager.registerProcessor(RpcCommandCode.RPC_RESPONSE,
            new RpcResponseProcessor());

        this.processorManager.registerProcessor(CommonCommandCode.HEARTBEAT,
            new RpcHeartBeatProcessor());

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
    @Override
    public void handleCommand(RemotingContext ctx, Object msg) throws Exception {
        this.handle(ctx, msg);
    }

    /*
     * Handle the request(s).
     */
    private void handle(final RemotingContext ctx, final Object msg) {
        try {
            if (msg instanceof List) {
                final Runnable handleTask = new Runnable() {
                    @Override
                    public void run() {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Batch message! size={}", ((List<?>) msg).size());
                        }
                        for (final Object m : (List<?>) msg) {
                            RpcCommandHandler.this.process(ctx, m);
                        }
                    }
                };
                if (RpcConfigManager.dispatch_msg_list_in_default_executor()) {
                    // If msg is list ,then the batch submission to biz threadpool can save io thread.
                    // See com.alipay.remoting.decoder.ProtocolDecoder
                    processorManager.getDefaultExecutor().execute(handleTask);
                } else {
                    handleTask.run();
                }
            } else {
                process(ctx, msg);
            }
        } catch (final Throwable t) {
            processException(ctx, msg, t);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void process(RemotingContext ctx, Object msg) {
        try {
            final RpcCommand cmd = (RpcCommand) msg;
            final RemotingProcessor processor = processorManager.getProcessor(cmd.getCmdCode());
            processor.process(ctx, cmd, processorManager.getDefaultExecutor());
        } catch (final Throwable t) {
            processException(ctx, msg, t);
        }
    }

    private void processException(RemotingContext ctx, Object msg, Throwable t) {
        if (msg instanceof List) {
            for (final Object m : (List<?>) msg) {
                processExceptionForSingleCommand(ctx, m, t);
            }
        } else {
            processExceptionForSingleCommand(ctx, msg, t);
        }
    }

    /*
     * Return error command if necessary.
     */
    private void processExceptionForSingleCommand(RemotingContext ctx, Object msg, Throwable t) {
        final int id = ((RpcCommand) msg).getId();
        final String emsg = "Exception caught when processing "
                            + ((msg instanceof RequestCommand) ? "request, id=" : "response, id=");
        logger.warn(emsg + id, t);
        if (msg instanceof RequestCommand) {
            final RequestCommand cmd = (RequestCommand) msg;
            if (cmd.getType() != RpcCommandType.REQUEST_ONEWAY) {
                if (t instanceof RejectedExecutionException) {
                    final ResponseCommand response = this.commandFactory.createExceptionResponse(
                        id, ResponseStatus.SERVER_THREADPOOL_BUSY);
                    // RejectedExecutionException here assures no response has been sent back
                    // Other exceptions should be processed where exception was caught, because here we don't known whether ack had been sent back.
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
    @Override
    public void registerProcessor(CommandCode cmd,
                                  @SuppressWarnings("rawtypes") RemotingProcessor processor) {
        this.processorManager.registerProcessor(cmd, processor);
    }

    /**
     * @see CommandHandler#registerDefaultExecutor(java.util.concurrent.ExecutorService)
     */
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
