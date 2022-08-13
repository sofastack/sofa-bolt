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

import org.junit.Test;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class HessianSerializerTest {

    private static HessianSerializer serializer = new HessianSerializer();

    @Test
    public void concurrentSerializeTest() throws InterruptedException {
        int concurrentNum = 10;
        CountDownLatch countDownLatch = new CountDownLatch(concurrentNum);
        for (int i = 0; i < concurrentNum; ++i) {
            MyThread thread = new MyThread(countDownLatch);
            new Thread(thread).start();
        }
        countDownLatch.await(2, TimeUnit.SECONDS);

    }

    static class MyThread implements Runnable {
        CountDownLatch countDownLatch;

        public MyThread(CountDownLatch countDownLatch) {
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {
            try {
                for (int i = 0; i < 100; i++) {
                    String randomStr = UUID.randomUUID().toString();
                    byte[] bytes = serializer.serialize(randomStr);
                    String o = serializer.deserialize(bytes, null);
                    assertEquals(o, randomStr);
                }
            } catch (Exception e) {
                fail();
            } finally {
                countDownLatch.countDown();
            }
        }
    }
}