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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.alipay.remoting.SystemProperties;
import com.alipay.remoting.connection.DefaultConnectionFactory;
import io.netty.channel.ChannelHandler;

import com.alipay.remoting.connection.ConnectionFactory;
import com.alipay.remoting.NamedThreadFactory;
import com.alipay.remoting.ProtocolCode;
import com.alipay.remoting.codec.ProtocolCodeBasedEncoder;
import com.alipay.remoting.rpc.protocol.RpcProtocolDecoder;
import com.alipay.remoting.rpc.protocol.RpcProtocolManager;
import com.alipay.remoting.rpc.protocol.RpcProtocolV2;
import com.alipay.remoting.rpc.protocol.UserProcessor;

import io.netty.handler.timeout.IdleStateHandler;

/**
 * Default RPC connection factory impl.
 *
 * @author chengyi (mark.lx@antfin.com) 2018-06-20 15:32
 */
public class RpcConnectionFactory extends DefaultConnectionFactory implements ConnectionFactory {

    public RpcConnectionFactory(ConcurrentHashMap<String, UserProcessor<?>> userProcessors) {
        this(Runtime.getRuntime().availableProcessors() + 1, new NamedThreadFactory(
            "Rpc-netty-client-worker", true), new ProtocolCodeBasedEncoder(
            ProtocolCode.fromBytes(RpcProtocolV2.PROTOCOL_CODE)), new RpcProtocolDecoder(
            RpcProtocolManager.DEFAULT_PROTOCOL_CODE_LENGTH), new IdleStateHandler(
            SystemProperties.tcp_idle(), SystemProperties.tcp_idle(), 0, TimeUnit.MILLISECONDS),
            new HeartbeatHandler(), new RpcHandler(userProcessors));
    }

    public RpcConnectionFactory(int threads, NamedThreadFactory threadFactory,
                                ChannelHandler encoder, ChannelHandler decoder,
                                IdleStateHandler idleStateHandler, ChannelHandler heartbeatHandler,
                                ChannelHandler handler) {
        super(threads, threadFactory, encoder, decoder, idleStateHandler, heartbeatHandler, handler);
    }
}