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
package com.alipay.remoting.rpc.lock;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alipay.remoting.ConnectionEventType;
import com.alipay.remoting.InvokeContext;
import com.alipay.remoting.Url;
import com.alipay.remoting.exception.RemotingException;
import com.alipay.remoting.rpc.RpcAddressParser;
import com.alipay.remoting.rpc.RpcClient;
import com.alipay.remoting.rpc.common.BoltServer;
import com.alipay.remoting.rpc.common.CONNECTEventProcessor;
import com.alipay.remoting.rpc.common.ConcurrentServerUserProcessor;
import com.alipay.remoting.rpc.common.DISCONNECTEventProcessor;
import com.alipay.remoting.rpc.common.RequestBody;
import com.alipay.remoting.rpc.common.SimpleClientUserProcessor;

/** 
 * alipay-com/bolt#110
 * 
 * @author tsui 
 * @version $Id: CreateConnLockTest.java, v 0.1 2016-09-26 11:49 tsui Exp $
 */
public class CreateConnLockTest {

    static Logger                 logger                               = LoggerFactory
                                                                           .getLogger(CreateConnLockTest.class);

    BoltServer                    server;
    RpcClient                     client;

    int                           port                                 = 12200;                                 //PortScan.select();
    String                        ip                                   = "127.0.0.1";
    String                        bad_ip                               = "127.0.0.2";
    String                        ip_prefix                            = "127.0.0.";
    String                        addr                                 = "127.0.0.1:" + port;

    int                           invokeTimes                          = 3;

    ConcurrentServerUserProcessor serverUserProcessor                  = new ConcurrentServerUserProcessor();
    SimpleClientUserProcessor     clientUserProcessor                  = new SimpleClientUserProcessor();
    CONNECTEventProcessor         clientConnectProcessor               = new CONNECTEventProcessor();
    CONNECTEventProcessor         serverConnectProcessor               = new CONNECTEventProcessor();
    DISCONNECTEventProcessor      clientDisConnectProcessor            = new DISCONNECTEventProcessor();
    DISCONNECTEventProcessor      serverDisConnectProcessor            = new DISCONNECTEventProcessor();

    private AtomicBoolean         whetherConnectTimeoutConsumedTooLong = new AtomicBoolean();

    @Before
    public void init() {
        whetherConnectTimeoutConsumedTooLong.set(false);
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
    public void testSync_DiffAddressOnePort() throws InterruptedException {
        for (int i = 0; i < invokeTimes; ++i) {
            Url url = new Url(ip_prefix + i, port);//127.0.0.1:12200, 127.0.0.2:12200, 127.0.0.3:12200...
            MyThread thread = new MyThread(url, 1, false);
            new Thread(thread).start();
        }

        Thread.sleep(5000);
        Assert.assertFalse(whetherConnectTimeoutConsumedTooLong.get());
    }

    @Test
    public void testSync_OneAddressDiffPort() throws InterruptedException {
        for (int i = 0; i < invokeTimes; ++i) {
            Url url = new Url(ip, port++);//127.0.0.1:12200, 127.0.0.2:12201, 127.0.0.3:12202...
            MyThread thread = new MyThread(url, 1, false);
            new Thread(thread).start();
        }

        Thread.sleep(5000);
        Assert.assertFalse(whetherConnectTimeoutConsumedTooLong.get());
    }

    /**
     * enable this case only when non-lock feature of ConnectionManager implemented
     */
    @Test
    public void testSync_OneAddressOnePort() throws InterruptedException {
        for (int i = 0; i < invokeTimes; ++i) {
            Url url = new Url(bad_ip, port);//127.0.0.2:12200
            MyThread thread = new MyThread(url, 1, false);
            new Thread(thread).start();
        }

        Thread.sleep(5000);
        Assert.assertFalse(whetherConnectTimeoutConsumedTooLong.get());
    }

    class MyThread implements Runnable {
        Url              url;
        int              connNum;
        boolean          warmup;
        RpcAddressParser parser;

        public MyThread(Url url, int connNum, boolean warmup) {
            this.url = url;
            this.connNum = connNum;
            this.warmup = warmup;
            this.parser = new RpcAddressParser();
        }

        @Override
        public void run() {
            InvokeContext ctx = new InvokeContext();
            try {
                RequestBody req = new RequestBody(1, "hello world sync");
                url.setConnectTimeout(100);// default to be 1000
                url.setConnNum(connNum);
                url.setConnWarmup(warmup);
                this.parser.initUrlArgs(url);
                client.invokeSync(url, req, ctx, 3000);
                long time = getAndPrintCreateConnTime(ctx);
                //                Assert.assertTrue(time < 1500);
            } catch (RemotingException e) {
                logger.error("error!", e);
                long time = getAndPrintCreateConnTime(ctx);
                //                Assert.assertTrue(time < 1500);
            } catch (Exception e) {
                logger.error("error!", e);
                long time = getAndPrintCreateConnTime(ctx);
                //                Assert.assertTrue(time < 1500);
            }
        }

        private long getAndPrintCreateConnTime(InvokeContext ctx) {
            long time = ctx.get(InvokeContext.CLIENT_CONN_CREATETIME) == null ? -1l : (Long) ctx
                .get(InvokeContext.CLIENT_CONN_CREATETIME);
            if (time > 1500) {
                whetherConnectTimeoutConsumedTooLong.set(true);
            }
            logger.warn("CREATE CONN TIME CONSUMED: " + time);
            return time;
        }

    }
}