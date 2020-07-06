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

import com.alipay.remoting.exception.CodecException;

/**
 * Serializer for serialize and deserialize.
 * 
 * @author jiangping
 * @version $Id: Serializer.java, v 0.1 2015-10-4 PM9:37:57 tao Exp $
 */
public interface Serializer {
    /**
     * Encode object into bytes.
     * 
     * @param obj target object
     * @return serialized result
     */
    byte[] serialize(final Object obj) throws CodecException;

    /**
     * Decode bytes into Object.
     * 
     * @param data serialized data
     * @param classOfT class of original data
     */
    <T> T deserialize(final byte[] data, String classOfT) throws CodecException;
}
