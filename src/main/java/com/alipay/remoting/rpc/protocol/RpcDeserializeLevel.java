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

/**
 * Rpc deserialize level.
 *
 * @author tsui
 * @version $Id: RpcDeserializeLevel.java, v 0.1 2017-04-24 15:12 tsui Exp $
 */
public class RpcDeserializeLevel {
    /** deserialize clazz, header, contents all three parts of rpc command */
    public final static int DESERIALIZE_ALL    = 0x02;
    /** deserialize both header and clazz parts of rpc command */
    public final static int DESERIALIZE_HEADER = 0x01;
    /** deserialize only the clazz part of rpc command */
    public final static int DESERIALIZE_CLAZZ  = 0x00;

    /**
     * Convert to String.
     */
    public static String valueOf(int value) {
        switch (value) {
            case 0x00:
                return "DESERIALIZE_CLAZZ";
            case 0x01:
                return "DESERIALIZE_HEADER";
            case 0x02:
                return "DESERIALIZE_ALL";
        }
        throw new IllegalArgumentException("Unknown deserialize level value ," + value);
    }
}