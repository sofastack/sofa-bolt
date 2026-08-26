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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.alipay.remoting.exception.CodecException;
import com.alipay.remoting.rpc.RpcCommandType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class RpcCommandDecoderTest {

    @Test(expected = CodecException.class)
    public void rejectsNegativeClassLengthInV1Request() throws Exception {
        ByteBuf frame = v1RequestFrame((short) -1, (short) 0, 0);
        try {
            new RpcCommandDecoder().decode(context(), frame, new ArrayList<Object>());
        } finally {
            frame.release();
        }
    }

    @Test(expected = CodecException.class)
    public void rejectsNegativeHeaderLengthInV2Request() throws Exception {
        ByteBuf frame = v2RequestFrame((short) 0, (short) -1, 0);
        try {
            new RpcCommandDecoderV2().decode(context(), frame, new ArrayList<Object>());
        } finally {
            frame.release();
        }
    }

    @Test(expected = CodecException.class)
    public void rejectsNegativeContentLengthInV1Request() throws Exception {
        ByteBuf frame = v1RequestFrame((short) 0, (short) 0, -1);
        try {
            new RpcCommandDecoder().decode(context(), frame, new ArrayList<Object>());
        } finally {
            frame.release();
        }
    }

    @Test(expected = CodecException.class)
    public void rejectsNegativeContentLengthInV2Request() throws Exception {
        ByteBuf frame = v2RequestFrame((short) 0, (short) 0, -1);
        try {
            new RpcCommandDecoderV2().decode(context(), frame, new ArrayList<Object>());
        } finally {
            frame.release();
        }
    }

    @Test(expected = CodecException.class)
    public void rejectsOverflowedTotalLengthInV1Request() throws Exception {
        ByteBuf frame = v1RequestFrame((short) 1, (short) 0, Integer.MAX_VALUE);
        try {
            new RpcCommandDecoder().decode(context(), frame, new ArrayList<Object>());
        } finally {
            frame.release();
        }
    }

    @Test(expected = CodecException.class)
    public void rejectsOverflowedTotalLengthInV2Request() throws Exception {
        ByteBuf frame = v2RequestFrame((short) 1, (short) 0, Integer.MAX_VALUE);
        try {
            new RpcCommandDecoderV2().decode(context(), frame, new ArrayList<Object>());
        } finally {
            frame.release();
        }
    }

    @Test(expected = CodecException.class)
    public void rejectsNegativeClassLengthInV1Response() throws Exception {
        ByteBuf frame = v1ResponseFrame((short) -1, (short) 0, 0);
        try {
            new RpcCommandDecoder().decode(context(), frame, new ArrayList<Object>());
        } finally {
            frame.release();
        }
    }

    @Test(expected = CodecException.class)
    public void rejectsNegativeHeaderLengthInV2Response() throws Exception {
        ByteBuf frame = v2ResponseFrame((short) 0, (short) -1, 0);
        try {
            new RpcCommandDecoderV2().decode(context(), frame, new ArrayList<Object>());
        } finally {
            frame.release();
        }
    }

    @Test(expected = CodecException.class)
    public void rejectsOverflowedTotalLengthInV2Response() throws Exception {
        ByteBuf frame = v2ResponseFrame((short) 0, (short) 1, Integer.MAX_VALUE);
        try {
            new RpcCommandDecoderV2().decode(context(), frame, new ArrayList<Object>());
        } finally {
            frame.release();
        }
    }

    @Test(expected = CodecException.class)
    public void rejectsOverflowedTotalLengthInV1Response() throws Exception {
        ByteBuf frame = v1ResponseFrame((short) 0, (short) 1, Integer.MAX_VALUE);
        try {
            new RpcCommandDecoder().decode(context(), frame, new ArrayList<Object>());
        } finally {
            frame.release();
        }
    }

    @Test
    public void decodesValidV2RequestWithContent() throws Exception {
        byte[] body = "hello".getBytes(StandardCharsets.UTF_8);
        ByteBuf frame = v2RequestFrame((short) 0, (short) 0, body.length);
        frame.writeBytes(body);
        List<Object> out = new ArrayList<Object>();
        try {
            new RpcCommandDecoderV2().decode(context(), frame, out);
        } finally {
            frame.release();
        }
        Assert.assertEquals(1, out.size());
    }

    @Test
    public void decodesValidV1RequestWithContent() throws Exception {
        byte[] body = "world".getBytes(StandardCharsets.UTF_8);
        ByteBuf frame = v1RequestFrame((short) 0, (short) 0, body.length);
        frame.writeBytes(body);
        List<Object> out = new ArrayList<Object>();
        try {
            new RpcCommandDecoder().decode(context(), frame, out);
        } finally {
            frame.release();
        }
        Assert.assertEquals(1, out.size());
    }

    @Test
    public void decodesValidV1ResponseWithContent() throws Exception {
        byte[] body = "resp-v1".getBytes(StandardCharsets.UTF_8);
        ByteBuf frame = v1ResponseFrame((short) 0, (short) 0, body.length);
        frame.writeBytes(body);
        List<Object> out = new ArrayList<Object>();
        try {
            new RpcCommandDecoder().decode(context(), frame, out);
        } finally {
            frame.release();
        }
        Assert.assertEquals(1, out.size());
    }

    @Test
    public void decodesValidV2ResponseWithContent() throws Exception {
        byte[] body = "resp-v2".getBytes(StandardCharsets.UTF_8);
        ByteBuf frame = v2ResponseFrame((short) 0, (short) 0, body.length);
        frame.writeBytes(body);
        List<Object> out = new ArrayList<Object>();
        try {
            new RpcCommandDecoderV2().decode(context(), frame, out);
        } finally {
            frame.release();
        }
        Assert.assertEquals(1, out.size());
    }

    private static ByteBuf v1RequestFrame(short classLen, short headerLen, int contentLen) {
        return Unpooled.buffer(RpcProtocol.getRequestHeaderLength())
            .writeByte(RpcProtocol.PROTOCOL_CODE).writeByte(RpcCommandType.REQUEST)
            .writeShort(RpcCommandCode.RPC_REQUEST.value()).writeByte(1).writeInt(1).writeByte(1)
            .writeInt(1000).writeShort(classLen).writeShort(headerLen).writeInt(contentLen);
    }

    private static ByteBuf v2RequestFrame(short classLen, short headerLen, int contentLen) {
        return Unpooled.buffer(RpcProtocolV2.getRequestHeaderLength())
            .writeByte(RpcProtocolV2.PROTOCOL_CODE).writeByte(RpcProtocolV2.PROTOCOL_VERSION_1)
            .writeByte(RpcCommandType.REQUEST).writeShort(RpcCommandCode.RPC_REQUEST.value())
            .writeByte(1).writeInt(1).writeByte(1).writeByte(0).writeInt(1000).writeShort(classLen)
            .writeShort(headerLen).writeInt(contentLen);
    }

    private static ByteBuf v1ResponseFrame(short classLen, short headerLen, int contentLen) {
        return Unpooled.buffer(RpcProtocol.getResponseHeaderLength())
            .writeByte(RpcProtocol.PROTOCOL_CODE).writeByte(RpcCommandType.RESPONSE)
            .writeShort(RpcCommandCode.RPC_RESPONSE.value()).writeByte(1).writeInt(1).writeByte(1)
            .writeShort(0).writeShort(classLen).writeShort(headerLen).writeInt(contentLen);
    }

    private static ByteBuf v2ResponseFrame(short classLen, short headerLen, int contentLen) {
        return Unpooled.buffer(RpcProtocolV2.getResponseHeaderLength())
            .writeByte(RpcProtocolV2.PROTOCOL_CODE).writeByte(RpcProtocolV2.PROTOCOL_VERSION_1)
            .writeByte(RpcCommandType.RESPONSE).writeShort(RpcCommandCode.RPC_RESPONSE.value())
            .writeByte(1).writeInt(1).writeByte(1).writeByte(0).writeShort(0).writeShort(classLen)
            .writeShort(headerLen).writeInt(contentLen);
    }

    private static ChannelHandlerContext context() {
        ChannelHandlerContext context = Mockito.mock(ChannelHandlerContext.class);
        Mockito.when(context.channel()).thenReturn(Mockito.mock(Channel.class));
        return context;
    }
}
