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
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

import com.alipay.remoting.CustomSerializer;
import com.alipay.remoting.DefaultCustomSerializer;
import com.alipay.remoting.InvokeContext;
import com.alipay.remoting.exception.DeserializationException;
import com.alipay.remoting.exception.SerializationException;
import com.alipay.remoting.rpc.RequestCommand;
import com.alipay.remoting.rpc.common.RequestBody;
import com.alipay.remoting.rpc.protocol.RpcRequestCommand;
import com.alipay.remoting.util.StringUtils;

/**
 * a custom serialize demo
 * 
 * @author xiaomin.cxm
 * @version $Id: NormalRequestBodyCustomSerializer.java, v 0.1 Apr 11, 2016 10:18:59 PM xiaomin.cxm Exp $
 */
public class NormalRequestBodyCustomSerializer_InvokeContext extends DefaultCustomSerializer {

    private AtomicBoolean      serialFlag        = new AtomicBoolean();
    private AtomicBoolean      deserialFlag      = new AtomicBoolean();

    public static final String UNIVERSAL_REQ     = "UNIVERSAL REQUEST";

    public static final String SERIALTYPE_KEY    = "serial.type";
    public static final String SERIALTYPE1_value = "SERIAL1";
    public static final String SERIALTYPE2_value = "SERIAL2";

    /**
     * @see CustomSerializer#serializeContent(RequestCommand, InvokeContext)
     */
    @Override
    public <T extends RequestCommand> boolean serializeContent(T req, InvokeContext invokeContext)
                                                                                                  throws SerializationException {
        serialFlag.set(true);
        RpcRequestCommand rpcReq = (RpcRequestCommand) req;
        if (StringUtils.equals(SERIALTYPE1_value, (String) invokeContext.get(SERIALTYPE_KEY))) {
            RequestBody bd = (RequestBody) rpcReq.getRequestObject();
            int id = bd.getId();
            byte[] msg;
            try {
                msg = bd.getMsg().getBytes("UTF-8");
                ByteBuffer bb = ByteBuffer.allocate(4 + msg.length);
                bb.putInt(id);
                bb.put(msg);
                rpcReq.setContent(bb.array());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else {
            try {
                rpcReq.setContent(UNIVERSAL_REQ.getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        return true;
    }

    /**
     * @see CustomSerializer#deserializeContent(RequestCommand)
     */
    @Override
    public <T extends RequestCommand> boolean deserializeContent(T req)
                                                                       throws DeserializationException {
        deserialFlag.set(true);
        RpcRequestCommand rpcReq = (RpcRequestCommand) req;
        byte[] content = rpcReq.getContent();
        ByteBuffer bb = ByteBuffer.wrap(content);
        int a = bb.getInt();
        byte[] dst = new byte[content.length - 4];
        bb.get(dst, 0, dst.length);
        try {
            String b = new String(dst, "UTF-8");
            RequestBody bd = new RequestBody(a, b);
            rpcReq.setRequestObject(bd);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean isSerialized() {
        return this.serialFlag.get();
    }

    public boolean isDeserialized() {
        return this.deserialFlag.get();
    }
}
