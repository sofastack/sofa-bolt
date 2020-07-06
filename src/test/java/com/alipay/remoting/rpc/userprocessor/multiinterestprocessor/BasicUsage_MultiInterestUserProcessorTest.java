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
package com.alipay.remoting.rpc.userprocessor.multiinterestprocessor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alipay.remoting.ConnectionEventType;
import com.alipay.remoting.InvokeCallback;
import com.alipay.remoting.exception.RemotingException;
import com.alipay.remoting.rpc.RpcClient;
import com.alipay.remoting.rpc.RpcResponseFuture;
import com.alipay.remoting.rpc.common.BoltServer;
import com.alipay.remoting.rpc.common.CONNECTEventProcessor;
import com.alipay.remoting.rpc.common.DISCONNECTEventProcessor;
import com.alipay.remoting.rpc.common.PortScan;

/**
 * @antuor muyun.cyt (muyun.cyt@antfin.com)  2018/7/5   11:20 AM
 */
public class BasicUsage_MultiInterestUserProcessorTest {
    static Logger                          logger                    = LoggerFactory
                                                                         .getLogger(BasicUsage_MultiInterestUserProcessorTest.class);

    BoltServer                             server;
    RpcClient                              client;

    int                                    port                      = PortScan.select();
    String                                 ip                        = "127.0.0.1";
    String                                 addr                      = "127.0.0.1:" + port;

    int                                    invokeTimes               = 5;

    SimpleServerMultiInterestUserProcessor serverUserProcessor       = new SimpleServerMultiInterestUserProcessor();
    SimpleClientMultiInterestUserProcessor clientUserProcessor       = new SimpleClientMultiInterestUserProcessor();
    CONNECTEventProcessor                  clientConnectProcessor    = new CONNECTEventProcessor();
    CONNECTEventProcessor                  serverConnectProcessor    = new CONNECTEventProcessor();
    DISCONNECTEventProcessor               clientDisConnectProcessor = new DISCONNECTEventProcessor();
    DISCONNECTEventProcessor               serverDisConnectProcessor = new DISCONNECTEventProcessor();

