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
package com.alipay.remoting.serialization;

/**
 * Manage all serializers.
 *
 * Notice: Serializer is different with Codec.
 * Serializer is mainly used to deserialize bytes to object, or serialize object to bytes. We can use hessian, json, protocol buff etc.
 * Codec mainly used to encode bytes or decode bytes according to the protocol format. We can use {@link com.alipay.remoting.codec.ProtocolCodeBasedEncoder} or {@link io.netty.handler.codec.LengthFieldBasedFrameDecoder} etc.
 * 
 * @author jiangping
 * @version $Id: SerializerManager.java, v 0.1 2015-9-28 PM3:55:59 tao Exp $
 */
public class SerializerManager {

    private static Serializer[] serializers = new Serializer[5];
    public static final byte    Hessian2    = 1;
    //public static final byte    Json        = 2;

    static {
        addSerializer(Hessian2, new HessianSerializer());
    }

    public static Serializer getSerializer(int idx) {
        return serializers[idx];
    }

    public static void addSerializer(int idx, Serializer serializer) {
        if (serializers.length <= idx) {
            Serializer[] newSerializers = new Serializer[idx + 5];
            System.arraycopy(serializers, 0, newSerializers, 0, serializers.length);
            serializers = newSerializers;
        }
        serializers[idx] = serializer;
    }
}
