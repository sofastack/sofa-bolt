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

import com.alipay.remoting.BizContext;
import com.alipay.remoting.Connection;
import com.alipay.remoting.ConnectionEventListener;
import com.alipay.remoting.DefaultClientConnectionManager;
import com.alipay.remoting.RandomSelectStrategy;
import com.alipay.remoting.Url;
import com.alipay.remoting.exception.RemotingException;
import com.alipay.remoting.rpc.RpcClient;
import com.alipay.remoting.rpc.RpcConnectionEventHandler;
import com.alipay.remoting.rpc.RpcConnectionFactory;
import com.alipay.remoting.rpc.common.BoltServer;
import com.alipay.remoting.rpc.protocol.SyncUserProcessor;
import com.alipay.remoting.rpc.protocol.UserProcessor;
import com.alipay.remoting.util.ThreadTestUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ConcurrentHashMap;

public class ConnectionCreatingTest {

    @Test
    public void testCreateConnection() throws RemotingException, InterruptedException {
        // 启动一个MOCK的Server
        BoltServer boltServer = new BoltServer(8888);
        boltServer.registerUserProcessor(new SyncUserProcessor<String>() {
            @Override
            public Object handleRequest(BizContext bizCtx, String request) {
                return "Hello";
            }

            @Override
            public String interest() {
                return "java.lang.String";
            }
        });
        // 初始Server建连成功，验证创建连接正常
        boltServer.start();
        ThreadTestUtils.sleep(1000);

        RpcClient rpcClient = new RpcClient();
        rpcClient.startup();

        long start = System.currentTimeMillis();
        Object response = rpcClient.invokeSync("127.0.0.1:8888", "TEST", 3000);
        Assert.assertEquals("Hello", response);
        long cost = System.currentTimeMillis() - start;
        Assert.assertTrue(cost < 1000);
        rpcClient.shutdown();
        // 关闭Server
        boltServer.stop();
    }

    @Test
    public void testCreateConnectionWithTimeout() {
        // 启动一个MOCK的Server
        BoltServer boltServer = new BoltServer(8888);
        boltServer.registerUserProcessor(new SyncUserProcessor<String>() {
            @Override
            public Object handleRequest(BizContext bizCtx, String request) throws Exception {
                return "Hello";
            }

            @Override
            public String interest() {
                return "java.lang.String";
            }
        });
        // 初始Server建连成功，验证创建连接正常
        boltServer.start();
        ThreadTestUtils.sleep(1000);

        RpcClient rpcClient = new RpcClient();
        MockConnectionManager connectionManager = new MockConnectionManager(rpcClient);
        connectionManager.setSleepTime(500);
        connectionManager.startup();
        rpcClient.setConnectionManager(connectionManager);
        rpcClient.startup();
        long start = System.currentTimeMillis();
        try {
            Object object = rpcClient.invokeSync("127.0.0.1:8888", "TEST", 499);
            System.out.println(object);
        } catch (Exception e) {
            // ignore
            // create connection timeout
            e.printStackTrace();
        }
        long cost = System.currentTimeMillis() - start;
        Assert.assertTrue(cost >= 500 && cost < 800);

        rpcClient.shutdown();
        // 关闭Server
        boltServer.stop();
    }

    class MockConnectionManager extends DefaultClientConnectionManager {

        private volatile long sleepTime = 0L;

        public void setSleepTime(long sleepTime) {
            this.sleepTime = sleepTime;
        }

        public MockConnectionManager(RpcClient rpcClient) {
            super(new RandomSelectStrategy(null), new RpcConnectionFactory(
                new ConcurrentHashMap<String, UserProcessor<?>>(), rpcClient),
                new RpcConnectionEventHandler(), new ConnectionEventListener());
        }

        @Override
        public Connection getAndCreateIfAbsent(Url url) throws InterruptedException,
                                                       RemotingException {
            Connection connection = super.getAndCreateIfAbsent(url);
            if (sleepTime > 0L) {
                ThreadTestUtils.sleep(sleepTime);
            }
            return connection;
        }
    }
}
