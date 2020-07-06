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
import com.alipay.remoting.CustomSerializerManager;
import com.alipay.remoting.InvokeCallback;
import com.alipay.remoting.InvokeContext;
import com.alipay.remoting.config.Configs;
import com.alipay.remoting.exception.DeserializationException;
import com.alipay.remoting.exception.SerializationException;
import com.alipay.remoting.rpc.RpcClient;
import com.alipay.remoting.rpc.RpcResponseFuture;
import com.alipay.remoting.rpc.common.BoltServer;
import com.alipay.remoting.rpc.common.CONNECTEventProcessor;
import com.alipay.remoting.rpc.common.DISCONNECTEventProcessor;
import com.alipay.remoting.rpc.common.PortScan;
import com.alipay.remoting.rpc.common.RequestBody;
import com.alipay.remoting.rpc.common.SimpleClientUserProcessor;
import com.alipay.remoting.rpc.common.SimpleServerUserProcessor;
import com.alipay.remoting.serialization.SerializerManager;

/**
 * Custom Serializer Test: Normal, Exception included
 * 
 * @author xiaomin.cxm
 * @version $Id: ClassCustomSerializerTest.java, v 0.1 Apr 11, 2016 10:42:59 PM xiaomin.cxm Exp $
 */
public class ClassCustomSerializerTest {
    Logger                    logger                    = LoggerFactory
                                                            .getLogger(ClassCustomSerializerTest.class);
    BoltServer                server;
    RpcClient                 client;

    int                       port                      = PortScan.select();
    String                    ip                        = "127.0.0.1";
    String                    addr                      = "127.0.0.1:" + port;

    int                       invokeTimes               = 5;

    SimpleServerUserProcessor serverUserProcessor       = new SimpleServerUserProcessor();
    SimpleClientUserProcessor clientUserProcessor       = new SimpleClientUserProcessor();
    CONNECTEventProcessor     clientConnectProcessor    = new CONNECTEventProcessor();
    CONNECTEventProcessor     serverConnectProcessor    = new CONNECTEventProcessor();
    DISCONNECTEventProcessor  clientDisConnectProcessor = new DISCONNECTEventProcessor();
    DISCONNECTEventProcessor  serverDisConnectProcessor = new DISCONNECTEventProcessor();

