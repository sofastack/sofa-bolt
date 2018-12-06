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
import org.slf4j.Logger;

/**
 * Processor for heart beat.
 *
 * @author tsui
 * @version $Id: RpcHeartBeatProcessor.java, v 0.1 2018-03-29 11:02 tsui Exp $
 */
// TODO: 2018/4/24 by zmyer
public class RpcHeartBeatProcessor extends AbstractRemotingProcessor {
    //日志
    private static final Logger logger = BoltLoggerFactory.getLogger("RpcRemoting");

    // TODO: 2018/4/24 by zmyer
    @Override
    public void doProcess(final RemotingContext ctx, RemotingCommand msg) {
        if (msg instanceof HeartbeatCommand) {// process the heartbeat
            //读取消息id
            final int id = msg.getId();
            if (logger.isDebugEnabled()) {
                logger.debug("Heartbeat received! Id=" + id + ", from "
                             + RemotingUtil.parseRemoteAddress(ctx.getChannelContext().channel()));
            }
            //创建心跳ack
            HeartbeatAckCommand ack = new HeartbeatAckCommand();
            //设置消息id
            ack.setId(id);
            //返回应答消息
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
            //获取连接对象
            Connection conn = ctx.getChannelContext().channel().attr(Connection.CONNECTION).get();
            //从连接对象中删除本次发送的心跳调用
            InvokeFuture future = conn.removeInvokeFuture(msg.getId());
            if (future != null) {
                //设置应答结果
                future.putResponse(msg);
                //取消超时监控
                future.cancelTimeout();
                try {
                    //开始回调
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