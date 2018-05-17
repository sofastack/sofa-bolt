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
package com.alipay.remoting.rpc.exception;

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

import com.alipay.remoting.Connection;
import com.alipay.remoting.ConnectionEventType;
import com.alipay.remoting.InvokeCallback;
import com.alipay.remoting.exception.RemotingException;
import com.alipay.remoting.rpc.RpcClient;
import com.alipay.remoting.rpc.RpcResponseFuture;
import com.alipay.remoting.rpc.common.AsyncClientUserProcessor;
import com.alipay.remoting.rpc.common.AsyncServerUserProcessor;
import com.alipay.remoting.rpc.common.BoltServer;
import com.alipay.remoting.rpc.common.CONNECTEventProcessor;
import com.alipay.remoting.rpc.common.DISCONNECTEventProcessor;
import com.alipay.remoting.rpc.common.PortScan;
import com.alipay.remoting.rpc.common.RequestBody;
import com.alipay.remoting.util.RemotingUtil;

/**
 * test async send back exception
 * 
 * @author xiaomin.cxm
 * @version $Id: BasicUsageTest.java, v 0.1 Apr 6, 2016 8:58:36 PM xiaomin.cxm Exp $
 */
public class BasicUsage_AsyncProcessor_Exception_Test {
    static Logger            logger                    = LoggerFactory
                                                           .getLogger(BasicUsage_AsyncProcessor_Exception_Test.class);

    BoltServer               server;
    RpcClient                client;

    int                      port                      = PortScan.select();
    String                   ip                        = "127.0.0.1";
    String                   addr                      = "127.0.0.1:" + port;

    int                      invokeTimes               = 5;

    AsyncServerUserProcessor serverUserProcessor       = new AsyncServerUserProcessor(true, false);
    AsyncClientUserProcessor clientUserProcessor       = new AsyncClientUserProcessor(true, false);
    CONNECTEventProcessor    clientConnectProcessor    = new CONNECTEventProcessor();
    CONNECTEventProcessor    serverConnectProcessor    = new CONNECTEventProcessor();
    DISCONNECTEventProcessor clientDisConnectProcessor = new DISCONNECTEventProcessor();
    DISCONNECTEventProcessor serverDisConnectProcessor = new DISCONNECTEventProcessor();

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
        RequestBody req = new RequestBody(2, "hello world oneway");
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

