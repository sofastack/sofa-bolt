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
package com.alipay.remoting.rpc.callback;

import com.alipay.remoting.*;
import com.alipay.remoting.rpc.RpcClient;
import com.alipay.remoting.rpc.common.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author muyun
 * @version $Id: RejectionProcessableInvokeCallbackTest.java, v 0.1 2019年12月05日 9:29 PM muyun Exp $
 */
public class RejectionProcessableInvokeCallbackTest {
    private BoltServer                server;
    private RpcClient                 client;

    private int                       port                      = PortScan.select();
    private String                    addr                      = "127.0.0.1:" + port;

    private SimpleServerUserProcessor serverUserProcessor       = new SimpleServerUserProcessor(0,
                                                                    1, 3, 60, 500);
    private SimpleClientUserProcessor clientUserProcessor       = new SimpleClientUserProcessor(0,
                                                                    1, 3, 60, 500);
    private CONNECTEventProcessor     clientConnectProcessor    = new CONNECTEventProcessor();
    private CONNECTEventProcessor     serverConnectProcessor    = new CONNECTEventProcessor();
    private DISCONNECTEventProcessor  clientDisConnectProcessor = new DISCONNECTEventProcessor();
    private DISCONNECTEventProcessor  serverDisConnectProcessor = new DISCONNECTEventProcessor();

    private ThreadPoolExecutor        executor;
    private InvokeCallback            callback;

    @Before
    public void setUp() {
        server = new BoltServer(port, true);
        server.start();
        server.addConnectionEventProcessor(ConnectionEventType.CONNECT, serverConnectProcessor);
        server.addConnectionEventProcessor(ConnectionEventType.CLOSE, serverDisConnectProcessor);
        server.registerUserProcessor(serverUserProcessor);

        client = new RpcClient();
        client.addConnectionEventProcessor(ConnectionEventType.CONNECT, clientConnectProcessor);
        client.addConnectionEventProcessor(ConnectionEventType.CLOSE, clientDisConnectProcessor);
        client.registerUserProcessor(clientUserProcessor);
        client.startup();

        executor = new ThreadPoolExecutor(1, 1, 60, TimeUnit.SECONDS,
            new ArrayBlockingQueue<Runnable>(2), new NamedThreadFactory(
                "test-rejection-processable"));
    }

    @After
    public void tearDown() {
        server.stop();
        executor.shutdown();
    }

    @Test
    public void testCallerRunsPolicy() {
        RequestBody req = new RequestBody(1, "hello world sync");

        int invokeCount = 50;
        final CountDownLatch latch = new CountDownLatch(invokeCount);
        final AtomicInteger count = new AtomicInteger(0);
        callback = new RejectionProcessableInvokeCallback() {
            @Override
            public RejectedExecutionPolicy rejectedExecutionPolicy() {
                return RejectedExecutionPolicy.CALLER_RUNS;
            }

            @Override
            public void onResponse(Object result) {
                count.incrementAndGet();
                latch.countDown();
            }

            @Override
            public void onException(Throwable e) {
                latch.countDown();
            }

            @Override
            public Executor getExecutor() {
                return executor;
            }
        };
        try {
            for (int i = 0; i < invokeCount; i++) {
                client.invokeWithCallback(addr, req, callback, 100);
            }

            try {
                latch.await(200L * invokeCount, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ignore) {
            }

            Assert.assertEquals(0, latch.getCount());
            // Invoke callbacks are triggered by IO threads after the thread pool is full, so the total number of normal responses received is equal to the total number of calls
            Assert.assertEquals(invokeCount, count.get());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testDiscardPolicy() {
        RequestBody req = new RequestBody(1, "hello world sync");

        int invokeCount = 20;
        final CountDownLatch latch = new CountDownLatch(invokeCount);
        callback = new RejectionProcessableInvokeCallback() {
            @Override
            public RejectedExecutionPolicy rejectedExecutionPolicy() {
                return RejectedExecutionPolicy.DISCARD;
            }

            @Override
            public void onResponse(Object result) {
                latch.countDown();
            }

            @Override
            public void onException(Throwable e) {
                latch.countDown();
            }

            @Override
            public Executor getExecutor() {
                return executor;
            }
        };
        try {
            for (int i = 0; i < invokeCount; i++) {
                client.invokeWithCallback(addr, req, callback, 100);
            }

            try {
                latch.await(150L * invokeCount, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ignore) {
            }

            // The total number of callbacks received in discard mode must be less than the total number of requests
            Assert.assertTrue(latch.getCount() > 0);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testCallerHandleExceptionPolicy() {
        RequestBody req = new RequestBody(1, "hello world sync");

        int invokeCount = 50;
        final CountDownLatch latch = new CountDownLatch(invokeCount);
        final AtomicInteger errCount = new AtomicInteger(0);
        callback = new RejectionProcessableInvokeCallback() {
            @Override
            public RejectedExecutionPolicy rejectedExecutionPolicy() {
                return RejectedExecutionPolicy.CALLER_HANDLE_EXCEPTION;
            }

            @Override
            public void onResponse(Object result) {
                latch.countDown();
            }

            @Override
            public void onException(Throwable e) {
                latch.countDown();
                errCount.incrementAndGet();
            }

            @Override
            public Executor getExecutor() {
                return executor;
            }
        };
        try {
            for (int i = 0; i < invokeCount; i++) {
                client.invokeWithCallback(addr, req, callback, 100);
            }

            try {
                latch.await(150L * invokeCount, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ignore) {
            }

            // Exception callbacks are triggered by IO threads after the thread pool is full, so the total number of responses received is equal to the total number of calls
            Assert.assertEquals(0, latch.getCount());
            // The number of exception callbacks triggered by IO threads must exist
            Assert.assertTrue(errCount.get() > 0);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

}
