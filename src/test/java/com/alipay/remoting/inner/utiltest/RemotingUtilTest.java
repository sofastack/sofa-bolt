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
package com.alipay.remoting.inner.utiltest;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alipay.remoting.BizContext;
import com.alipay.remoting.Connection;
import com.alipay.remoting.NamedThreadFactory;
import com.alipay.remoting.Url;
import com.alipay.remoting.rpc.RpcAddressParser;
import com.alipay.remoting.rpc.RpcClient;
import com.alipay.remoting.rpc.RpcServer;
import com.alipay.remoting.rpc.common.RequestBody;
import com.alipay.remoting.rpc.protocol.SyncUserProcessor;
import com.alipay.remoting.util.RemotingUtil;
import com.alipay.remoting.util.StringUtils;

import io.netty.channel.Channel;

/**
 * remoting util test
 * 
 * @author xiaomin.cxm
 * @version $Id: RemotingUtilTest.java, v 0.1 Jan 26, 2016 7:29:42 PM xiaomin.cxm Exp $
 */
public class RemotingUtilTest {

    Logger                      logger      = LoggerFactory.getLogger(RemotingUtilTest.class);

    Server                      server;
    RpcClient                   client;

    private static final int    port        = 1111;
    private static final String localIP     = "127.0.0.1";

    private static final Url    connAddress = new Url(localIP, port);
    RpcAddressParser            parser      = new RpcAddressParser();

    @Before
    public void init() {
        server = new Server();
        try {
            server.startServer();
            Thread.sleep(100);
        } catch (InterruptedException e) {
            logger.error("Start server failed!", e);
        }
        client = new RpcClient();
        client.init();
    }

    @After
    public void stop() {
        server.stopServer();
        client.closeConnection(connAddress);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            logger.error("Stop server failed!", e);
        }
    }

    /**
     * parse channel to get address(format [ip:port])
     */
    @Test
    public void testParseRemoteAddress() {
        Connection conn;
        try {
            parser.initUrlArgs(connAddress);
            conn = client.getConnection(connAddress, 1000);
            Channel channel = conn.getChannel();
            String res = RemotingUtil.parseRemoteAddress(channel);
            Assert.assertEquals(connAddress.getUniqueKey(), res);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.assertFalse(true);
        }

    }

    /**
     * parse channel to get address(format [ip:port])
     */
    @Test
    public void testParseLocalAddress() {
        Connection conn;
        try {
            parser.initUrlArgs(connAddress);
            conn = client.getConnection(connAddress, 1000);
            Channel channel = conn.getChannel();
            String res = RemotingUtil.parseLocalAddress(channel);
            Assert.assertNotNull(res);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.assertFalse(true);
        }

    }

    /**
     * parse channel to get ip (format [ip])
     */
    @Test
    public void testParseRemoteHostIp() {
        Connection conn;
        try {
            parser.initUrlArgs(connAddress);
            conn = client.getConnection(connAddress, 1000);
            Channel channel = conn.getChannel();
            String res = RemotingUtil.parseRemoteIP(channel);
            Assert.assertEquals(localIP, res);
        } catch (Exception e) {
            Assert.assertFalse(true);
        }
    }

    /**
     * parse {@link InetSocketAddress} to get address (format [ip:port])
     */
    @Test
    public void testParseSocketAddressToString() {
        String localhostName;
        String localIP;
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            localhostName = inetAddress.getHostName();
            localIP = inetAddress.getHostAddress();
            if (null == localIP || StringUtils.isBlank(localIP)) {
                return;
            }
        } catch (UnknownHostException e) {
            localhostName = "localhost";
            localIP = "127.0.0.1";
        }
        SocketAddress socketAddress = new InetSocketAddress(localhostName, port);
        String res = RemotingUtil.parseSocketAddressToString(socketAddress);
        Assert.assertEquals(localIP + ":" + port, res);
    }

    /**
     * parse InetSocketAddress to get address (format [ip:port])
     * 
     * e.g.1 /127.0.0.1:1234 -> 127.0.0.1:1234
     * e.g.2 sofatest-2.stack.alipay.net/10.209.155.54:12200 -> 10.209.155.54:12200
     */
    @Test
    public void testParseSocketAddressToString_MuiltiFormatTest() {
        SocketAddress socketAddress = new InetSocketAddress("/127.0.0.1", 1111);
        String res = RemotingUtil.parseSocketAddressToString(socketAddress);
        Assert.assertEquals("127.0.0.1:1111", res);

        socketAddress = new InetSocketAddress("sofatest-2.stack.alipay.net/127.0.0.1", 12200);
        res = RemotingUtil.parseSocketAddressToString(socketAddress);
        Assert.assertEquals("127.0.0.1:12200", res);
    }

    /**
     * parse InetSocketAddress to get ip (format [ip])
     */
    @Test
    public void testParseSocketAddressToHostIp() {
        String localhostName;
        String localIP;
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            localhostName = inetAddress.getHostName();
            localIP = inetAddress.getHostAddress();
            if (null == localIP || StringUtils.isBlank(localIP)) {
                return;
            }
        } catch (UnknownHostException e) {
            localhostName = "localhost";
            localIP = "127.0.0.1";
        }

        SocketAddress socketAddress = new InetSocketAddress(localhostName, port);
        String res = RemotingUtil.parseSocketAddressToHostIp(socketAddress);
        Assert.assertEquals(localIP, res);
    }

    class Server {
        Logger    logger = LoggerFactory.getLogger(Server.class);
        RpcServer server;

        public void startServer() {
            server = new RpcServer(port);
            server.registerUserProcessor(new SyncUserProcessor<RequestBody>() {
                ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 3, 60, TimeUnit.SECONDS,
                                                new ArrayBlockingQueue<Runnable>(4),
                                                new NamedThreadFactory("Request-process-pool"));

                @Override
                public Object handleRequest(BizContext bizCtx, RequestBody request) {
                    logger.warn("Request received:" + request);
                    return "Hello world!";
                }

                @Override
                public String interest() {
                    return RequestBody.class.toString();
                }

                @Override
                public Executor getExecutor() {
                    return executor;
                }

            });
            server.start();
        }

        public void stopServer() {
            server.stop();
        }
    }

}
