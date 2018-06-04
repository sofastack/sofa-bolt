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

import org.slf4j.Logger;

import com.alipay.remoting.AbstractRemotingProcessor;
import com.alipay.remoting.Connection;
import com.alipay.remoting.InvokeFuture;
import com.alipay.remoting.RemotingCommand;
import com.alipay.remoting.RemotingContext;
import com.alipay.remoting.log.BoltLoggerFactory;
import com.alipay.remoting.rpc.HeartbeatAckCommand;
import com.alipay.remoting.rpc.HeartbeatCommand;
import com.alipay.remoting.util.RemotingUtil;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

/**
 * Processor for heart beat.
 *
 * @author tsui
 * @version $Id: RpcHeartBeatProcessor.java, v 0.1 2018-03-29 11:02 tsui Exp $
 */
public class RpcHeartBeatProcessor extends AbstractRemotingProcessor {
    private static final Logger logger = BoltLoggerFactory.getLogger("RpcRemoting");

    @Override
    public void doProcess(final RemotingContext ctx, RemotingCommand msg) {
        if (msg instanceof HeartbeatCommand) {// process the heartbeat
            final int id = msg.getId();
            if (logger.isDebugEnabled()) {
                logger.debug("Heartbeat received! Id=" + id + ", from "
                             + RemotingUtil.parseRemoteAddress(ctx.getChannelContext().channel()));
            }
            HeartbeatAckCommand ack = new HeartbeatAckCommand();
            ack.setId(id);
            ctx.writeAndFlush(ack).addListener(new ChannelFutureListener() {

                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Send heartbeat ack done! Id={}, to remoteAddr={}", id,
                                RemotingUtil.parseRemoteAddress(ctx.getChannelContext().channel()));
                        }
                    } else {
                        logger.error("Send heartbeat ack failed! Id={}, to remoteAddr={}", id,
                            RemotingUtil.parseRemoteAddress(ctx.getChannelContext().channel()));
                    }
                }

            });
        } else if (msg instanceof HeartbeatAckCommand) {
            Connection conn = ctx.getChannelContext().channel().attr(Connection.CONNECTION).get();
            InvokeFuture future = conn.removeInvokeFuture(msg.getId());
            if (future != null) {
                future.putResponse(msg);
                future.cancelTimeout();
                try {
                    future.executeInvokeCallback();
                } catch (Exception e) {
                    logger.error(
                        "Exception caught when executing heartbeat invoke callback. From {}",
                        RemotingUtil.parseRemoteAddress(ctx.getChannelContext().channel()), e);
                }
            } else {
                logger
                    .warn(
                        "Cannot find heartbeat InvokeFuture, maybe already timeout. Id={}, From {}",
                        msg.getId(),
                        RemotingUtil.parseRemoteAddress(ctx.getChannelContext().channel()));
            }
        } else {
            throw new RuntimeException("Cannot process command: " + msg.getClass().getName());
        }
    }

}