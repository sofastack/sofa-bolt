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

import java.util.List;

import com.alipay.remoting.Connection;
import com.alipay.remoting.Protocol;
import com.alipay.remoting.ProtocolCode;
import com.alipay.remoting.ProtocolManager;
import com.alipay.remoting.exception.CodecException;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * Protocol code based decoder, the main decoder for a certain protocol, which is lead by one or multi bytes (magic code).
 *
 * Notice: this is not stateless, can not be noted as {@link io.netty.channel.ChannelHandler.Sharable}
 * @author xiaomin.cxm
 * @version $Id: ProtocolCodeBasedDecoder.java, v0.1 Mar 20, 2017 2:42:46 PM xiaomin.cxm Exp $
 */
public class ProtocolCodeBasedDecoder extends AbstractBatchDecoder {
    /** by default, suggest design a single byte for protocol version. */
    public static final int DEFAULT_PROTOCOL_VERSION_LENGTH         = 1;
    /** protocol version should be a positive number, we use -1 to represent illegal */
    public static final int DEFAULT_ILLEGAL_PROTOCOL_VERSION_LENGTH = -1;

    /** the length of protocol code */
    protected int           protocolCodeLength;

    public ProtocolCodeBasedDecoder(int protocolCodeLength) {
        super();
        this.protocolCodeLength = protocolCodeLength;
    }

    /**
     * decode the protocol code
     *
     * @param in input byte buf
     * @return an instance of ProtocolCode
     */
    protected ProtocolCode decodeProtocolCode(ByteBuf in) {
        if (in.readableBytes() >= protocolCodeLength) {
            byte[] protocolCodeBytes = new byte[protocolCodeLength];
            in.readBytes(protocolCodeBytes);
            return ProtocolCode.fromBytes(protocolCodeBytes);
        }
        return null;
    }

    /**
     * decode the protocol version
     *
     * @param in input byte buf
     * @return a byte to represent protocol version
     */
    protected byte decodeProtocolVersion(ByteBuf in) {
        if (in.readableBytes() >= DEFAULT_PROTOCOL_VERSION_LENGTH) {
            return in.readByte();
        }
        return DEFAULT_ILLEGAL_PROTOCOL_VERSION_LENGTH;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        in.markReaderIndex();
        ProtocolCode protocolCode = decodeProtocolCode(in);
        if (null != protocolCode) {
            byte protocolVersion = decodeProtocolVersion(in);
            if (ctx.channel().attr(Connection.PROTOCOL).get() == null) {
                ctx.channel().attr(Connection.PROTOCOL).set(protocolCode);
                if (DEFAULT_ILLEGAL_PROTOCOL_VERSION_LENGTH != protocolVersion) {
                    ctx.channel().attr(Connection.VERSION).set(protocolVersion);
                }
            }
            Protocol protocol = ProtocolManager.getProtocol(protocolCode);
            if (null != protocol) {
                in.resetReaderIndex();
                protocol.getDecoder().decode(ctx, in, out);
            } else {
                throw new CodecException("Unknown protocol code: [" + protocolCode
                                         + "] while decode in ProtocolDecoder.");
            }
        }
    }
}
