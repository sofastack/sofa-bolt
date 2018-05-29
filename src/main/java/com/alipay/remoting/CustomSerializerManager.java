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

import java.util.concurrent.ConcurrentHashMap;

/**
 * Manage the custom serializer according to the class name.
 * 
 * @author jiangping
 * @version $Id: CustomSerializerManager.java, v 0.1 2015-10-20 AM10:46:39 tao Exp $
 */
public class CustomSerializerManager {

    /** For rpc */
    private static ConcurrentHashMap<String/* class name */, CustomSerializer>        classCustomSerializer   = new ConcurrentHashMap<String, CustomSerializer>();

    /** For user defined command */
    private static ConcurrentHashMap<CommandCode/* command code */, CustomSerializer> commandCustomSerializer = new ConcurrentHashMap<CommandCode, CustomSerializer>();

    /**
     * Register custom serializer for class name.
     * 
     * @param className
     * @param serializer
     * @return
     */
    public static void registerCustomSerializer(String className, CustomSerializer serializer) {
        CustomSerializer prevSerializer = classCustomSerializer.putIfAbsent(className, serializer);
        if (prevSerializer != null) {
            throw new RuntimeException("CustomSerializer has been registered for class: "
                                       + className + ", the custom serializer is: "
                                       + prevSerializer.getClass().getName());
        }
    }

    /**
     * Get the custom serializer for class name.
     * 
     * @param className
     * @return
     */
    public static CustomSerializer getCustomSerializer(String className) {
        if (!classCustomSerializer.isEmpty()) {
            return classCustomSerializer.get(className);
        }
        return null;
    }

    /**
     * Register custom serializer for command code.
     * 
     * @param code
     * @param serializer
     * @return
     */
    public static void registerCustomSerializer(CommandCode code, CustomSerializer serializer) {
        CustomSerializer prevSerializer = commandCustomSerializer.putIfAbsent(code, serializer);
        if (prevSerializer != null) {
            throw new RuntimeException("CustomSerializer has been registered for command code: "
                                       + code + ", the custom serializer is: "
                                       + prevSerializer.getClass().getName());
        }
    }

    /**
     * Get the custom serializer for command code.
     * 
     * @param code
     * @return
     */
    public static CustomSerializer getCustomSerializer(CommandCode code) {
        if (!commandCustomSerializer.isEmpty()) {
            return commandCustomSerializer.get(code);
        }
        return null;
    }

    /**
     * clear the custom serializers.
     */
    public static void clear() {
        classCustomSerializer.clear();
        commandCustomSerializer.clear();
    }
}