    @Before
    public void init() {
        server = new BoltServer(port, true);
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
    public void testOneway() throws InterruptedException {
        MultiInterestBaseRequestBody req = new RequestBodyC1(2, "hello world oneway--c1");
        MultiInterestBaseRequestBody req2 = new RequestBodyC2(3, "hello world oneway--c2");
        for (int i = 0; i < invokeTimes; i++) {
            try {
                client.oneway(addr, req);
                Thread.sleep(100);
            } catch (RemotingException e) {
                String errMsg = "RemotingException caught in oneway!";
                logger.error(errMsg, e);
                Assert.fail(errMsg);
            }
        }
        for (int j = 0; j < invokeTimes; j++) {
            try {
                client.oneway(addr, req2);
                Thread.sleep(100);
            } catch (RemotingException e) {
                String errMsg = "RemotingException caught in oneway!";
                logger.error(errMsg, e);
                Assert.fail(errMsg);
            }
        }
        //System.out.println(serverUserProcessor.getInvokeTimesC1()+" "+serverUserProcessor.getInvokeTimesC2());

        Assert.assertTrue(serverConnectProcessor.isConnected());
        Assert.assertEquals(1, serverConnectProcessor.getConnectTimes());
        Assert.assertEquals(invokeTimes, serverUserProcessor.getInvokeTimesC1());
        Assert.assertEquals(invokeTimes, serverUserProcessor.getInvokeTimesC2());
    }

    @Test
    public void testSync() throws InterruptedException {
        MultiInterestBaseRequestBody req = new RequestBodyC1(1, "hello world sync--c1");
        MultiInterestBaseRequestBody req2 = new RequestBodyC2(4, "hello world sync--c2");
        for (int i = 0; i < invokeTimes; i++) {
            try {
                String res = (String) client.invokeSync(addr, req, 3000);
                logger.warn("Result received in sync: " + res);
                Assert.assertEquals(RequestBodyC1.DEFAULT_SERVER_RETURN_STR, res);
            } catch (RemotingException e) {
                String errMsg = "RemotingException caught in sync!";
                logger.error(errMsg, e);
                Assert.fail(errMsg);
            } catch (InterruptedException e) {
                String errMsg = "InterruptedException caught in sync!";
                logger.error(errMsg, e);
                Assert.fail(errMsg);
            }
        }
        for (int i = 0; i < invokeTimes; i++) {
            try {
                String res = (String) client.invokeSync(addr, req2, 3000);
                logger.warn("Result received in sync: " + res);
                Assert.assertEquals(RequestBodyC2.DEFAULT_SERVER_RETURN_STR, res);
            } catch (RemotingException e) {
                String errMsg = "RemotingException caught in sync!";
                logger.error(errMsg, e);
                Assert.fail(errMsg);
            } catch (InterruptedException e) {
                String errMsg = "InterruptedException caught in sync!";
                logger.error(errMsg, e);
                Assert.fail(errMsg);
            }
        }

        Assert.assertTrue(serverConnectProcessor.isConnected());
        Assert.assertEquals(1, serverConnectProcessor.getConnectTimes());
        Assert.assertEquals(invokeTimes, serverUserProcessor.getInvokeTimesC1());
        Assert.assertEquals(invokeTimes, serverUserProcessor.getInvokeTimesC2());
    }

    @Test
    public void testFuture() throws InterruptedException {
        MultiInterestBaseRequestBody req = new RequestBodyC1(2, "hello world future--c1");
        MultiInterestBaseRequestBody req2 = new RequestBodyC2(3, "hello world future--c2");
        for (int i = 0; i < invokeTimes; i++) {
            try {
                RpcResponseFuture future = client.invokeWithFuture(addr, req, 3000);
                String res = (String) future.get();
                Assert.assertEquals(RequestBodyC1.DEFAULT_SERVER_RETURN_STR, res);
            } catch (RemotingException e) {
                String errMsg = "RemotingException caught in future!";
                logger.error(errMsg, e);
                Assert.fail(errMsg);
            } catch (InterruptedException e) {
                String errMsg = "InterruptedException caught in future!";
                logger.error(errMsg, e);
                Assert.fail(errMsg);
            }
        }
        for (int i = 0; i < invokeTimes; i++) {
            try {
                RpcResponseFuture future = client.invokeWithFuture(addr, req2, 3000);
                String res = (String) future.get();
                Assert.assertEquals(RequestBodyC2.DEFAULT_SERVER_RETURN_STR, res);
            } catch (RemotingException e) {
                String errMsg = "RemotingException caught in future!";
                logger.error(errMsg, e);
                Assert.fail(errMsg);
            } catch (InterruptedException e) {
                String errMsg = "InterruptedException caught in future!";
                logger.error(errMsg, e);
                Assert.fail(errMsg);
            }
        }

        Assert.assertTrue(serverConnectProcessor.isConnected());
        Assert.assertEquals(1, serverConnectProcessor.getConnectTimes());
        Assert.assertEquals(invokeTimes, serverUserProcessor.getInvokeTimesC1());
        Assert.assertEquals(invokeTimes, serverUserProcessor.getInvokeTimesC2());
    }

    @Test
    public void testCallback() throws InterruptedException {
        MultiInterestBaseRequestBody req = new RequestBodyC1(1, "hello world callback--c1");
        MultiInterestBaseRequestBody req2 = new RequestBodyC2(1, "hello world callback--c2");
        final List<String> rets = new ArrayList<String>(1);
        for (int i = 0; i < invokeTimes; i++) {
            final CountDownLatch latch = new CountDownLatch(1);
            try {
                client.invokeWithCallback(addr, req, new InvokeCallback() {
                    Executor executor = Executors.newCachedThreadPool();

                    @Override
                    public void onResponse(Object result) {
                        logger.warn("Result received in callback: " + result);
                        rets.add((String) result);
                        latch.countDown();
                    }

                    @Override
                    public void onException(Throwable e) {
                        logger.error("Process exception in callback.", e);
                        latch.countDown();
                    }

                    @Override
                    public Executor getExecutor() {
                        return executor;
                    }

                }, 1000);

            } catch (RemotingException e) {
                latch.countDown();
                String errMsg = "RemotingException caught in callback!";
                logger.error(errMsg, e);
                Assert.fail(errMsg);
            }
            try {
                latch.await();
            } catch (InterruptedException e) {
                String errMsg = "InterruptedException caught in callback!";
                logger.error(errMsg, e);
                Assert.fail(errMsg);
            }
            if (rets.size() == 0) {
                Assert.fail("No result! Maybe exception caught!");
            }
            Assert.assertEquals(RequestBodyC1.DEFAULT_SERVER_RETURN_STR, rets.get(0));
            rets.clear();
        }
        final List<String> rets2 = new ArrayList<String>(1);
        for (int i = 0; i < invokeTimes; i++) {
            final CountDownLatch latch = new CountDownLatch(1);
            try {
                client.invokeWithCallback(addr, req2, new InvokeCallback() {
                    Executor executor = Executors.newCachedThreadPool();

                    @Override
                    public void onResponse(Object result) {
                        logger.warn("Result received in callback: " + result);
                        rets2.add((String) result);
                        latch.countDown();
                    }

                    @Override
                    public void onException(Throwable e) {
                        logger.error("Process exception in callback.", e);
                        latch.countDown();
                    }

                    @Override
                    public Executor getExecutor() {
                        return executor;
                    }

                }, 1000);

            } catch (RemotingException e) {
                latch.countDown();
                String errMsg = "RemotingException caught in callback!";
                logger.error(errMsg, e);
                Assert.fail(errMsg);
            }
            try {
                latch.await();
            } catch (InterruptedException e) {
                String errMsg = "InterruptedException caught in callback!";
                logger.error(errMsg, e);
                Assert.fail(errMsg);
            }
            if (rets2.size() == 0) {
                Assert.fail("No result! Maybe exception caught!");
            }
            Assert.assertEquals(RequestBodyC2.DEFAULT_SERVER_RETURN_STR, rets2.get(0));
            rets.clear();
        }

        Assert.assertTrue(serverConnectProcessor.isConnected());
        Assert.assertEquals(1, serverConnectProcessor.getConnectTimes());
        Assert.assertEquals(invokeTimes, serverUserProcessor.getInvokeTimesC1());
        Assert.assertEquals(invokeTimes, serverUserProcessor.getInvokeTimesC2());
    }

}
