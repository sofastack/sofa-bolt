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
package com.alipay.remoting.util;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * IDGenerator is used for generating request id in integer form.
 * 
 * @author jiangping
 * @version $Id: IDGenerator.java, v 0.1 2015-9-23 PM5:28:58 tao Exp $
 */
public class IDGenerator {
    private static final AtomicInteger id = new AtomicInteger(0);

    /**
     * generate the next id
     * 
     * @return
     */
    public static int nextId() {
        return id.incrementAndGet();
    }

    public static void resetId() {
        id.set(0);
    }
}
