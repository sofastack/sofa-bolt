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
import com.alipay.remoting.util.ThreadTestUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author muyun
 * @version $Id: RejectionProcessableInvokeCallbackTest.java, v 0.1 2019年12月05日 9:29 PM muyun Exp $
 */
public class RejectionProcessableInvokeCallbackTest {
    private BoltServer                server;
    private RpcClient                 client;

    private int                       port                      = PortScan.select();
    private String                    addr                      = "127.0.0.1:" + port;

    private SimpleServerUserProcessor serverUserProcessor       = new SimpleServerUserProcessor();
    private SimpleClientUserProcessor clientUserProcessor       = new SimpleClientUserProcessor();
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
        final AtomicInteger count = new AtomicInteger(0);
        callback = new RejectionProcessableInvokeCallback() {
            @Override
            public RejectedExecutionPolicy rejectedExecutionPolicy() {
                return RejectedExecutionPolicy.CALLER_RUNS;
            }

            @Override
            public void onResponse(Object result) {
                count.incrementAndGet();
            }

            @Override
            public void onException(Throwable e) {
                Assert.fail("should not reach here");
            }

            @Override
            public Executor getExecutor() {
                return executor;
            }
        };
        try {
            for (int i = 0; i < 5; i++) {
                client.invokeWithCallback(addr, req, callback, 3000);
            }
            ThreadTestUtils.sleep(1000);
            Assert.assertEquals(5, count.get());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testDiscardPolicy() {
        RequestBody req = new RequestBody(1, "hello world sync");
        final AtomicInteger count = new AtomicInteger(0);
        callback = new RejectionProcessableInvokeCallback() {
            @Override
            public RejectedExecutionPolicy rejectedExecutionPolicy() {
                return RejectedExecutionPolicy.DISCARD;
            }

            @Override
            public void onResponse(Object result) {
                count.incrementAndGet();
            }

            @Override
            public void onException(Throwable e) {
                Assert.fail("should not reach here");
            }

            @Override
            public Executor getExecutor() {
                return executor;
            }
        };
        try {
            for (int i = 0; i < 5; i++) {
                client.invokeWithCallback(addr, req, callback, 3000);
            }
            ThreadTestUtils.sleep(1000);
            Assert.assertEquals(3, count.get());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testCallerHandleExceptionPolicy() {
        RequestBody req = new RequestBody(1, "hello world sync");
        final AtomicInteger errCount = new AtomicInteger(0);
        callback = new RejectionProcessableInvokeCallback() {
            @Override
            public RejectedExecutionPolicy rejectedExecutionPolicy() {
                return RejectedExecutionPolicy.CALLER_HANDLE_EXCEPTION;
            }

            @Override
            public void onResponse(Object result) {
            }

            @Override
            public void onException(Throwable e) {
                errCount.incrementAndGet();
            }

            @Override
            public Executor getExecutor() {
                return executor;
            }
        };
        try {
            for (int i = 0; i < 5; i++) {
                client.invokeWithCallback(addr, req, callback, 3000);
            }
            ThreadTestUtils.sleep(1000);
            Assert.assertEquals(2, errCount.get());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

}
