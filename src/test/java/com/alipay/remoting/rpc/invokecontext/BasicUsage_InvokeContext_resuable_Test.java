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
package com.alipay.remoting.rpc.invokecontext;

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
import com.alipay.remoting.InvokeContext;
import com.alipay.remoting.exception.RemotingException;
import com.alipay.remoting.rpc.RpcClient;
import com.alipay.remoting.rpc.RpcResponseFuture;
import com.alipay.remoting.rpc.common.BoltServer;
import com.alipay.remoting.rpc.common.CONNECTEventProcessor;
import com.alipay.remoting.rpc.common.DISCONNECTEventProcessor;
import com.alipay.remoting.rpc.common.PortScan;
import com.alipay.remoting.rpc.common.RequestBody;
import com.alipay.remoting.rpc.common.SimpleClientUserProcessor;
import com.alipay.remoting.rpc.common.SimpleServerUserProcessor;
import com.alipay.remoting.util.RemotingUtil;
import com.alipay.remoting.util.TraceLogUtil;

/**
 * basic usage test with invoke context
 *
 * @author xiaomin.cxm
 * @version $Id: BasicUsage_InvokeContext_resuable_Test.java, v 0.1 Apr 6, 2016 8:58:36 PM xiaomin.cxm Exp $
 */
public class BasicUsage_InvokeContext_resuable_Test {
    static Logger               logger                    = LoggerFactory
                                                              .getLogger(BasicUsage_InvokeContext_resuable_Test.class);

    BoltServer                  server;
    RpcClient                   client;

    int                         port                      = PortScan.select();
    String                      ip                        = "127.0.0.1";
    String                      addr                      = "127.0.0.1:" + port;

    int                         invokeTimes               = 5;

    SimpleServerUserProcessor   serverUserProcessor       = new SimpleServerUserProcessor();
    SimpleClientUserProcessor   clientUserProcessor       = new SimpleClientUserProcessor();
    CONNECTEventProcessor       clientConnectProcessor    = new CONNECTEventProcessor();
    CONNECTEventProcessor       serverConnectProcessor    = new CONNECTEventProcessor();
    DISCONNECTEventProcessor    clientDisConnectProcessor = new DISCONNECTEventProcessor();
    DISCONNECTEventProcessor    serverDisConnectProcessor = new DISCONNECTEventProcessor();

    private static final String TESTKEY                   = "test";

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
                InvokeContext invokeContext = new InvokeContext();
                invokeContext.put(TESTKEY, "TESTVALUE");
                client.oneway(addr, req, invokeContext);
                Assert.assertEquals("127.0.0.1", invokeContext.get(InvokeContext.CLIENT_LOCAL_IP));
                Assert.assertEquals("127.0.0.1", invokeContext.get(InvokeContext.CLIENT_REMOTE_IP));
                Assert.assertNotNull(invokeContext.get(InvokeContext.CLIENT_LOCAL_PORT));
                Assert.assertNotNull(invokeContext.get(InvokeContext.CLIENT_REMOTE_PORT));

                Assert.assertNotNull(invokeContext.get(InvokeContext.CLIENT_CONN_CREATETIME));
                logger.warn("CLIENT_CONN_CREATETIME:"
                            + invokeContext.get(InvokeContext.CLIENT_CONN_CREATETIME));

