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

import com.alipay.remoting.config.Configs;
import com.alipay.remoting.exception.RemotingException;
import com.alipay.remoting.rpc.RpcClient;
import com.alipay.remoting.rpc.common.BoltServer;
import com.alipay.remoting.rpc.common.PortScan;

/**
 * water mark init test
 * 
 * @author xiaomin.cxm
 * @version $Id: WaterMarkTest.java, v 0.1 Apr 6, 2016 8:58:36 PM xiaomin.cxm Exp $
 */
public class WaterMarkInitTest {
    BoltServer server;
    RpcClient  client;

    int        port = PortScan.select();
    String     addr = "127.0.0.1:" + port;

    @Before
    public void init() {

    }

    @After
    public void stop() {
    }

    @Test
    public void testLowBiggerThanHigh() {
        System.setProperty(Configs.NETTY_BUFFER_HIGH_WATERMARK, Integer.toString(1));
        System.setProperty(Configs.NETTY_BUFFER_LOW_WATERMARK, Integer.toString(2));
        try {
            server = new BoltServer(port, true);
            server.start();
            Assert.fail("should not reach here");
        } catch (IllegalStateException e) {
            // expect IllegalStateException
        }

        try {
            client = new RpcClient();
            client.init();
            Assert.fail("should not reach here");
        } catch (IllegalArgumentException e) {
            // expect IllegalStateException
        }
    }

    @Test
    public void testLowBiggerThanDefaultHigh() throws InterruptedException {
        System.setProperty(Configs.NETTY_BUFFER_HIGH_WATERMARK, Integer.toString(300 * 1024));
        System.setProperty(Configs.NETTY_BUFFER_LOW_WATERMARK, Integer.toString(200 * 1024));
        server = new BoltServer(port, true);
        Assert.assertTrue(server.start());

        try {
            client = new RpcClient();
            client.init();
            client.getConnection(addr, 3000);
        } catch (IllegalArgumentException e) {
            Assert.fail("should not reach here");
        } catch (RemotingException e) {
            // not connected, but ok, here only test args init
        }
    }

    @Test
    public void testHighSmallerThanDefaultLow() throws InterruptedException {
        System.setProperty(Configs.NETTY_BUFFER_HIGH_WATERMARK, Integer.toString(3 * 1024));
        System.setProperty(Configs.NETTY_BUFFER_LOW_WATERMARK, Integer.toString(2 * 1024));
        server = new BoltServer(port, true);
        Assert.assertTrue(server.start());

        try {
            client = new RpcClient();
            client.init();
            client.getConnection(addr, 3000);
        } catch (IllegalArgumentException e) {
            Assert.fail("should not reach here");
        } catch (RemotingException e) {
            // not connected, but ok, here only test args init
        }
    }
}