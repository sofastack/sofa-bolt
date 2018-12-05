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

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;

import com.alipay.remoting.CommandFactory;
import com.alipay.remoting.Connection;
import com.alipay.remoting.HeartbeatTrigger;
import com.alipay.remoting.InvokeCallbackListener;
import com.alipay.remoting.InvokeFuture;
import com.alipay.remoting.ResponseStatus;
import com.alipay.remoting.TimerHolder;
import com.alipay.remoting.config.ConfigManager;
import com.alipay.remoting.log.BoltLoggerFactory;
import com.alipay.remoting.rpc.DefaultInvokeFuture;
import com.alipay.remoting.rpc.HeartbeatCommand;
import com.alipay.remoting.rpc.ResponseCommand;
import com.alipay.remoting.util.RemotingUtil;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;

/**
 * Handler for heart beat.
 * 
 * @author jiangping
 * @version $Id: RpcHeartbeatTrigger.java, v 0.1 2015-9-29 PM3:17:45 tao Exp $
 */
public class RpcHeartbeatTrigger implements HeartbeatTrigger {
    private static final Logger logger                 = BoltLoggerFactory.getLogger("RpcRemoting");

    /** max trigger times */
    public static final Integer maxCount               = ConfigManager.tcp_idle_maxtimes();

    private static final long   heartbeatTimeoutMillis = 1000;

    private CommandFactory      commandFactory;

    public RpcHeartbeatTrigger(CommandFactory commandFactory) {
        this.commandFactory = commandFactory;
    }

    /**
     * @see com.alipay.remoting.HeartbeatTrigger#heartbeatTriggered(io.netty.channel.ChannelHandlerContext)
     */
    @Override
    public void heartbeatTriggered(final ChannelHandlerContext ctx) throws Exception {
        Integer heartbeatTimes = ctx.channel().attr(Connection.HEARTBEAT_COUNT).get();
        final Connection conn = ctx.channel().attr(Connection.CONNECTION).get();
        if (heartbeatTimes >= maxCount) {
            try {
                conn.close();
                logger.error(
                    "Heartbeat failed for {} times, close the connection from client side: {} ",
                    heartbeatTimes, RemotingUtil.parseRemoteAddress(ctx.channel()));
            } catch (Exception e) {
                logger.warn("Exception caught when closing connection in SharableHandler.", e);
            }
        } else {
            boolean heartbeatSwitch = ctx.channel().attr(Connection.HEARTBEAT_SWITCH).get();
            if (!heartbeatSwitch) {
                return;
            }
            final HeartbeatCommand heartbeat = new HeartbeatCommand();

            final InvokeFuture future = new DefaultInvokeFuture(heartbeat.getId(),
                new InvokeCallbackListener() {
                    @Override
                    public void onResponse(InvokeFuture future) {
                        ResponseCommand response;
                        try {
                            response = (ResponseCommand) future.waitResponse(0);
                        } catch (InterruptedException e) {
                            logger.error("Heartbeat ack process error! Id={}, from remoteAddr={}",
                                heartbeat.getId(), RemotingUtil.parseRemoteAddress(ctx.channel()),
                                e);
                            return;
                        }
                        if (response != null
                            && response.getResponseStatus() == ResponseStatus.SUCCESS) {
                            if (logger.isDebugEnabled()) {
                                logger.debug("Heartbeat ack received! Id={}, from remoteAddr={}",
                                    response.getId(),
                                    RemotingUtil.parseRemoteAddress(ctx.channel()));
                            }
                            ctx.channel().attr(Connection.HEARTBEAT_COUNT).set(0);
                        } else {
                            if (response == null) {
                                logger.error("Heartbeat timeout! The address is {}",
                                    RemotingUtil.parseRemoteAddress(ctx.channel()));
                            } else {
                                logger.error(
                                    "Heartbeat exception caught! Error code={}, The address is {}",
                                    response.getResponseStatus(),
                                    RemotingUtil.parseRemoteAddress(ctx.channel()));
                            }
                            Integer times = ctx.channel().attr(Connection.HEARTBEAT_COUNT).get();
                            ctx.channel().attr(Connection.HEARTBEAT_COUNT).set(times + 1);
                        }
                    }

                    @Override
                    public String getRemoteAddress() {
                        return ctx.channel().remoteAddress().toString();
                    }
                }, null, heartbeat.getProtocolCode().getFirstByte(), this.commandFactory);
            final int heartbeatId = heartbeat.getId();
            conn.addInvokeFuture(future);
            if (logger.isDebugEnabled()) {
                logger.debug("Send heartbeat, successive count={}, Id={}, to remoteAddr={}",
                    heartbeatTimes, heartbeatId, RemotingUtil.parseRemoteAddress(ctx.channel()));
            }
            ctx.writeAndFlush(heartbeat).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Send heartbeat done! Id={}, to remoteAddr={}",
                                heartbeatId, RemotingUtil.parseRemoteAddress(ctx.channel()));
                        }
                    } else {
                        logger.error("Send heartbeat failed! Id={}, to remoteAddr={}", heartbeatId,
                            RemotingUtil.parseRemoteAddress(ctx.channel()));
                    }
                }
            });
            TimerHolder.getTimer().newTimeout(new TimerTask() {
                @Override
                public void run(Timeout timeout) throws Exception {
                    InvokeFuture future = conn.removeInvokeFuture(heartbeatId);
                    if (future != null) {
                        future.putResponse(commandFactory.createTimeoutResponse(conn
                            .getRemoteAddress()));
                        future.tryAsyncExecuteInvokeCallbackAbnormally();
                    }
                }
            }, heartbeatTimeoutMillis, TimeUnit.MILLISECONDS);
        }

    }
}