                Assert.assertEquals(invokeContext.get(TESTKEY), "TESTVALUE");
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
            try {
                InvokeContext invokeContext = new InvokeContext();
                invokeContext.put(TESTKEY, "TESTVALUE");
                String res = (String) client.invokeSync(addr, req, invokeContext, 3000);
                logger.warn("Result received in sync: " + res);
                Assert.assertEquals(RequestBody.DEFAULT_SERVER_RETURN_STR, res);

                Assert.assertEquals("127.0.0.1", invokeContext.get(InvokeContext.CLIENT_LOCAL_IP));
                Assert.assertEquals("127.0.0.1", invokeContext.get(InvokeContext.CLIENT_REMOTE_IP));
                Assert.assertNotNull(invokeContext.get(InvokeContext.CLIENT_LOCAL_PORT));
                Assert.assertNotNull(invokeContext.get(InvokeContext.CLIENT_REMOTE_PORT));

                Assert.assertNotNull(invokeContext.get(InvokeContext.CLIENT_CONN_CREATETIME));
                logger.warn("CLIENT_CONN_CREATETIME:"
                            + invokeContext.get(InvokeContext.CLIENT_CONN_CREATETIME));

                Assert.assertEquals(invokeContext.get(TESTKEY), "TESTVALUE");
                TraceLogUtil.printConnectionTraceLog(logger, "0af4232214701387943901253",
                    invokeContext);
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
        Assert.assertEquals(invokeTimes, serverUserProcessor.getInvokeTimes());
    }

