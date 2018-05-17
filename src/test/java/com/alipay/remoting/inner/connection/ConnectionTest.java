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
package com.alipay.remoting.inner.connection;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alipay.remoting.Connection;
import com.alipay.remoting.ConnectionEventType;
import com.alipay.remoting.rpc.RpcClient;
import com.alipay.remoting.rpc.common.BoltServer;
import com.alipay.remoting.rpc.common.CONNECTEventProcessor;
import com.alipay.remoting.rpc.common.DISCONNECTEventProcessor;
import com.alipay.remoting.rpc.common.PortScan;
import com.alipay.remoting.rpc.common.SimpleClientUserProcessor;
import com.alipay.remoting.rpc.common.SimpleServerUserProcessor;

/**
 * Connection test
 * 
 * @author jiangping
 * @version $Id: ConnectionTest.java, v 0.1 Mar 6, 2016 9:55:50 PM tao Exp $
 */
public class ConnectionTest {

    static Logger             logger                    = LoggerFactory
                                                            .getLogger(ConnectionTest.class);

    BoltServer                server;
    RpcClient                 client;

    int                       port                      = PortScan.select();
    String                    ip                        = "127.0.0.1";
    String                    addr                      = "127.0.0.1:" + port;

    SimpleServerUserProcessor serverUserProcessor       = new SimpleServerUserProcessor();
    SimpleClientUserProcessor clientUserProcessor       = new SimpleClientUserProcessor();
    CONNECTEventProcessor     clientConnectProcessor    = new CONNECTEventProcessor();
    CONNECTEventProcessor     serverConnectProcessor    = new CONNECTEventProcessor();
    DISCONNECTEventProcessor  clientDisConnectProcessor = new DISCONNECTEventProcessor();
    DISCONNECTEventProcessor  serverDisConnectProcessor = new DISCONNECTEventProcessor();

    @Before
    public void init() throws Exception {
        server = new BoltServer(port);
        server.start();
        server.addConnectionEventProcessor(ConnectionEventType.CONNECT, serverConnectProcessor);
        server.addConnectionEventProcessor(ConnectionEventType.CLOSE, serverDisConnectProcessor);
        server.registerUserProcessor(serverUserProcessor);// no use here

        client = new RpcClient();
        client.addConnectionEventProcessor(ConnectionEventType.CONNECT, clientConnectProcessor);
        client.addConnectionEventProcessor(ConnectionEventType.CLOSE, clientDisConnectProcessor);
        client.registerUserProcessor(clientUserProcessor);// no use here
        client.init();
        Thread.sleep(100);
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
    public void connectionTest() throws Exception {
        Connection conn = client.createStandaloneConnection(ip, port, 1000);
        Thread.sleep(100);
        Assert.assertTrue(conn.isFine());
        Assert.assertTrue(serverConnectProcessor.isConnected());
        Assert.assertEquals(1, clientConnectProcessor.getConnectTimes());
        Assert.assertEquals(1, serverConnectProcessor.getConnectTimes());
        client.closeStandaloneConnection(conn);
        Thread.sleep(100);
        Assert.assertTrue(!conn.isFine());
        Assert.assertTrue(serverDisConnectProcessor.isDisConnected());
        Assert.assertEquals(1, clientDisConnectProcessor.getDisConnectTimes());
        Assert.assertEquals(1, serverDisConnectProcessor.getDisConnectTimes());

        conn = client.createStandaloneConnection(ip, port, 1000);
        Thread.sleep(100);
        Assert.assertTrue(conn.isFine());
        Assert.assertEquals(2, clientConnectProcessor.getConnectTimes());
        Assert.assertEquals(2, serverConnectProcessor.getConnectTimes());
        client.closeStandaloneConnection(conn);
        Thread.sleep(100);
        Assert.assertTrue(!conn.isFine());
        Assert.assertEquals(2, clientDisConnectProcessor.getDisConnectTimes());
        Assert.assertEquals(2, serverDisConnectProcessor.getDisConnectTimes());

        conn = client.createStandaloneConnection(ip, port, 1000);
        Thread.sleep(100);
        Assert.assertTrue(conn.isFine());
        Assert.assertEquals(3, clientConnectProcessor.getConnectTimes());
        Assert.assertEquals(3, serverConnectProcessor.getConnectTimes());
        client.closeStandaloneConnection(conn);
        Thread.sleep(100);
        Assert.assertTrue(!conn.isFine());
        Assert.assertEquals(3, clientDisConnectProcessor.getDisConnectTimes());
        Assert.assertEquals(3, serverDisConnectProcessor.getDisConnectTimes());
        Thread.sleep(100);
        Assert.assertEquals(3, serverConnectProcessor.getConnectTimes());
    }
}
