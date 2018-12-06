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
package com.alipay.remoting.codec;

import com.alipay.remoting.Connection;
import com.alipay.remoting.Protocol;
import com.alipay.remoting.ProtocolCode;
import com.alipay.remoting.ProtocolManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.Attribute;

import java.io.Serializable;

/**
 * Protocol code based encoder, the main encoder for a certain protocol, which is lead by one or multi bytes (magic code).
 *
 * Notice: this is stateless can be noted as {@link io.netty.channel.ChannelHandler.Sharable}
 * @author jiangping
 * @version $Id: ProtocolCodeBasedEncoder.java, v 0.1 2015-12-11 PM 7:30:30 tao Exp $
 */
// TODO: 2018/4/23 by zmyer
@ChannelHandler.Sharable
public class ProtocolCodeBasedEncoder extends MessageToByteEncoder<Serializable> {

    /** default protocol code */
    protected ProtocolCode defaultProtocolCode;

    public ProtocolCodeBasedEncoder(ProtocolCode defaultProtocolCode) {
        super();
        this.defaultProtocolCode = defaultProtocolCode;
    }

    // TODO: 2018/4/23 by zmyer
    @Override
    protected void encode(ChannelHandlerContext ctx, Serializable msg, ByteBuf out)
                                                                                   throws Exception {
        //读取协议编码
        Attribute<ProtocolCode> att = ctx.channel().attr(Connection.PROTOCOL);
        ProtocolCode protocolCode;
        if (att == null || att.get() == null) {
            //如果通道对象中未带有编码信息，则采用默认
            protocolCode = this.defaultProtocolCode;
        } else {
            //获取协议编码
            protocolCode = att.get();
        }
        //获取协议对象
        Protocol protocol = ProtocolManager.getProtocol(protocolCode);
        //开始采用具体的协议编码器进行编码
        protocol.getEncoder().encode(ctx, msg, out);
    }

}
