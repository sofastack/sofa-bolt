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

import io.netty.util.concurrent.FastThreadLocal;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zhaowang
 * @version : ThreadLocalTimeHolder.java, v 0.1 2021年07月01日 3:05 下午 zhaowang
 */
public class ThreadLocalArriveTimeHolder {
    private static FastThreadLocal<Map<Integer, Long>> arriveTimeInNano = new FastThreadLocal<Map<Integer, Long>>();

    static {
        arriveTimeInNano.set(new HashMap<Integer, Long>(256));
    }

    public static void arrive(int requestId) {

        Map<Integer, Long> map = arriveTimeInNano.get();
        if (map.get(requestId) == null) {
            map.put(requestId, System.nanoTime());
        }
    }

    public static long getAndClear(int requestId) {
        Map<Integer, Long> map = arriveTimeInNano.get();
        Long result = map.remove(requestId);
        if (result == null) {
            return -1;
        }
        return result;
    }

}