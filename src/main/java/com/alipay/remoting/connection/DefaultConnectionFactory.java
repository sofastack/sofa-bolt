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
package com.alipay.remoting.connection;

import com.alipay.remoting.Connection;
import com.alipay.remoting.ConnectionEventType;
import com.alipay.remoting.ProtocolCode;
import com.alipay.remoting.Url;
import com.alipay.remoting.codec.Codec;
import com.alipay.remoting.config.Configuration;

import com.alipay.remoting.rpc.protocol.RpcProtocol;
import com.alipay.remoting.rpc.protocol.RpcProtocolV2;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;

/**
 * Default connection factory.
 *
 * @author chengyi (mark.lx@antfin.com) 2018-06-20 15:18
 */
public class DefaultConnectionFactory extends AbstractConnectionFactory {

    public DefaultConnectionFactory(Codec codec, ChannelHandler heartbeatHandler,
                                    ChannelHandler handler, Configuration configuration) {
        super(codec, heartbeatHandler, handler, configuration);
    }

    @Override
    public Connection createConnection(String targetIP, int targetPort, int connectTimeout)
                                                                                           throws Exception {
        Channel channel = doCreateConnection(targetIP, targetPort, connectTimeout);
        Connection conn = new Connection(channel,
            ProtocolCode.fromBytes(RpcProtocol.PROTOCOL_CODE), RpcProtocolV2.PROTOCOL_VERSION_1,
            new Url(targetIP, targetPort));
        if (channel.isActive()) {
            channel.pipeline().fireUserEventTriggered(ConnectionEventType.CONNECT);
        } else {
            channel.pipeline().fireUserEventTriggered(ConnectionEventType.CONNECT_FAILED);
        }
        return conn;
    }

    @Override
    public Connection createConnection(String targetIP, int targetPort, byte version,
                                       int connectTimeout) throws Exception {
        Channel channel = doCreateConnection(targetIP, targetPort, connectTimeout);
        Connection conn = new Connection(channel,
            ProtocolCode.fromBytes(RpcProtocolV2.PROTOCOL_CODE), version, new Url(targetIP,
                targetPort));
        if (channel.isActive()) {
            channel.pipeline().fireUserEventTriggered(ConnectionEventType.CONNECT);
        } else {
            channel.pipeline().fireUserEventTriggered(ConnectionEventType.CONNECT_FAILED);
        }
        return conn;
    }

}
