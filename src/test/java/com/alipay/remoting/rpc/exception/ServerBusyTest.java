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

/**
 * 
 * @author jiangping
 * @version $Id: ServerBusyTest.java, v 0.1 2015-10-20 PM2:48:57 tao Exp $
 */
public class ServerBusyTest {
    static Logger             logger                    = LoggerFactory
                                                            .getLogger(BasicUsageTest.class);

    BoltServer                server;
    RpcClient                 client;

    int                       port                      = PortScan.select();
    String                    ip                        = "127.0.0.1";
    String                    addr                      = "127.0.0.1:" + port;

    int                       invokeTimes               = 5;
    int                       timeout                   = 15000;

    int                       coreThread                = 1;
    int                       maxThread                 = 3;
    int                       workQueue                 = 4;
    int                       concurrent                = maxThread + workQueue;

    SimpleServerUserProcessor serverUserProcessor       = new SimpleServerUserProcessor(timeout,
                                                            coreThread, maxThread, 60, workQueue);
    SimpleClientUserProcessor clientUserProcessor       = new SimpleClientUserProcessor();
    CONNECTEventProcessor     clientConnectProcessor    = new CONNECTEventProcessor();
    CONNECTEventProcessor     serverConnectProcessor    = new CONNECTEventProcessor();
    DISCONNECTEventProcessor  clientDisConnectProcessor = new DISCONNECTEventProcessor();
    DISCONNECTEventProcessor  serverDisConnectProcessor = new DISCONNECTEventProcessor();

    @Before
    public void init() throws InterruptedException {
        concurrent = maxThread + workQueue;
        //        System.setProperty(RpcConfigs.TP_MIN_SIZE, "1");
        //        System.setProperty(RpcConfigs.TP_QUEUE_SIZE, "4");
        //        System.setProperty(RpcConfigs.TP_MAX_SIZE, "3");

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

        for (int i = 0; i < concurrent; i++) {
            final RequestBody bd = new RequestBody(i + 1, "Hello world!");
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    Object obj = null;
                    try {
                        logger.info("client fire! =========" + bd.getId());
                        obj = client.invokeSync(addr, bd, 200);
                    } catch (InvokeTimeoutException e) {
                        Assert.assertNull(obj);
                    } catch (RemotingException e) {
                        logger.error(
                            "Other RemotingException but InvokeTimeoutException occurred in sync",
                            e);
                        Assert.fail("Should not reach here!");
                    } catch (InterruptedException e) {
                        logger.error("InterruptedException in sync", e);
                        Assert.fail("Should not reach here!");
                    }
                }

            });
            t.start();
            Thread.sleep(100);
        }
        Thread.sleep(100);
    }

    @After
    public void stop() {
        server.stop();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            logger.error("Stop server failed!", e);
        }
    }

    @Test
    public void testSync() throws InterruptedException {
        Object obj = null;
        try {
            final RequestBody bd = new RequestBody(8, "Hello world!");
            logger.info("client last sync invoke! =========" + bd.getId());
            obj = client.invokeSync(addr, bd, 3000);
            Assert.fail("Should not reach here!");
        } catch (InvokeServerBusyException e) {
            Assert.assertNull(obj);
        } catch (RemotingException e) {
            logger.error("Other RemotingException but InvokeServerBusyException occurred in sync",
                e);
            Assert.fail("Should not reach here!");
        } catch (InterruptedException e) {
            logger.error("InterruptedException in sync", e);
            Assert.fail("Should not reach here!");
        }
    }

    @Test
    public void testFuture() {
        RequestBody b4 = new RequestBody(4, "Hello world!");
        Object obj = null;
        try {
            RpcResponseFuture future = client.invokeWithFuture(addr, b4, 1000);
            obj = future.get(1500);
            Assert.fail("Should not reach here!");
        } catch (InvokeServerBusyException e) {
            Assert.assertNull(obj);
        } catch (RemotingException e) {
            logger.error(
                "Other RemotingException but InvokeServerBusyException occurred in future", e);
            Assert.fail("Should not reach here!");
        } catch (InterruptedException e) {
            logger.error("InterruptedException in future", e);
            Assert.fail("Should not reach here!");
        }

    }

    @Test
    public void callback() throws InterruptedException {
        RequestBody b3 = new RequestBody(3, "Hello world!");
        final CountDownLatch latch = new CountDownLatch(1);
        final List<Throwable> ret = new ArrayList<Throwable>(1);
        try {
            client.invokeWithCallback(addr, b3, new InvokeCallback() {

                @Override
                public void onResponse(Object result) {
                    Assert.fail("Should not reach here!");
                }

                @Override
                public void onException(Throwable e) {
                    ret.add(e);
                    latch.countDown();
                }

                @Override
                public Executor getExecutor() {
                    return null;
                }

            }, 1000);

        } catch (RemotingException e) {
            logger.error(
                "Other RemotingException but InvokeServerBusyException occurred in callback", e);
            Assert.fail("Should not reach here!");
        }
        latch.await();
        Assert.assertEquals(InvokeServerBusyException.class, ret.get(0).getClass());
    }
}