        Assert.assertTrue(serverConnectProcessor.isConnected());
        Assert.assertEquals(1, serverConnectProcessor.getConnectTimes());
        Assert.assertEquals(invokeTimes, serverUserProcessor.getInvokeTimes());
    }

    @Test
    public void testSync() throws InterruptedException {
        RequestBody req = new RequestBody(1, "hello world sync");
        for (int i = 0; i < invokeTimes; i++) {
            Exception res = null;
            try {
                res = (Exception) client.invokeSync(addr, req, 3000);
                logger.warn("Result received in sync: " + res);
                Assert.assertEquals(res.getClass().getName(), "java.lang.IllegalArgumentException");
            } catch (RemotingException e) {
                String errMsg = "RemotingException caught in sync!";
                logger.error(errMsg, e);
                Assert.fail("Should not reach here!");
            } catch (InterruptedException e) {
                String errMsg = "InterruptedException caught in sync!";
                logger.error(errMsg, e);
                Assert.fail(errMsg);
            }
        }

        Assert.assertTrue(serverConnectProcessor.isConnected());
        Assert.assertEquals(1, serverConnectProcessor.getConnectTimes());
        Assert.assertEquals(invokeTimes, serverUserProcessor.getInvokeTimes());
    }

    @Test
    public void testFuture() throws InterruptedException {
        RequestBody req = new RequestBody(2, "hello world future");
        for (int i = 0; i < invokeTimes; i++) {
            Exception res = null;
            try {
                RpcResponseFuture future = client.invokeWithFuture(addr, req, 3000);
                res = (Exception) future.get();
                Assert.assertEquals(res.getClass().getName(), "java.lang.IllegalArgumentException");
            } catch (RemotingException e) {
                String errMsg = "RemotingException caught in future!";
                logger.error(errMsg, e);
                Assert.fail("Should not reach here!");
            } catch (InterruptedException e) {
                String errMsg = "InterruptedException caught in future!";
                logger.error(errMsg, e);
                Assert.fail(errMsg);
            }
        }

        Assert.assertTrue(serverConnectProcessor.isConnected());
        Assert.assertEquals(1, serverConnectProcessor.getConnectTimes());
        Assert.assertEquals(invokeTimes, serverUserProcessor.getInvokeTimes());
    }

    @Test
    public void testCallback() throws InterruptedException {
        RequestBody req = new RequestBody(1, "hello world callback");
        final List<Object> rets = new ArrayList<Object>(1);
        for (int i = 0; i < invokeTimes; i++) {
            final CountDownLatch latch = new CountDownLatch(1);
            try {
                client.invokeWithCallback(addr, req, new InvokeCallback() {
                    Executor executor = Executors.newCachedThreadPool();

                    @Override
                    public void onResponse(Object result) {
                        logger.warn("Result received in callback: " + result);
                        rets.add(result);
                        latch.countDown();
                    }

                    @Override
                    public void onException(Throwable e) {
                        logger.error("Process exception in callback.", e);
                        rets.add(e);
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
                Assert.fail("Should not reach here!");
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
            Assert.assertEquals(rets.get(0).getClass().getName(),
                "java.lang.IllegalArgumentException");
            rets.clear();
        }

        Assert.assertTrue(serverConnectProcessor.isConnected());
        Assert.assertEquals(1, serverConnectProcessor.getConnectTimes());
        Assert.assertEquals(invokeTimes, serverUserProcessor.getInvokeTimes());
    }

    @Test
    public void testServerSyncUsingConnection() throws Exception {
        Connection clientConn = client.createStandaloneConnection(ip, port, 1000);

        for (int i = 0; i < invokeTimes; i++) {
            RequestBody req1 = new RequestBody(1, RequestBody.DEFAULT_CLIENT_STR);
            Exception serverres = null;
            try {
                serverres = (Exception) client.invokeSync(clientConn, req1, 1000);
                Assert.assertEquals(serverres.getClass().getName(),
                    "java.lang.IllegalArgumentException");
            } catch (RemotingException e) {
                Assert.fail("Should not reach here!");
            }

            Assert.assertNotNull(serverConnectProcessor.getConnection());
            Connection serverConn = serverConnectProcessor.getConnection();
            RequestBody req = new RequestBody(1, RequestBody.DEFAULT_SERVER_STR);
            Exception clientres = null;
            try {
                clientres = (Exception) server.getRpcServer().invokeSync(serverConn, req, 1000);
                Assert.assertEquals(clientres.getClass().getName(),
                    "java.lang.IllegalArgumentException");
            } catch (RemotingException e) {
                Assert.fail("Should not reach here!");
            }
        }

        Assert.assertTrue(serverConnectProcessor.isConnected());
        Assert.assertEquals(1, serverConnectProcessor.getConnectTimes());
        Assert.assertEquals(invokeTimes, serverUserProcessor.getInvokeTimes());
    }

    @Test
    public void testServerSyncUsingAddress() throws Exception {
        Connection clientConn = client.createStandaloneConnection(ip, port, 1000);
        String remote = RemotingUtil.parseRemoteAddress(clientConn.getChannel());
        String local = RemotingUtil.parseLocalAddress(clientConn.getChannel());
        logger.warn("Client say local:" + local);
        logger.warn("Client say remote:" + remote);

        for (int i = 0; i < invokeTimes; i++) {
            RequestBody req1 = new RequestBody(1, RequestBody.DEFAULT_CLIENT_STR);
            Exception serverres = null;
            try {
                serverres = (Exception) client.invokeSync(clientConn, req1, 1000);
                Assert.assertEquals(serverres.getClass().getName(),
                    "java.lang.IllegalArgumentException");
            } catch (RemotingException e) {
                Assert.fail("Should not reach here!");
            }

            Assert.assertNotNull(serverConnectProcessor.getConnection());
            // only when client invoked, the remote address can be get by UserProcessor
            // otherwise, please use ConnectionEventProcessor
            String remoteAddr = serverUserProcessor.getRemoteAddr();
            RequestBody req = new RequestBody(1, RequestBody.DEFAULT_SERVER_STR);

            Exception clientres = null;
            try {
                clientres = (Exception) server.getRpcServer().invokeSync(remoteAddr, req, 1000);
                Assert.assertEquals(clientres.getClass().getName(),
                    "java.lang.IllegalArgumentException");
            } catch (RemotingException e) {
                Assert.fail("Should not reach here!");
            }
        }

        Assert.assertTrue(serverConnectProcessor.isConnected());
        Assert.assertEquals(1, serverConnectProcessor.getConnectTimes());
        Assert.assertEquals(invokeTimes, serverUserProcessor.getInvokeTimes());
    }
}