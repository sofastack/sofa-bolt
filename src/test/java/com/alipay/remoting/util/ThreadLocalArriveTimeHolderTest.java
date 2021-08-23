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

import io.netty.channel.embedded.EmbeddedChannel;
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
        EmbeddedChannel channel = new EmbeddedChannel();
        long start = System.nanoTime();
        ThreadLocalArriveTimeHolder.arrive(channel, 1);
        long end = System.nanoTime();
        ThreadLocalArriveTimeHolder.arrive(channel, 1);
        long time = ThreadLocalArriveTimeHolder.getAndClear(channel, 1);
        Assert.assertTrue(time >= start);
        Assert.assertTrue(time <= end);
        Assert.assertEquals(-1, ThreadLocalArriveTimeHolder.getAndClear(channel, 1));
    }

    @Test
    public void testRemoveNull() {
        EmbeddedChannel channel = new EmbeddedChannel();
        Assert.assertEquals(-1, ThreadLocalArriveTimeHolder.getAndClear(channel, 1));
    }

    @Test
    public void testMultiThread() throws InterruptedException {
        final EmbeddedChannel channel = new EmbeddedChannel();
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        long start = System.nanoTime();
        ThreadLocalArriveTimeHolder.arrive(channel, 1);
        long end = System.nanoTime();
        ThreadLocalArriveTimeHolder.arrive(channel, 1);
        long time = ThreadLocalArriveTimeHolder.getAndClear(channel, 1);
        Assert.assertTrue(time >= start);
        Assert.assertTrue(time <= end);
        Assert.assertEquals(-1, ThreadLocalArriveTimeHolder.getAndClear(channel, 1));
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                long start = System.nanoTime();
                ThreadLocalArriveTimeHolder.arrive(channel, 1);
                long end = System.nanoTime();
                long time = ThreadLocalArriveTimeHolder.getAndClear(channel, 1);
                Assert.assertTrue(time >= start);
                Assert.assertTrue(time <= end);
                Assert.assertEquals(-1, ThreadLocalArriveTimeHolder.getAndClear(channel, 1));
                countDownLatch.countDown();
            }
        };
        new Thread(runnable).start();
        Assert.assertTrue(countDownLatch.await(2, TimeUnit.SECONDS));
    }
}
