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
package com.alipay.remoting.rpc.watermark;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alipay.remoting.Connection;
import com.alipay.remoting.ConnectionEventType;
import com.alipay.remoting.config.Configs;
import com.alipay.remoting.exception.RemotingException;
import com.alipay.remoting.rpc.RpcClient;
import com.alipay.remoting.rpc.common.BoltServer;
import com.alipay.remoting.rpc.common.CONNECTEventProcessor;
import com.alipay.remoting.rpc.common.DISCONNECTEventProcessor;
import com.alipay.remoting.rpc.common.PortScan;
import com.alipay.remoting.rpc.common.RequestBody;
import com.alipay.remoting.rpc.common.SimpleClientUserProcessor;
import com.alipay.remoting.rpc.common.SimpleServerUserProcessor;

/**
 * water mark normal test, set a large enough buffer mark, and not trigger write over flow.
 * 
 * @author xiaomin.cxm
 * @version $Id: WaterMarkTest.java, v 0.1 Apr 6, 2016 8:58:36 PM xiaomin.cxm Exp $
 */
public class WaterMarkTest {
    static Logger             logger                    = LoggerFactory
                                                            .getLogger(WaterMarkTest.class);

    BoltServer                server;
    RpcClient                 client;

    int                       port                      = PortScan.select();
    String                    ip                        = "127.0.0.1";
    String                    addr                      = "127.0.0.1:" + port;

    int                       invokeTimes               = 5;

    SimpleServerUserProcessor serverUserProcessor       = new SimpleServerUserProcessor(0, 20, 20,
                                                            60, 100);
    SimpleClientUserProcessor clientUserProcessor       = new SimpleClientUserProcessor();
    CONNECTEventProcessor     clientConnectProcessor    = new CONNECTEventProcessor();
    CONNECTEventProcessor     serverConnectProcessor    = new CONNECTEventProcessor();
    DISCONNECTEventProcessor  clientDisConnectProcessor = new DISCONNECTEventProcessor();
    DISCONNECTEventProcessor  serverDisConnectProcessor = new DISCONNECTEventProcessor();

    @Before
    public void init() {
        System.setProperty(Configs.NETTY_BUFFER_HIGH_WATERMARK, Integer.toString(128 * 1024));
        System.setProperty(Configs.NETTY_BUFFER_LOW_WATERMARK, Integer.toString(32 * 1024));

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
    public void testSync() throws InterruptedException {
        final RequestBody req = new RequestBody(1, 1024);
        for (int i = 0; i < invokeTimes; i++) {
            new Thread() {
                @Override
                public void run() {
                    String res = null;
                    try {
                        for (int i = 0; i < invokeTimes; i++) {
                            res = (String) client.invokeSync(addr, req, 3000);
                        }
                    } catch (RemotingException e) {
                        String errMsg = "RemotingException caught in sync!";
                        logger.error(errMsg, e);
                        Assert.fail(errMsg);
                    } catch (InterruptedException e) {
                        String errMsg = "InterruptedException caught in sync!";
                        logger.error(errMsg, e);
                        Assert.fail(errMsg);
                    }
                    logger.warn("Result received in sync: " + res);
                    Assert.assertEquals(RequestBody.DEFAULT_SERVER_RETURN_STR, res);
                }
            }.start();
        }

        Thread.sleep(5000);

        Assert.assertTrue(serverConnectProcessor.isConnected());
        Assert.assertEquals(1, serverConnectProcessor.getConnectTimes());
        Assert.assertEquals(invokeTimes * invokeTimes, serverUserProcessor.getInvokeTimes());
    }

    @Test
    public void testServerSyncUsingConnection() throws Exception {
        Connection clientConn = client.createStandaloneConnection(ip, port, 1000);

        RequestBody req1 = new RequestBody(1, RequestBody.DEFAULT_CLIENT_STR);
        String serverres = (String) client.invokeSync(clientConn, req1, 1000);
        Assert.assertEquals(serverres, RequestBody.DEFAULT_SERVER_RETURN_STR);
        for (int i = 0; i < invokeTimes; i++) {
            new Thread() {
                public void run() {
                    try {
                        String remoteAddr = serverUserProcessor.getRemoteAddr();
                        Assert.assertNotNull(remoteAddr);
                        RequestBody req = new RequestBody(1, 1024);
                        for (int i = 0; i < invokeTimes; i++) {
                            String clientres = (String) server.getRpcServer().invokeSync(
                                remoteAddr, req, 1000);
                            Assert.assertEquals(clientres, RequestBody.DEFAULT_CLIENT_RETURN_STR);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (RemotingException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }
        Thread.sleep(5000);
        Assert.assertTrue(serverConnectProcessor.isConnected());
        Assert.assertEquals(1, serverConnectProcessor.getConnectTimes());
        Assert.assertEquals(invokeTimes * invokeTimes, clientUserProcessor.getInvokeTimes());
    }
}