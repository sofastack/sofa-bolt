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
package com.alipay.remoting.rpc.serializer;

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
import com.alipay.remoting.config.Configs;
import com.alipay.remoting.exception.CodecException;
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

/**
 * Codec Test
 * 
 * Test three type serializer [HESSIAN, JAVA, JSON]
 * 
 * note: json need the class have a default constructor
 * 
 * @author jiangping
 * @version $Id: CodecTest.java, v 0.1 2015-9-7 PM5:00:26 tao Exp $
 */
public class CodecTest {
    Logger                    logger                    = LoggerFactory.getLogger(CodecTest.class);

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
    public void init() {
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
    public void testFuture() {
        RequestBody b4 = new RequestBody(4, "hello world future");

        for (int i = 0; i < 3; i++) {
            try {
                System.setProperty(Configs.SERIALIZER, String.valueOf(i));
                RpcResponseFuture future = client.invokeWithFuture(addr, b4, 1000);
                Object obj = future.get(1500);
                Assert.assertEquals(RequestBody.DEFAULT_SERVER_RETURN_STR, obj);
                logger.warn("Result received in future:" + obj);
            } catch (CodecException e) {
                String errMsg = "Codec Exception caught in callback!";
                logger.error(errMsg, e);
                Assert.fail(errMsg);
            } catch (RemotingException e) {
                String errMsg = "Remoting Exception caught in callback!";
                logger.error(errMsg, e);
                Assert.fail(errMsg);
            } catch (InterruptedException e) {
                String errMsg = "Interrupted Exception caught in callback!";
                logger.error(errMsg, e);
                Assert.fail(errMsg);
            }
        }
    }

    @Test
    public void testCallback() throws InterruptedException {
        RequestBody b3 = new RequestBody(3, "hello world callback");
        final List<String> rets = new ArrayList<String>(1);
        for (int i = 0; i < 3; i++) {
            final CountDownLatch latch = new CountDownLatch(1);
            try {
                System.setProperty(Configs.SERIALIZER, String.valueOf(i));
                client.invokeWithCallback(addr, b3, new InvokeCallback() {

                    @Override
                    public void onResponse(Object result) {
                        logger.warn("Result received in callback: " + result);
                        rets.add((String) result);
                        latch.countDown();
                    }

                    @Override
                    public void onException(Throwable e) {
                        logger.error("Process exception in callback.", e);
                    }

                    @Override
                    public Executor getExecutor() {
                        return null;
                    }

                }, 1000);

            } catch (CodecException e) {
                String errMsg = "Codec Exception caught in callback!";
                logger.error(errMsg, e);
                Assert.fail(errMsg);
            } catch (RemotingException e) {
                String errMsg = "Remoting Exception caught in callback!";
                logger.error(errMsg, e);
                Assert.fail(errMsg);
            } catch (InterruptedException e) {
                String errMsg = "Interrupted Exception caught in callback!";
                logger.error(errMsg, e);
                Assert.fail(errMsg);
            }
            latch.await();
            Assert.assertEquals(RequestBody.DEFAULT_SERVER_RETURN_STR, rets.get(0));
            rets.clear();
        }
    }

    @Test
    public void testOneway() {
        RequestBody b2 = new RequestBody(2, "hello world oneway");

        for (int i = 0; i < 3; i++) {
            try {
                System.setProperty(Configs.SERIALIZER, String.valueOf(i));
                client.oneway(addr, b2);
            } catch (CodecException e) {
                String errMsg = "Codec Exception caught in callback!";
                logger.error(errMsg, e);
                Assert.fail(errMsg);
            } catch (RemotingException e) {
                String errMsg = "Remoting Exception caught in callback!";
                logger.error(errMsg, e);
                Assert.fail(errMsg);
            } catch (InterruptedException e) {
                String errMsg = "Interrupted Exception caught in callback!";
                logger.error(errMsg, e);
                Assert.fail(errMsg);
            }
        }
    }

    @Test
    public void testSync() {
        RequestBody b1 = new RequestBody(1, "hello world sync");
        for (int i = 0; i < 3; i++) {
            try {
                System.setProperty(Configs.SERIALIZER, String.valueOf(i));
                String ret = (String) client.invokeSync(addr, b1, 3000);
                logger.warn("Result received in sync: " + ret);
                Assert.assertEquals(RequestBody.DEFAULT_SERVER_RETURN_STR, ret);
            } catch (CodecException e) {
                String errMsg = "Codec Exception caught in callback!";
                logger.error(errMsg, e);
                Assert.fail(errMsg);
            } catch (RemotingException e) {
                String errMsg = "Remoting Exception caught in callback!";
                logger.error(errMsg, e);
                Assert.fail(errMsg);
            } catch (InterruptedException e) {
                String errMsg = "Interrupted Exception caught in callback!";
                logger.error(errMsg, e);
                Assert.fail(errMsg);
            }
        }
    }
}
