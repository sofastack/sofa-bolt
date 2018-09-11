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
package com.alipay.remoting;

import java.io.Serializable;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * Encode command.
 * 
 * @author jiangping
 * @version $Id: CommandEncoder.java, v 0.1 Mar 10, 2016 11:33:02 AM jiangping Exp $
 */
public interface CommandEncoder {

    /**
     * Encode object into bytes.
     * 
     * @param ctx
     * @param msg
     * @param out
     * @throws Exception
     */
    void encode(ChannelHandlerContext ctx, Serializable msg, ByteBuf out) throws Exception;

}