    boolean                   flag1_RequestBody         = false;
    boolean                   flag2_RequestBody         = false;
    boolean                   flag1_String              = false;
    boolean                   flag2_String              = false;

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
        CustomSerializerManager.clear();
        server.stop();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            logger.error("Stop server failed!", e);
        }
    }

    /**
     * normal test
     * @throws Exception
     */
    @Test
    public void testNormalCustomSerializer() throws Exception {
        NormalRequestBodyCustomSerializer s1 = new NormalRequestBodyCustomSerializer();
        NormalStringCustomSerializer s2 = new NormalStringCustomSerializer();
        CustomSerializerManager.registerCustomSerializer(RequestBody.class.getName(), s1);
        CustomSerializerManager.registerCustomSerializer(String.class.getName(), s2);

        RequestBody body = new RequestBody(1, "hello world!");
        String ret = (String) client.invokeSync(addr, body, 1000);
        Assert.assertEquals(RequestBody.DEFAULT_SERVER_RETURN_STR + "RANDOM", ret);
        Assert.assertTrue(s1.isSerialized());
        Assert.assertTrue(s1.isDeserialized());
        Assert.assertTrue(s2.isSerialized());
        Assert.assertTrue(s2.isDeserialized());
    }

    /**
     * test SerializationException when serial request
     * @throws Exception
     */
    @Test
    public void testRequestSerialException() throws Exception {
        ExceptionRequestBodyCustomSerializer s1 = new ExceptionRequestBodyCustomSerializer(true,
            false, false, false);
        NormalStringCustomSerializer s2 = new NormalStringCustomSerializer();
        CustomSerializerManager.registerCustomSerializer(RequestBody.class.getName(), s1);
        CustomSerializerManager.registerCustomSerializer(String.class.getName(), s2);

        RequestBody body = new RequestBody(1, "hello world!");
        String ret = null;
        try {
            ret = (String) client.invokeSync(addr, body, 1000);
            Assert.fail("Should not reach here!");
        } catch (SerializationException e) {
            logger.error("", e);
            Assert.assertFalse(e.isServerSide());
            Assert.assertEquals(null, ret);
            Assert.assertTrue(s1.isSerialized());
            Assert.assertFalse(s1.isDeserialized());
            Assert.assertFalse(s2.isSerialized());
            Assert.assertFalse(s2.isDeserialized());
        } catch (Throwable t) {
            Assert.fail("Should not reach here!");
        }
    }

    /**
     * test RuntimeException when serial request
     * @throws Exception
     */
    @Test
    public void testRequestSerialRuntimeException() throws Exception {
        ExceptionRequestBodyCustomSerializer s1 = new ExceptionRequestBodyCustomSerializer(false,
            true, false, false);
        NormalStringCustomSerializer s2 = new NormalStringCustomSerializer();
        CustomSerializerManager.registerCustomSerializer(RequestBody.class.getName(), s1);
        CustomSerializerManager.registerCustomSerializer(String.class.getName(), s2);

        RequestBody body = new RequestBody(1, "hello world!");
        String ret = null;
        try {
            ret = (String) client.invokeSync(addr, body, 1000);
            Assert.fail("Should not reach here!");
        } catch (SerializationException e) {
            logger.error("", e);
            Assert.assertFalse(e.isServerSide());
            Assert.assertEquals(null, ret);
            Assert.assertTrue(s1.isSerialized());
            Assert.assertFalse(s1.isDeserialized());
            Assert.assertFalse(s2.isSerialized());
            Assert.assertFalse(s2.isDeserialized());
        } catch (Throwable t) {
            Assert.fail("Should not reach here!");
        }
    }

    /**
     * test DeserializationException when deserial request
     * @throws Exception
     */
    @Test
    public void testRequestDeserialException() throws Exception {
        System.setProperty(Configs.SERIALIZER, Byte.toString(SerializerManager.Hessian2));
        ExceptionRequestBodyCustomSerializer s1 = new ExceptionRequestBodyCustomSerializer(false,
            false, true, false);
        NormalStringCustomSerializer s2 = new NormalStringCustomSerializer();
        CustomSerializerManager.registerCustomSerializer(RequestBody.class.getName(), s1);
        CustomSerializerManager.registerCustomSerializer(String.class.getName(), s2);

        RequestBody body = new RequestBody(1, "hello world!");
        String ret = null;
        try {
            ret = (String) client.invokeSync(addr, body, 1000);
            Assert.fail("Should not reach here!");
        } catch (DeserializationException e) {
            logger.error("", e);
            Assert.assertTrue(e.isServerSide());
            Assert.assertEquals(null, ret);
            Assert.assertTrue(s1.isSerialized());
            Assert.assertTrue(s1.isDeserialized());
            Assert.assertFalse(s2.isSerialized());
            Assert.assertFalse(s2.isDeserialized());
        } catch (Throwable t) {
            Assert.fail("Should not reach here!");
        }
    }

    /**
     * test RuntimeException when deserial request
     * @throws Exception
     */
    @Test
    public void testRequestDeserialRuntimeException() throws Exception {
        System.setProperty(Configs.SERIALIZER, Byte.toString(SerializerManager.Hessian2));
        ExceptionRequestBodyCustomSerializer s1 = new ExceptionRequestBodyCustomSerializer(false,
            false, false, true);
        NormalStringCustomSerializer s2 = new NormalStringCustomSerializer();
        CustomSerializerManager.registerCustomSerializer(RequestBody.class.getName(), s1);
        CustomSerializerManager.registerCustomSerializer(String.class.getName(), s2);

        RequestBody body = new RequestBody(1, "hello world!");
        String ret = null;
        try {
            ret = (String) client.invokeSync(addr, body, 1000);
            Assert.fail("Should not reach here!");
        } catch (DeserializationException e) {
            logger.error("", e);
            Assert.assertTrue(e.isServerSide());
            Assert.assertEquals(null, ret);
            Assert.assertTrue(s1.isSerialized());
            Assert.assertTrue(s1.isDeserialized());
            Assert.assertFalse(s2.isSerialized());
            Assert.assertFalse(s2.isDeserialized());
        } catch (Throwable t) {
            Assert.fail("Should not reach here!");
        }
    }

    /**
     * test SerializationException when serial response
     * @throws Exception
     */
    @Test
    public void testResponseSerialException() throws Exception {
        NormalRequestBodyCustomSerializer s1 = new NormalRequestBodyCustomSerializer();
        ExceptionStringCustomSerializer s2 = new ExceptionStringCustomSerializer(true, false,
            false, false);
        CustomSerializerManager.registerCustomSerializer(RequestBody.class.getName(), s1);
        CustomSerializerManager.registerCustomSerializer(String.class.getName(), s2);

        RequestBody body = new RequestBody(1, "hello world!");
        String ret = null;
        try {
            ret = (String) client.invokeSync(addr, body, 1000);
            Assert.fail("Should not reach here!");
        } catch (SerializationException e) {
            logger.error("", e);
            Assert.assertTrue(e.isServerSide());
            Assert.assertEquals(null, ret);
            Assert.assertTrue(s1.isSerialized());
            Assert.assertTrue(s1.isDeserialized());
            Assert.assertTrue(s2.isSerialized());
            Assert.assertFalse(s2.isDeserialized());
        } catch (Throwable t) {
            Assert.fail("Should not reach here!");
        }
    }

    /**
     * test RuntimeException when serial response
     * @throws Exception
     */
    @Test
    public void testResponseSerialRuntimeException() throws Exception {
        NormalRequestBodyCustomSerializer s1 = new NormalRequestBodyCustomSerializer();
        ExceptionStringCustomSerializer s2 = new ExceptionStringCustomSerializer(false, true,
            false, false);
        CustomSerializerManager.registerCustomSerializer(RequestBody.class.getName(), s1);
        CustomSerializerManager.registerCustomSerializer(String.class.getName(), s2);

        RequestBody body = new RequestBody(1, "hello world!");
        String ret = null;
        try {
            ret = (String) client.invokeSync(addr, body, 1000);
            Assert.fail("Should not reach here!");
        } catch (SerializationException e) {
            logger.error("", e);
            Assert.assertTrue(e.isServerSide());
            Assert.assertEquals(null, ret);
            Assert.assertTrue(s1.isSerialized());
            Assert.assertTrue(s1.isDeserialized());
            Assert.assertTrue(s2.isSerialized());
            Assert.assertFalse(s2.isDeserialized());
        } catch (Throwable t) {
            Assert.fail("Should not reach here!");
        }
    }

    /**
     * test DeserializationException when deserial response
     * @throws Exception
     */
    @Test
    public void testResponseDeserialzeException() throws Exception {
        NormalRequestBodyCustomSerializer s1 = new NormalRequestBodyCustomSerializer();
        ExceptionStringCustomSerializer s2 = new ExceptionStringCustomSerializer(false, false,
            true, false);
        CustomSerializerManager.registerCustomSerializer(RequestBody.class.getName(), s1);
        CustomSerializerManager.registerCustomSerializer(String.class.getName(), s2);

        RequestBody body = new RequestBody(1, "hello world!");
        String ret = null;
        try {
            ret = (String) client.invokeSync(addr, body, 1000);
            Assert.fail("Should not reach here!");
        } catch (DeserializationException e) {
            logger.error("", e);
            Assert.assertFalse(e.isServerSide());
            Assert.assertEquals(null, ret);
            Assert.assertTrue(s1.isSerialized());
            Assert.assertTrue(s1.isDeserialized());
            Assert.assertTrue(s2.isSerialized());
            Assert.assertTrue(s2.isDeserialized());
        } catch (Throwable t) {
            Assert.fail("Should not reach here!");
        }
    }

    /**
     * test RuntimeException when deserial response
     * @throws Exception
     */
    @Test
    public void testResponseDeserialzeRuntimeException() throws Exception {
        NormalRequestBodyCustomSerializer s1 = new NormalRequestBodyCustomSerializer();
        ExceptionStringCustomSerializer s2 = new ExceptionStringCustomSerializer(false, false,
            false, true);
        CustomSerializerManager.registerCustomSerializer(RequestBody.class.getName(), s1);
        CustomSerializerManager.registerCustomSerializer(String.class.getName(), s2);

        RequestBody body = new RequestBody(1, "hello world!");
        String ret = null;
        try {
            ret = (String) client.invokeSync(addr, body, 1000);
            Assert.fail("Should not reach here!");
        } catch (DeserializationException e) {
            logger.error("", e);
            Assert.assertFalse(e.isServerSide());
            Assert.assertEquals(null, ret);
            Assert.assertTrue(s1.isSerialized());
            Assert.assertTrue(s1.isDeserialized());
            Assert.assertTrue(s2.isSerialized());
            Assert.assertTrue(s2.isDeserialized());
        } catch (Throwable t) {
            Assert.fail("Should not reach here!");
        }
    }

    /**
     * test custom serializer using invoke contxt in sync
     *
     * @throws Exception
     */
    @Test
    public void testInvokeContextCustomSerializer_SYNC() throws Exception {
        NormalRequestBodyCustomSerializer_InvokeContext s1 = new NormalRequestBodyCustomSerializer_InvokeContext();
        NormalStringCustomSerializer_InvokeContext s2 = new NormalStringCustomSerializer_InvokeContext();
        CustomSerializerManager.registerCustomSerializer(RequestBody.class.getName(), s1);
        CustomSerializerManager.registerCustomSerializer(String.class.getName(), s2);

        RequestBody body = new RequestBody(1, "hello world!");
        InvokeContext invokeContext = new InvokeContext();
        invokeContext.putIfAbsent(NormalRequestBodyCustomSerializer_InvokeContext.SERIALTYPE_KEY,
            NormalRequestBodyCustomSerializer_InvokeContext.SERIALTYPE1_value);
        String ret = (String) client.invokeSync(addr, body, invokeContext, 1000);
        Assert.assertEquals(RequestBody.DEFAULT_SERVER_RETURN_STR + "RANDOM", ret);
        Assert.assertTrue(s1.isSerialized());
        Assert.assertTrue(s1.isDeserialized());

        invokeContext.clear();
        invokeContext.putIfAbsent(NormalRequestBodyCustomSerializer_InvokeContext.SERIALTYPE_KEY,
            NormalRequestBodyCustomSerializer_InvokeContext.SERIALTYPE2_value);
        ret = (String) client.invokeSync(addr, body, invokeContext, 1000);
        Assert.assertEquals(NormalStringCustomSerializer_InvokeContext.UNIVERSAL_RESP, ret);
        Assert.assertTrue(s1.isSerialized());
        Assert.assertTrue(s1.isDeserialized());
    }

    /**
     * test custom serializer using invoke contxt in future
     * @throws Exception
     */
    @Test
    public void testInvokeContextCustomSerializer_FUTURE() throws Exception {
        NormalRequestBodyCustomSerializer_InvokeContext s1 = new NormalRequestBodyCustomSerializer_InvokeContext();
        NormalStringCustomSerializer_InvokeContext s2 = new NormalStringCustomSerializer_InvokeContext();
        CustomSerializerManager.registerCustomSerializer(RequestBody.class.getName(), s1);
        CustomSerializerManager.registerCustomSerializer(String.class.getName(), s2);

        RequestBody body = new RequestBody(1, "hello world!");
        InvokeContext invokeContext = new InvokeContext();
        invokeContext.putIfAbsent(NormalRequestBodyCustomSerializer_InvokeContext.SERIALTYPE_KEY,
            NormalRequestBodyCustomSerializer_InvokeContext.SERIALTYPE1_value);
        RpcResponseFuture future = client.invokeWithFuture(addr, body, invokeContext, 1000);
        String ret = (String) future.get(1000);
        Assert.assertEquals(RequestBody.DEFAULT_SERVER_RETURN_STR + "RANDOM", ret);
        Assert.assertTrue(s1.isSerialized());
        Assert.assertTrue(s1.isDeserialized());

        invokeContext.clear();
        invokeContext.putIfAbsent(NormalRequestBodyCustomSerializer_InvokeContext.SERIALTYPE_KEY,
            NormalRequestBodyCustomSerializer_InvokeContext.SERIALTYPE2_value);
        future = client.invokeWithFuture(addr, body, invokeContext, 1000);
        ret = (String) future.get(1000);
        Assert.assertEquals(NormalStringCustomSerializer_InvokeContext.UNIVERSAL_RESP, ret);
        Assert.assertTrue(s1.isSerialized());
        Assert.assertTrue(s1.isDeserialized());
    }

    /**
     * test custom serializer using invoke contxt in callback
     * @throws Exception
     */
    @Test
    public void testInvokeContextCustomSerializer_CALLBACK() throws Exception {
        NormalRequestBodyCustomSerializer_InvokeContext s1 = new NormalRequestBodyCustomSerializer_InvokeContext();
        NormalStringCustomSerializer_InvokeContext s2 = new NormalStringCustomSerializer_InvokeContext();
        CustomSerializerManager.registerCustomSerializer(RequestBody.class.getName(), s1);
        CustomSerializerManager.registerCustomSerializer(String.class.getName(), s2);

        RequestBody body = new RequestBody(1, "hello world!");
        InvokeContext invokeContext = new InvokeContext();
        invokeContext.putIfAbsent(NormalRequestBodyCustomSerializer_InvokeContext.SERIALTYPE_KEY,
            NormalRequestBodyCustomSerializer_InvokeContext.SERIALTYPE1_value);

        final List<Object> rets = new ArrayList<Object>();
        final CountDownLatch latch = new CountDownLatch(1);
        client.invokeWithCallback(addr, body, invokeContext, new InvokeCallback() {
            @Override
            public void onResponse(Object result) {
                rets.clear();
                rets.add(result);
                latch.countDown();
            }

            @Override
            public void onException(Throwable e) {

            }

            @Override
            public Executor getExecutor() {
                return null;
            }
        }, 1000);
        latch.await();
        String ret = (String) rets.get(0);
        Assert.assertEquals(RequestBody.DEFAULT_SERVER_RETURN_STR + "RANDOM", ret);
        Assert.assertTrue(s1.isSerialized());
        Assert.assertTrue(s1.isDeserialized());

        invokeContext.clear();
        invokeContext.putIfAbsent(NormalRequestBodyCustomSerializer_InvokeContext.SERIALTYPE_KEY,
            NormalRequestBodyCustomSerializer_InvokeContext.SERIALTYPE2_value);
        final CountDownLatch latch1 = new CountDownLatch(1);
        client.invokeWithCallback(addr, body, invokeContext, new InvokeCallback() {
            @Override
            public void onResponse(Object result) {
                rets.clear();
                rets.add(result);
                latch1.countDown();
            }

            @Override
            public void onException(Throwable e) {

            }

            @Override
            public Executor getExecutor() {
                return null;
            }
        }, 1000);
        latch1.await();
        ret = (String) rets.get(0);
        Assert.assertEquals(NormalStringCustomSerializer_InvokeContext.UNIVERSAL_RESP, ret);
        Assert.assertTrue(s1.isSerialized());
        Assert.assertTrue(s1.isDeserialized());
    }

}