    @Test
    public void testFuture() throws InterruptedException {
        RequestBody req = new RequestBody(2, "hello world future");
        for (int i = 0; i < invokeTimes; i++) {
            try {
                InvokeContext invokeContext = new InvokeContext();
                invokeContext.put(TESTKEY, "TESTVALUE");
                RpcResponseFuture future = client.invokeWithFuture(addr, req, invokeContext, 3000);
                String res = (String) future.get();
                Assert.assertEquals(RequestBody.DEFAULT_SERVER_RETURN_STR, res);

                Assert.assertEquals("127.0.0.1", invokeContext.get(InvokeContext.CLIENT_LOCAL_IP));
                Assert.assertEquals("127.0.0.1", invokeContext.get(InvokeContext.CLIENT_REMOTE_IP));
                Assert.assertNotNull(invokeContext.get(InvokeContext.CLIENT_LOCAL_PORT));
                Assert.assertNotNull(invokeContext.get(InvokeContext.CLIENT_REMOTE_PORT));

                Assert.assertNotNull(invokeContext.get(InvokeContext.CLIENT_CONN_CREATETIME));
                logger.warn("CLIENT_CONN_CREATETIME:"
                            + invokeContext.get(InvokeContext.CLIENT_CONN_CREATETIME));

                Assert.assertEquals(invokeContext.get(TESTKEY), "TESTVALUE");
                TraceLogUtil.printConnectionTraceLog(logger, "0af4232214701387943901253",
                    invokeContext);
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
        Assert.assertEquals(invokeTimes, serverUserProcessor.getInvokeTimes());
    }

    @Test
    public void testCallback() throws InterruptedException {
        RequestBody req = new RequestBody(1, "hello world callback");
        final List<String> rets = new ArrayList<String>(1);
        for (int i = 0; i < invokeTimes; i++) {
            final CountDownLatch latch = new CountDownLatch(1);
            InvokeContext invokeContext = new InvokeContext();
            invokeContext.put(TESTKEY, "TESTVALUE");
            try {
                client.invokeWithCallback(addr, req, invokeContext, new InvokeCallback() {
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
            Assert.assertEquals(RequestBody.DEFAULT_SERVER_RETURN_STR, rets.get(0));
            rets.clear();

            Assert.assertEquals("127.0.0.1", invokeContext.get(InvokeContext.CLIENT_LOCAL_IP));
            Assert.assertEquals("127.0.0.1", invokeContext.get(InvokeContext.CLIENT_REMOTE_IP));
            Assert.assertNotNull(invokeContext.get(InvokeContext.CLIENT_LOCAL_PORT));
            Assert.assertNotNull(invokeContext.get(InvokeContext.CLIENT_REMOTE_PORT));

            Assert.assertNotNull(invokeContext.get(InvokeContext.CLIENT_CONN_CREATETIME));
            logger.warn("CLIENT_CONN_CREATETIME:"
                        + invokeContext.get(InvokeContext.CLIENT_CONN_CREATETIME));

            Assert.assertEquals(invokeContext.get(TESTKEY), "TESTVALUE");
            TraceLogUtil
                .printConnectionTraceLog(logger, "0af4232214701387943901253", invokeContext);
        }

        Assert.assertTrue(serverConnectProcessor.isConnected());
        Assert.assertEquals(1, serverConnectProcessor.getConnectTimes());
        Assert.assertEquals(invokeTimes, serverUserProcessor.getInvokeTimes());
    }

    @Test
    public void testServerSyncUsingConnection() throws Exception {
        Connection clientConn = client.createStandaloneConnection(ip, port, 1000);

        for (int i = 0; i < invokeTimes; i++) {
            InvokeContext invokeContext = new InvokeContext();
            invokeContext.put(TESTKEY, "TESTVALUE");
            RequestBody req1 = new RequestBody(1, RequestBody.DEFAULT_CLIENT_STR);
            String serverres = (String) client.invokeSync(clientConn, req1, invokeContext, 1000);
            Assert.assertEquals(serverres, RequestBody.DEFAULT_SERVER_RETURN_STR);

            invokeContext.clear();
            invokeContext.put(TESTKEY, "TESTVALUE");
            Assert.assertNotNull(serverConnectProcessor.getConnection());
            Connection serverConn = serverConnectProcessor.getConnection();
            RequestBody req = new RequestBody(1, RequestBody.DEFAULT_SERVER_STR);
            String clientres = (String) server.getRpcServer().invokeSync(serverConn, req,
                invokeContext, 1000);
            Assert.assertEquals(clientres, RequestBody.DEFAULT_CLIENT_RETURN_STR);

            Assert.assertEquals("127.0.0.1", invokeContext.get(InvokeContext.SERVER_LOCAL_IP));
            Assert.assertEquals("127.0.0.1", invokeContext.get(InvokeContext.SERVER_REMOTE_IP));
            Assert.assertNotNull(invokeContext.get(InvokeContext.SERVER_LOCAL_PORT));
            Assert.assertNotNull(invokeContext.get(InvokeContext.SERVER_REMOTE_PORT));
            Assert.assertEquals(invokeContext.get(TESTKEY), "TESTVALUE");
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
            InvokeContext invokeContext = new InvokeContext();
            invokeContext.put(TESTKEY, "TESTVALUE");
            RequestBody req1 = new RequestBody(1, RequestBody.DEFAULT_CLIENT_STR);
            String serverres = (String) client.invokeSync(clientConn, req1, invokeContext, 1000);
            Assert.assertEquals(serverres, RequestBody.DEFAULT_SERVER_RETURN_STR);

            invokeContext.clear();
            invokeContext.put(TESTKEY, "TESTVALUE");
            Assert.assertNotNull(serverConnectProcessor.getConnection());
            // only when client invoked, the remote address can be get by UserProcessor
            // otherwise, please use ConnectionEventProcessor
            String remoteAddr = serverUserProcessor.getRemoteAddr();
            RequestBody req = new RequestBody(1, RequestBody.DEFAULT_SERVER_STR);
            String clientres = (String) server.getRpcServer().invokeSync(remoteAddr, req,
                invokeContext, 1000);
            Assert.assertEquals(clientres, RequestBody.DEFAULT_CLIENT_RETURN_STR);

            Assert.assertEquals("127.0.0.1", invokeContext.get(InvokeContext.SERVER_LOCAL_IP));
            Assert.assertEquals("127.0.0.1", invokeContext.get(InvokeContext.SERVER_REMOTE_IP));
            Assert.assertNotNull(invokeContext.get(InvokeContext.SERVER_LOCAL_PORT));
            Assert.assertNotNull(invokeContext.get(InvokeContext.SERVER_REMOTE_PORT));
            Assert.assertEquals(invokeContext.get(TESTKEY), "TESTVALUE");
        }

        Assert.assertTrue(serverConnectProcessor.isConnected());
        Assert.assertEquals(1, serverConnectProcessor.getConnectTimes());
        Assert.assertEquals(invokeTimes, serverUserProcessor.getInvokeTimes());
    }
}