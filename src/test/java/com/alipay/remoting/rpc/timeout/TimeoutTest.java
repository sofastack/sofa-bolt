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
package com.alipay.remoting.rpc.timeout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alipay.remoting.ConnectionEventType;
import com.alipay.remoting.InvokeCallback;
import com.alipay.remoting.exception.RemotingException;
import com.alipay.remoting.rpc.BasicUsageTest;
import com.alipay.remoting.rpc.RpcClient;
import com.alipay.remoting.rpc.RpcResponseFuture;
import com.alipay.remoting.rpc.common.BoltServer;
import com.alipay.remoting.rpc.common.CONNECTEventProcessor;
import com.alipay.remoting.rpc.common.DISCONNECTEventProcessor;
import com.alipay.remoting.rpc.common.PortScan;
import com.alipay.remoting.rpc.common.RequestBody;
import com.alipay.remoting.rpc.common.SimpleClientUserProcessor;
import com.alipay.remoting.rpc.common.SimpleServerUserProcessor;
import com.alipay.remoting.rpc.exception.InvokeTimeoutException;

/**
 * Timeout Test
 * 
 * @author xiaomin.cxm
 * @version $Id: TimeoutTest.java, v 0.1 Apr 8, 2016 4:31:10 PM xiaomin.cxm Exp $
 */
public class TimeoutTest {

    static Logger             logger                    = LoggerFactory
                                                            .getLogger(BasicUsageTest.class);

    BoltServer                server;
    RpcClient                 client;

    int                       port                      = PortScan.select();
    String                    ip                        = "127.0.0.1";
    String                    addr                      = "127.0.0.1:" + port;

    int                       invokeTimes               = 5;
    int                       timeout                   = 250;

    SimpleServerUserProcessor serverUserProcessor       = new SimpleServerUserProcessor(timeout * 2);
    SimpleClientUserProcessor clientUserProcessor       = new SimpleClientUserProcessor();
    CONNECTEventProcessor     clientConnectProcessor    = new CONNECTEventProcessor();
    CONNECTEventProcessor     serverConnectProcessor    = new CONNECTEventProcessor();
    DISCONNECTEventProcessor  clientDisConnectProcessor = new DISCONNECTEventProcessor();
    DISCONNECTEventProcessor  serverDisConnectProcessor = new DISCONNECTEventProcessor();

    @Before
    public void init() {
        server = new BoltServer(port);
        server.start();
        server.addConnectionEventProcessor(ConnectionEventType.CONNECT, serverConnectProcessor);
        server.addConnectionEventProcessor(ConnectionEventType.CLOSE, serverDisConnectProcessor);
        server.registerUserProcessor(serverUserProcessor);

        client = new RpcClient();
        client.addConnectionEventProcessor(ConnectionEventType.CONNECT, clientConnectProcessor);
        client.addConnectionEventProcessor(ConnectionEventType.CLOSE, clientDisConnectProcessor);
        client.registerUserProcessor(clientUserProcessor);
        client.init();
    }

    @After
    public void stop() {
        try {
            server.stop();
            Thread.sleep(100);
        } catch (InterruptedException e) {
            logger.error("Stop server failed!", e);
        }
    }

    @Test
    public void testSyncTimeout() {
        RequestBody b1 = new RequestBody(1, "Hello world!");
        Object obj = null;
        try {
            obj = client.invokeSync(addr, b1, timeout);
            Assert.fail("Should not reach here!");
        } catch (InvokeTimeoutException e) {
            Assert.assertNull(obj);
        } catch (RemotingException e) {
            logger.error("Other RemotingException but InvokeTimeoutException occurred in sync", e);
            Assert.fail("Should not reach here!");
        } catch (InterruptedException e) {
            logger.error("InterruptedException in sync", e);
            Assert.fail("Should not reach here!");
        }
    }

    @Test
    public void testSyncOK() {
        RequestBody b1 = new RequestBody(1, "Hello world!");
        try {
            client.invokeSync(addr, b1, timeout + 500);
        } catch (InvokeTimeoutException e) {
            Assert.fail("Should not reach here!");
        } catch (RemotingException e) {
            logger.error("Other RemotingException but InvokeTimeoutException occurred in sync", e);
            Assert.fail("Should not reach here!");
        } catch (InterruptedException e) {
            logger.error("InterruptedException in sync", e);
            Assert.fail("Should not reach here!");
        }
    }

    /**
     * future try to get result wait a longer time than specified timeout in invokeWithFuture
     */
    @Test
    public void testFutureWithLongerTime() {
        RequestBody b4 = new RequestBody(4, "Hello world!");
        Object obj = null;
        try {
            RpcResponseFuture future = client.invokeWithFuture(addr, b4, timeout);
            obj = future.get(timeout + 100);
            Assert.fail("Should not reach here!");
        } catch (InvokeTimeoutException e) {
            Assert.assertNull(obj);
        } catch (RemotingException e) {
            logger
                .error("Other RemotingException but InvokeTimeoutException occurred in future", e);
            Assert.fail("Should not reach here!");
        } catch (InterruptedException e) {
            logger.error("InterruptedException in sync", e);
            Assert.fail("Should not reach here!");
        }

    }

    /**
     * future try to get result wait a shorter time or just the same with specified timeout in invokeWithFuture
     */
    @Test
    public void testFutureWithShorterOrJustTheSameTime() {
        RequestBody b4 = new RequestBody(4, "Hello world!");
        Object obj = null;
        try {
            RpcResponseFuture future = client.invokeWithFuture(addr, b4, timeout);
            obj = future.get(timeout - 50);
            Assert.fail("Should not reach here!");
        } catch (InvokeTimeoutException e) {
            Assert.assertNull(obj);
        } catch (RemotingException e) {
            logger.error("Should not catch any exception here", e);
            Assert.fail("Should not reach here!");
        } catch (InterruptedException e) {
            logger.error("InterruptedException in sync", e);
            Assert.fail("Should not reach here!");
        }

    }

    @Test
    public void testCallback() throws InterruptedException {
        RequestBody b3 = new RequestBody(3, "Hello world!");
        final CountDownLatch latch = new CountDownLatch(1);
        final List<Class<?>> ret = new ArrayList<Class<?>>();
        try {
            client.invokeWithCallback(addr, b3, new InvokeCallback() {

                @Override
                public void onResponse(Object result) {
                    Assert.fail("Should not reach here!");
                }

                @Override
                public void onException(Throwable e) {
                    //logger.error("Process exception in callback.", e);
                    ret.add(e.getClass());
                    latch.countDown();
                }

                @Override
                public Executor getExecutor() {
                    return null;
                }

            }, timeout);

        } catch (RemotingException e) {
            logger
                .error("Other RemotingException but InvokeTimeoutException occurred in future", e);
            Assert.fail("Should not reach here!");
        }
        latch.await();
        Assert.assertEquals(InvokeTimeoutException.class, ret.get(0));
    }
}
