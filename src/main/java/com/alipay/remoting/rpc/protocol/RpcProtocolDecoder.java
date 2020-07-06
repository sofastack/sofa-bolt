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

import com.alipay.remoting.codec.ProtocolCodeBasedDecoder;

import io.netty.buffer.ByteBuf;

/**
 * Rpc protocol decoder.
 *
 * @author tsui
 * @version $Id: RpcProtocolDecoder.java, v 0.1 2018-03-27 19:28 tsui Exp $
 */
public class RpcProtocolDecoder extends ProtocolCodeBasedDecoder {
    public static final int MIN_PROTOCOL_CODE_WITH_VERSION = 2;

    public RpcProtocolDecoder(int protocolCodeLength) {
        super(protocolCodeLength);
    }

    @Override
    protected byte decodeProtocolVersion(ByteBuf in) {
        in.resetReaderIndex();
        if (in.readableBytes() >= protocolCodeLength + DEFAULT_PROTOCOL_VERSION_LENGTH) {
            byte rpcProtocolCodeByte = in.readByte();
            if (rpcProtocolCodeByte >= MIN_PROTOCOL_CODE_WITH_VERSION) {
                return in.readByte();
            }
        }
        return DEFAULT_ILLEGAL_PROTOCOL_VERSION_LENGTH;
    }
}