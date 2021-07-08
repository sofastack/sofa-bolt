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

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author zhaowang
 * @version : ThreadLocalArriveTimeHolderTest.java, v 0.1 2021年07月07日 5:45 下午 zhaowang
 */
public class ThreadLocalArriveTimeHolderTest {

    @Test
    public void test() {
        long start = System.nanoTime();
        ThreadLocalArriveTimeHolder.arrive("a");
        long end = System.nanoTime();
        ThreadLocalArriveTimeHolder.arrive("a");
        long time = ThreadLocalArriveTimeHolder.getAndClear("a");
        Assert.assertTrue(time >= start);
        Assert.assertTrue(time <= end);
        Assert.assertEquals(-1, ThreadLocalArriveTimeHolder.getAndClear("a"));
    }

    @Test
    public void testRemoveNull() {
        Assert.assertEquals(-1, ThreadLocalArriveTimeHolder.getAndClear(null));
    }

    @Test
    public void testMultiThread() throws InterruptedException {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        long start = System.nanoTime();
        ThreadLocalArriveTimeHolder.arrive("a");
        long end = System.nanoTime();
        ThreadLocalArriveTimeHolder.arrive("a");
        long time = ThreadLocalArriveTimeHolder.getAndClear("a");
        Assert.assertTrue(time >= start);
        Assert.assertTrue(time <= end);
        Assert.assertEquals(-1, ThreadLocalArriveTimeHolder.getAndClear("a"));

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                long start = System.nanoTime();
                ThreadLocalArriveTimeHolder.arrive("a");
                long end = System.nanoTime();
                long time = ThreadLocalArriveTimeHolder.getAndClear("a");
                Assert.assertTrue(time >= start);
                Assert.assertTrue(time <= end);
                Assert.assertEquals(-1, ThreadLocalArriveTimeHolder.getAndClear("a"));
                countDownLatch.countDown();
            }
        };
        new Thread(runnable).start();
        Assert.assertTrue(countDownLatch.await(2, TimeUnit.SECONDS));
    }
}