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
import com.alipay.remoting.exception.CodecException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.util.List;

/**
 * Protocol code based decoder, the main decoder for a certain protocol, which is lead by one or multi bytes (magic code).
 *
 * Notice: this is not stateless, can not be noted as {@link io.netty.channel.ChannelHandler.Sharable}
 * @author xiaomin.cxm
 * @version $Id: ProtocolCodeBasedDecoder.java, v0.1 Mar 20, 2017 2:42:46 PM xiaomin.cxm Exp $
 */
// TODO: 2018/4/23 by zmyer
public class ProtocolCodeBasedDecoder extends AbstractBatchDecoder {
    /** by default, suggest design a single byte for protocol version. */
    //默认协议版本长度
    public static final int DEFAULT_PROTOCOL_VERSION_LENGTH         = 1;
    /** protocol version should be a positive number, we use -1 to represent illegal */
    //默认非法协议版本长度
    public static final int DEFAULT_ILLEGAL_PROTOCOL_VERSION_LENGTH = -1;

    /** the length of protocol code */
    //协议编码长度
    protected int           protocolCodeLength;

    // TODO: 2018/4/24 by zmyer
    public ProtocolCodeBasedDecoder(int protocolCodeLength) {
        super();
        this.protocolCodeLength = protocolCodeLength;
    }

    /**
     * decode the protocol code
     *
     * @param in
     * @return an instance of ProtocolCode
     */
    // TODO: 2018/4/24 by zmyer
    protected ProtocolCode decodeProtocolCode(ByteBuf in) {
        if (in.readableBytes() >= protocolCodeLength) {
            //创建协议编码字节数组
            byte[] protocolCodeBytes = new byte[protocolCodeLength];
            //读取编码信息
            in.readBytes(protocolCodeBytes);
            //反序列化
            return ProtocolCode.fromBytes(protocolCodeBytes);
        }
        return null;
    }

    /**
     * decode the protocol version
     *
     * @param in
     * @return a byte to represent protocol version
     */
    // TODO: 2018/4/24 by zmyer
    protected byte decodeProtocolVersion(ByteBuf in) {
        if (in.readableBytes() >= DEFAULT_PROTOCOL_VERSION_LENGTH) {
            return in.readByte();
        }
        return DEFAULT_ILLEGAL_PROTOCOL_VERSION_LENGTH;
    }

    // TODO: 2018/4/23 by zmyer
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        in.markReaderIndex();
        //读取协议编码信息
        ProtocolCode protocolCode = decodeProtocolCode(in);
        if (null != protocolCode) {
            //读取协议版本信息
            byte protocolVersion = decodeProtocolVersion(in);
            if (ctx.channel().attr(Connection.PROTOCOL).get() == null) {
                //设置连接对象协议编码
                ctx.channel().attr(Connection.PROTOCOL).set(protocolCode);
                if (DEFAULT_ILLEGAL_PROTOCOL_VERSION_LENGTH != protocolVersion) {
                    //设置连接对象协议版本信息
                    ctx.channel().attr(Connection.VERSION).set(protocolVersion);
                }
            }
            //根据协议编码，查找具体的协议对象
            Protocol protocol = ProtocolManager.getProtocol(protocolCode);
            if (null != protocol) {
                in.resetReaderIndex();
                //开始根据协议解码消息
                protocol.getDecoder().decode(ctx, in, out);
            } else {
                throw new CodecException("Unknown protocol code: [" + protocolCode
                                         + "] while decode in ProtocolDecoder.");
            }
        }
    }
}
