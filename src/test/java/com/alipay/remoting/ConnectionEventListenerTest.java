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

import com.alipay.remoting.rpc.RpcClient;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class ConnectionEventListenerTest {

    @Test
    public void addConnectionEventProcessorConcurrentTest() throws InterruptedException {
        int concurrentNum = 100;
        CountDownLatch countDownLatch = new CountDownLatch(concurrentNum);
        RpcClient rpcClient = new RpcClient();
        for (int i = 0; i < concurrentNum; ++i) {
            MyThread thread = new MyThread(countDownLatch, rpcClient);
            new Thread(thread).start();
        }
        Assert.assertTrue(countDownLatch.await(2, TimeUnit.SECONDS));
    }

    static class MyThread implements Runnable {
        CountDownLatch countDownLatch;
        RpcClient      rpcClient;

        public MyThread(CountDownLatch countDownLatch, RpcClient rpcClient) {
            this.countDownLatch = countDownLatch;
            this.rpcClient = rpcClient;
        }

        @Override
        public void run() {
            try {
                rpcClient.addConnectionEventProcessor(ConnectionEventType.CONNECT, (remoteAddress, connection) -> {});
            } catch (Exception e) {
                fail();
            } finally {
                countDownLatch.countDown();
            }
        }
    }
}