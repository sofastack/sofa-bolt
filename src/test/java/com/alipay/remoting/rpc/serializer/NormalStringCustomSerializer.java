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
package com.alipay.remoting.rpc.serializer;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.atomic.AtomicBoolean;

import com.alipay.remoting.CustomSerializer;
import com.alipay.remoting.DefaultCustomSerializer;
import com.alipay.remoting.InvokeContext;
import com.alipay.remoting.exception.DeserializationException;
import com.alipay.remoting.exception.SerializationException;
import com.alipay.remoting.rpc.ResponseCommand;
import com.alipay.remoting.rpc.protocol.RpcResponseCommand;

/**
 * a custom serialize demo
 *
 * @author xiaomin.cxm
 * @version $Id: NormalStringCustomSerializer.java, v 0.1 Apr 11, 2016 10:18:59 PM xiaomin.cxm Exp $
 */
public class NormalStringCustomSerializer extends DefaultCustomSerializer {

    private AtomicBoolean serialFlag         = new AtomicBoolean();
    private AtomicBoolean deserialFlag       = new AtomicBoolean();

    private byte          contentSerializer  = -1;
    private byte          contentDeserialier = -1;

    /**
     * @see CustomSerializer#serializeContent(ResponseCommand)
     */
    @Override
    public <T extends ResponseCommand> boolean serializeContent(T response)
                                                                           throws SerializationException {
        serialFlag.set(true);
        RpcResponseCommand rpcResp = (RpcResponseCommand) response;
        String str = (String) rpcResp.getResponseObject();
        try {
            rpcResp.setContent(str.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        contentSerializer = response.getSerializer();
        return true;
    }

    /**
     * @see CustomSerializer#deserializeContent(ResponseCommand, InvokeContext)
     */
    @Override
    public <T extends ResponseCommand> boolean deserializeContent(T response,
                                                                  InvokeContext invokeContext)
                                                                                              throws DeserializationException {
        deserialFlag.set(true);
        RpcResponseCommand rpcResp = (RpcResponseCommand) response;
        try {
            rpcResp.setResponseObject(new String(rpcResp.getContent(), "UTF-8") + "RANDOM");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        contentDeserialier = response.getSerializer();
        return true;
    }

    public boolean isSerialized() {
        return this.serialFlag.get();
    }

    public boolean isDeserialized() {
        return this.deserialFlag.get();
    }

    public byte getContentSerializer() {
        return contentSerializer;
    }

    public byte getContentDeserialier() {
        return contentDeserialier;
    }

    public void reset() {
        this.contentDeserialier = -1;
        this.contentDeserialier = -1;
        this.deserialFlag.set(false);
        this.serialFlag.set(false);
    }
}
