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

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alipay.remoting.CommandEncoder;
import com.alipay.remoting.Connection;
import com.alipay.remoting.config.switches.ProtocolSwitch;
import com.alipay.remoting.rpc.RequestCommand;
import com.alipay.remoting.rpc.ResponseCommand;
import com.alipay.remoting.rpc.RpcCommand;
import com.alipay.remoting.util.CrcUtil;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Attribute;

/**
 * Encode remoting command into ByteBuf v2.
 * 
 * @author jiangping
 * @version $Id: RpcCommandEncoderV2.java, v 0.1 2017-05-27 PM8:11:27 tao Exp $
 */
public class RpcCommandEncoderV2 implements CommandEncoder {
    /** logger */
    private static final Logger logger = LoggerFactory.getLogger("RpcRemoting");

    /**
     * @see CommandEncoder#encode(ChannelHandlerContext, Serializable, ByteBuf)
     */
    @Override
    public void encode(ChannelHandlerContext ctx, Serializable msg, ByteBuf out) throws Exception {
        try {
            if (msg instanceof RpcCommand) {
                /*
                 * proto: magic code for protocol
                 * ver: version for protocol
                 * type: request/response/request oneway
                 * cmdcode: code for remoting command
                 * ver2:version for remoting command
                 * requestId: id of request
                 * codec: code for codec
                 * switch: function switch
                 * (req)timeout: request timeout.
                 * (resp)respStatus: response status
                 * classLen: length of request or response class name
                 * headerLen: length of header
                 * cotentLen: length of content
                 * className
                 * header
                 * content
                 * crc (optional)
                 */
                int index = out.writerIndex();
                RpcCommand cmd = (RpcCommand) msg;
                out.writeByte(RpcProtocolV2.PROTOCOL_CODE);
                Attribute<Byte> version = ctx.channel().attr(Connection.VERSION);
                byte ver = RpcProtocolV2.PROTOCOL_VERSION_1;
                if (version != null && version.get() != null) {
                    ver = version.get();
                }
                out.writeByte(ver);
                out.writeByte(cmd.getType());
                out.writeShort(((RpcCommand) msg).getCmdCode().value());
                out.writeByte(cmd.getVersion());
                out.writeInt(cmd.getId());
                out.writeByte(cmd.getSerializer());
                out.writeByte(cmd.getProtocolSwitch().toByte());
                if (cmd instanceof RequestCommand) {
                    //timeout
                    out.writeInt(((RequestCommand) cmd).getTimeout());
                }
                if (cmd instanceof ResponseCommand) {
                    //response status
                    ResponseCommand response = (ResponseCommand) cmd;
                    out.writeShort(response.getResponseStatus().getValue());
                }
                out.writeShort(cmd.getClazzLength());
                out.writeShort(cmd.getHeaderLength());
                out.writeInt(cmd.getContentLength());
                if (cmd.getClazzLength() > 0) {
                    out.writeBytes(cmd.getClazz());
                }
                if (cmd.getHeaderLength() > 0) {
                    out.writeBytes(cmd.getHeader());
                }
                if (cmd.getContentLength() > 0) {
                    out.writeBytes(cmd.getContent());
                }
                if (ver == RpcProtocolV2.PROTOCOL_VERSION_2
                    && cmd.getProtocolSwitch().isOn(ProtocolSwitch.CRC_SWITCH_INDEX)) {
                    // compute the crc32 and write to out
                    byte[] frame = new byte[out.readableBytes()];
                    out.getBytes(index, frame);
                    out.writeInt(CrcUtil.crc32(frame));
                }
            } else {
                String warnMsg = "msg type [" + msg.getClass() + "] is not subclass of RpcCommand";
                logger.warn(warnMsg);
            }
        } catch (Exception e) {
            logger.error("Exception caught!", e);
            throw e;
        }
    }
}
