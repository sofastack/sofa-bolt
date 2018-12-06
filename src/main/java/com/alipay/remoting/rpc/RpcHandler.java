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
package com.alipay.remoting.rpc;

import com.alipay.remoting.Connection;
import com.alipay.remoting.InvokeContext;
import com.alipay.remoting.Protocol;
import com.alipay.remoting.ProtocolCode;
import com.alipay.remoting.ProtocolManager;
import com.alipay.remoting.RemotingContext;
import com.alipay.remoting.rpc.protocol.UserProcessor;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Dispatch messages to corresponding protocol.
 *
 * @author jiangping
 * @version $Id: RpcHandler.java, v 0.1 2015-12-14 PM4:01:37 tao Exp $
 */
// TODO: 2018/4/23 by zmyer
@Sharable
public class RpcHandler extends ChannelInboundHandlerAdapter {
    //是否是服务器
    private boolean                                     serverSide;
    //用户自定义处理器
    private ConcurrentHashMap<String, UserProcessor<?>> userProcessors;

    // TODO: 2018/4/23 by zmyer
    public RpcHandler() {
        serverSide = false;
    }

    // TODO: 2018/4/23 by zmyer
    public RpcHandler(ConcurrentHashMap<String, UserProcessor<?>> userProcessors) {
        serverSide = false;
        this.userProcessors = userProcessors;
    }

    // TODO: 2018/4/23 by zmyer
    public RpcHandler(boolean serverSide, ConcurrentHashMap<String, UserProcessor<?>> userProcessors) {
        this.serverSide = serverSide;
        this.userProcessors = userProcessors;
    }

    // TODO: 2018/4/23 by zmyer
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //读取协议编码
        ProtocolCode protocolCode = ctx.channel().attr(Connection.PROTOCOL).get();
        //根据协议编码，查找具体的协议对象
        Protocol protocol = ProtocolManager.getProtocol(protocolCode);
        //开始使用具体的协议处理器来处理当前消息
        protocol.getCommandHandler().handleCommand(
            new RemotingContext(ctx, new InvokeContext(), serverSide, userProcessors), msg);
    }
}
