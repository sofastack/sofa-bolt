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
package com.alipay.remoting.rpc.heartbeat;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alipay.remoting.CommonCommandCode;
import com.alipay.remoting.ConnectionEventType;
import com.alipay.remoting.config.Configs;
import com.alipay.remoting.exception.RemotingException;
import com.alipay.remoting.rpc.RpcClient;
import com.alipay.remoting.rpc.common.BoltServer;
import com.alipay.remoting.rpc.common.CONNECTEventProcessor;
import com.alipay.remoting.rpc.common.DISCONNECTEventProcessor;
import com.alipay.remoting.rpc.common.PortScan;
import com.alipay.remoting.rpc.common.SimpleClientUserProcessor;
import com.alipay.remoting.rpc.common.SimpleServerUserProcessor;
import com.alipay.remoting.rpc.protocol.RpcProtocol;

/**
 * Runtime operation connection heart beat test
 * 
 * @author xiaomin.cxm
 * @version $Id: ClientHeartBeatTest.java, v 0.1 Apr 12, 2016 11:13:10 AM xiaomin.cxm Exp $
 */
public class RuntimeClientHeartBeatTest {
    static Logger             logger                    = LoggerFactory
                                                            .getLogger(RuntimeClientHeartBeatTest.class);

    BoltServer                server;
    RpcClient                 client;
    int                       port;
    String                    addr;

    SimpleServerUserProcessor serverUserProcessor       = new SimpleServerUserProcessor();
    SimpleClientUserProcessor clientUserProcessor       = new SimpleClientUserProcessor();
    CONNECTEventProcessor     clientConnectProcessor    = new CONNECTEventProcessor();
    CONNECTEventProcessor     serverConnectProcessor    = new CONNECTEventProcessor();
    DISCONNECTEventProcessor  clientDisConnectProcessor = new DISCONNECTEventProcessor();
    DISCONNECTEventProcessor  serverDisConnectProcessor = new DISCONNECTEventProcessor();

    CustomHeartBeatProcessor  heartBeatProcessor        = new CustomHeartBeatProcessor();

    @Before
    public void init() {
        port = PortScan.select();
        addr = "127.0.0.1:" + port;

        System.setProperty(Configs.TCP_IDLE, "100");
        System.setProperty(Configs.TCP_IDLE_SWITCH, Boolean.toString(true));
        System.setProperty(Configs.TCP_IDLE_MAXTIMES, "1000");
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
    public void testRuntimeCloseAndEnableHeartbeat() throws InterruptedException {
        server.getRpcServer().registerProcessor(RpcProtocol.PROTOCOL_CODE,
            CommonCommandCode.HEARTBEAT, heartBeatProcessor);
        try {
            client.getConnection(addr, 1000);
        } catch (RemotingException e) {
            logger.error("", e);
        }
        Thread.sleep(1500);
        logger.warn("before disable: " + heartBeatProcessor.getHeartBeatTimes());
        Assert.assertTrue(heartBeatProcessor.getHeartBeatTimes() > 0);

        client.disableConnHeartbeat(addr);
        heartBeatProcessor.reset();
        Thread.sleep(1500);
        logger.warn("after disable: " + heartBeatProcessor.getHeartBeatTimes());
        Assert.assertEquals(0, heartBeatProcessor.getHeartBeatTimes());

        client.enableConnHeartbeat(addr);
        heartBeatProcessor.reset();
        Thread.sleep(1500);
        logger.warn("after enable: " + heartBeatProcessor.getHeartBeatTimes());
        Assert.assertTrue(heartBeatProcessor.getHeartBeatTimes() > 0);
    }
}
