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
import org.apache.fury.collection.Tuple2;
import org.apache.fury.config.Language;
import org.apache.fury.memory.MemoryBuffer;
import org.apache.fury.memory.MemoryUtils;
import org.apache.fury.util.LoaderBinding;

/**
 * @author jianbin@apache.org
 */
public class FurySerializer implements Serializer {

    private static final List<Class<?>> REGISTRY_LIST = new ArrayList<>();

    private static final ThreadLocal<Tuple2<LoaderBinding, MemoryBuffer>> furyFactory = ThreadLocal.withInitial(() -> {
        LoaderBinding binding = new LoaderBinding(classLoader ->{
            Fury fury = Fury.builder().withRefTracking(true)
            .requireClassRegistration(true).withClassLoader(classLoader).build();
            REGISTRY_LIST.forEach(fury::register);
            return fury;
        });
        MemoryBuffer buffer = MemoryUtils.buffer(32);
        return Tuple2.of(binding, buffer);
    });

    @Override
    public byte[] serialize(Object obj) throws CodecException {
        Tuple2<LoaderBinding, MemoryBuffer> tuple2 = furyFactory.get();
        tuple2.f0.setClassLoader(Thread.currentThread().getContextClassLoader());
        Fury fury = tuple2.f0.get();
        return fury.serialize(obj);
    }

    @Override
    public <T> T deserialize(byte[] data, String classOfT) throws CodecException {
        Tuple2<LoaderBinding, MemoryBuffer> tuple2 = furyFactory.get();
        tuple2.f0.setClassLoader(Thread.currentThread().getContextClassLoader());
        Fury fury = tuple2.f0.get();
        return (T)fury.deserialize(data);
    }

    public static void registry(Class<?> clazz) {
        REGISTRY_LIST.add(clazz);
    }

}
