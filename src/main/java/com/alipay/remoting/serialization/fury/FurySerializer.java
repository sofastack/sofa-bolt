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
package com.alipay.remoting.serialization.fury;

import java.util.ArrayList;
import java.util.List;
import com.alipay.remoting.exception.CodecException;
import com.alipay.remoting.serialization.Serializer;
import org.apache.fury.Fury;
import org.apache.fury.ThreadLocalFury;
import org.apache.fury.ThreadSafeFury;

/**
 * @author jianbin@apache.org
 */
public class FurySerializer implements Serializer {

    private static final List<Class<?>> REGISTRY_LIST = new ArrayList<>();

    private final ThreadSafeFury fury = new ThreadLocalFury(classLoader -> {
        Fury fury = Fury.builder().withRefTracking(true)
                .requireClassRegistration(true).withClassLoader(classLoader).build();
        REGISTRY_LIST.forEach(fury::register);
        return fury;
    });

    @Override
    public byte[] serialize(Object obj) throws CodecException {
        try {
            return fury.serialize(obj);
        } catch (Exception e) {
            throw new CodecException("Fury serialization failed", e);
        }
    }

    @Override
    public <T> T deserialize(byte[] data, String classOfT) throws CodecException {
        return (T)fury.deserialize(data);
    }

    public static void registry(Class<?> clazz) {
        REGISTRY_LIST.add(clazz);
    }

}
