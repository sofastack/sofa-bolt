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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alipay.remoting.Connection;
import com.alipay.remoting.ConnectionEventType;
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
 * Test rpc server invoke exception
 * 
 * @author xiaomin.cxm
 * @version $Id: ServerInvokeExceptionTest.java, v 0.1 Apr 14, 2016 10:00:48 AM xiaomin.cxm Exp $
 */
public class ServerInvokeExceptionTest {
    static Logger             logger                    = LoggerFactory
                                                            .getLogger(ServerInvokeExceptionTest.class);

    BoltServer                server;
    RpcClient                 client;

    int                       port                      = PortScan.select();
    String                    ip                        = "127.0.0.1";
    String                    addr                      = "127.0.0.1:" + port;

    int                       invokeTimes               = 1;

    SimpleServerUserProcessor serverUserProcessor       = new SimpleServerUserProcessor();
    SimpleClientUserProcessor clientUserProcessor       = new SimpleClientUserProcessor();
    CONNECTEventProcessor     clientConnectProcessor    = new CONNECTEventProcessor();
    CONNECTEventProcessor     serverConnectProcessor    = new CONNECTEventProcessor();
    DISCONNECTEventProcessor  clientDisConnectProcessor = new DISCONNECTEventProcessor();
    DISCONNECTEventProcessor  serverDisConnectProcessor = new DISCONNECTEventProcessor();

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
    public void testConnClose() throws InterruptedException, RemotingException {
        client.getConnection(addr, 1000);

        RequestBody req = new RequestBody(1, RequestBody.DEFAULT_SERVER_STR);
        for (int i = 0; i < invokeTimes; i++) {
            try {
                // only when client invoked, the remote address can be get by UserProcessor
                // otherwise, please use ConnectionEventProcessor
                String remoteAddr = serverUserProcessor.getRemoteAddr();
                Assert.assertNull(remoteAddr);
                remoteAddr = serverConnectProcessor.getRemoteAddr();
                Assert.assertNotNull(remoteAddr);
                Connection serverConn = serverConnectProcessor.getConnection();
                String clientres = (String) server.getRpcServer().invokeSync(remoteAddr, req, 1000);
                Assert.assertEquals(clientres, RequestBody.DEFAULT_CLIENT_RETURN_STR);
                Assert.assertTrue(server.getRpcServer().isConnected(remoteAddr));
                serverConn.close();
                Thread.sleep(100);
                Assert.assertFalse(server.getRpcServer().isConnected(remoteAddr));
                clientres = (String) server.getRpcServer().invokeSync(remoteAddr, req, 1000);
                Assert.fail("Connection removed! Should throw exception here.");
            } catch (RemotingException e) {
                logger.error(e.getMessage());
                Assert.assertTrue(e.getMessage().contains("not connected yet!"));
            }
        }

        Assert.assertTrue(serverConnectProcessor.isConnected());
        Assert.assertEquals(1, serverConnectProcessor.getConnectTimes());
        Assert.assertEquals(0, serverUserProcessor.getInvokeTimes());
        Assert.assertEquals(invokeTimes, clientUserProcessor.getInvokeTimes());
    }
}