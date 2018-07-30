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

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alipay.remoting.Connection;
import com.alipay.remoting.ConnectionEventHandler;
import com.alipay.remoting.ConnectionEventListener;
import com.alipay.remoting.ConnectionEventType;
import com.alipay.remoting.ConnectionSelectStrategy;
import com.alipay.remoting.DefaultConnectionManager;
import com.alipay.remoting.RandomSelectStrategy;
import com.alipay.remoting.RemotingAddressParser;
import com.alipay.remoting.Url;
import com.alipay.remoting.connection.ConnectionFactory;
import com.alipay.remoting.exception.RemotingException;
import com.alipay.remoting.rpc.RpcAddressParser;
import com.alipay.remoting.rpc.RpcClient;
import com.alipay.remoting.rpc.RpcConnectionEventHandler;
import com.alipay.remoting.rpc.RpcConnectionFactory;
import com.alipay.remoting.rpc.common.BoltServer;
import com.alipay.remoting.rpc.common.CONNECTEventProcessor;
import com.alipay.remoting.rpc.protocol.UserProcessor;

/**
 * Rpc connection manager test
 * 
 * @author xiaomin.cxm
 * @version $Id: RpcConnectionManagerTest.java, v 0.1 Mar 9, 2016 8:09:44 PM xiaomin.cxm Exp $
 */
public class RpcConnectionManagerTest {
    private final static Logger                         logger                   = LoggerFactory
                                                                                     .getLogger(RpcConnectionManagerTest.class);

    private ConcurrentHashMap<String, UserProcessor<?>> userProcessors           = new ConcurrentHashMap<String, UserProcessor<?>>();

    private DefaultConnectionManager                    cm;
    private ConnectionSelectStrategy                    connectionSelectStrategy = new RandomSelectStrategy();
    private RemotingAddressParser                       addressParser            = new RpcAddressParser();
    private ConnectionFactory                           connectionFactory        = new RpcConnectionFactory(
                                                                                     userProcessors,
                                                                                     new RpcClient());
    private ConnectionEventHandler                      connectionEventHandler   = new RpcConnectionEventHandler();
    private ConnectionEventListener                     connectionEventListener  = new ConnectionEventListener();

    private BoltServer                                  server;

    private String                                      ip                       = "127.0.0.1";
    private int                                         port                     = 1111;
    private String                                      addr                     = ip + ":" + port;
    private String                                      poolKey                  = ip + ":" + port;
    private Url                                         url                      = new Url(ip, port);

    CONNECTEventProcessor                               serverConnectProcessor   = new CONNECTEventProcessor();

    @Before
    public void init() {
        cm = new DefaultConnectionManager(connectionSelectStrategy, connectionFactory,
            connectionEventHandler, connectionEventListener);
        cm.setAddressParser(addressParser);
        cm.init();
        server = new BoltServer(port);
        server.start();
        server.addConnectionEventProcessor(ConnectionEventType.CONNECT, serverConnectProcessor);
        this.addressParser.initUrlArgs(url);
    }

    @After
    public void stop() {
        try {
            server.stop();
            Thread.sleep(1);
        } catch (InterruptedException e) {
            logger.error("Stop server failed!", e);
        }
    }

    @Test
    public void testAdd() {
        Connection conn = getAConn();
        cm.add(conn);
        Assert.assertEquals(1, cm.count(poolKey));
    }

    @Test
    public void testAddWithPoolKey() {
        Connection conn = getAConn();
        cm.add(conn, poolKey);
        Assert.assertEquals(1, cm.count(poolKey));
    }

    @Test
    public void testAddWconnithPoolKey_multiPoolKey() throws InterruptedException {
        Connection conn = getAConn();
        cm.add(conn, poolKey);
        cm.add(conn, "GROUP1");
        cm.add(conn, "GROUP2");
        Assert.assertEquals(1, cm.count(poolKey));
        Assert.assertEquals(1, cm.count("GROUP1"));
        Assert.assertEquals(1, cm.count("GROUP2"));

        cm.remove(conn, poolKey);
        Assert.assertTrue(conn.isFine());
        Assert.assertTrue(cm.get(poolKey) == null);
        Assert.assertTrue(cm.get("GROUP1").isFine());
        Assert.assertTrue(cm.get("GROUP2").isFine());

        cm.remove(conn, "GROUP1");
        cm.remove(conn, "GROUP2");
        Thread.sleep(1000);
        Assert.assertFalse(conn.isFine());
        Assert.assertTrue(cm.get(poolKey) == null);
        Assert.assertTrue(cm.get("GROUP1") == null);
        Assert.assertTrue(cm.get("GROUP2") == null);
    }

    @Test
    public void testGet() {
        Connection pool = cm.get(poolKey);
        Assert.assertNull(pool);
        cm.add(getAConn());
        Assert.assertNotNull(cm.get(poolKey));
    }

    @Test
    public void testGetAllWithPoolKey() {
        cm.add(getAConn());
        cm.add(getAConn());
        cm.add(getAConn());
        Assert.assertEquals(3, cm.getAll(poolKey).size());
    }

    @Test
    public void testGetAll() {
        cm.add(getAConn());
        cm.add(getAConn());
        cm.add(getAConn());
        cm.add(getAConn());
        Map<String, List<Connection>> conns = cm.getAll();
        Assert.assertEquals(1, conns.size());
        Assert.assertEquals(4, conns.get(poolKey).size());
    }

    @Test
    public void testRemoveConn() {
        Connection conn1 = getAConn();
        conn1.addPoolKey("hehe");
        Connection conn2 = getAConn();
        conn2.addPoolKey("hehe");
        cm.add(conn1);
        cm.add(conn2);
        Assert.assertEquals(2, cm.count(poolKey));
        cm.remove(conn1);
        Assert.assertEquals(1, cm.count(poolKey));
        cm.remove(conn2);
        Assert.assertEquals(0, cm.count(poolKey));
    }

    @Test
    public void testRemoveConnWithSpecifiedPoolkey() {
        Connection conn1 = getAConn();
        conn1.addPoolKey("hehe");
        Connection conn2 = getAConn();
        conn2.addPoolKey("hehe");
        cm.add(conn1);
        cm.add(conn2);
        Assert.assertEquals(2, cm.count(poolKey));
        cm.remove(conn1, poolKey);
        Assert.assertEquals(1, cm.count(poolKey));
    }

    @Test
    public void testRemoveAllConnsOfSpecifiedPoolKey() {
        Connection conn1 = getAConn();
        conn1.addPoolKey("hehe");
        Connection conn2 = getAConn();
        conn2.addPoolKey("hehe");
        cm.add(conn1);
        cm.add(conn2);
        Assert.assertEquals(2, cm.count(poolKey));
        cm.remove(poolKey);
        Assert.assertEquals(0, cm.count(poolKey));
    }

    @Test
    public void testGetAndCreateIfAbsent() {
        try {
            Connection conn = cm.getAndCreateIfAbsent(url);
            Assert.assertNotNull(conn);
        } catch (RemotingException e) {
            Assert.fail("should not reach here!");
        } catch (InterruptedException e) {
            Assert.fail("should not reach here!");
        }

    }

    @Test
    public void testCreateUsingUrl() {
        try {
            Connection conn = cm.create(url);
            Assert.assertNotNull(conn);
        } catch (RemotingException e) {
            Assert.fail("should not reach here!");
        }

    }

    @Test
    public void testCreateUsingAddress() {
        try {
            Connection conn = cm.create(addr, 1000);
            Assert.assertNotNull(conn);
        } catch (RemotingException e) {
            Assert.fail("should not reach here!");
        }

    }

    @Test
    public void testCreateUsingIpPort() {
        try {
            Connection conn = cm.create(ip, port, 1000);
            Assert.assertNotNull(conn);
        } catch (RemotingException e) {
            Assert.fail("should not reach here!");
        }

    }

    // ~~~ combinations

    @Test
    public void testGetAndCheckConnection() {
        final Url addr = new Url(ip, port);

        try {
            this.addressParser.initUrlArgs(addr);
            Connection conn = cm.getAndCreateIfAbsent(addr);
            Assert.assertTrue(conn.isFine());
        } catch (RemotingException e) {
            Assert.fail("RemotingException!");
        } catch (InterruptedException e) {
            Assert.fail("InterruptedException!");
        }
    }

    @Test
    public void testCheckConnectionException() {
        final Url addr = new Url(ip, port);

        Connection conn = null;
        try {
            cm.check(conn);
            Assert.assertTrue(false);
        } catch (RemotingException e) {
            logger.error("Connection null", e);
            Assert.assertTrue(true);
        }

        try {
            this.addressParser.initUrlArgs(addr);
            conn = cm.getAndCreateIfAbsent(addr);
            Assert.assertEquals(1, cm.count(addr.getUniqueKey()));
            conn.close();
            Thread.sleep(100);
            cm.check(conn);
            Assert.assertTrue(false);
        } catch (RemotingException e) {
            // test  remove success when do check
            Assert.assertEquals(0, cm.count(addr.getUniqueKey()));
            Assert.assertTrue(true);
        } catch (InterruptedException e) {
            logger.error("", e);
        }
    }

    @Test
    public void testAddAndRemoveConnection() {
        String poolKey = ip + ":" + port;
        int connectTimeoutMillis = 1000;

        Connection conn = null;
        try {
            conn = cm.create(ip, port, connectTimeoutMillis);
            conn.addPoolKey(poolKey);
        } catch (Exception e) {
            logger.error("", e);
        }
        cm.add(conn);
        Assert.assertEquals(1, cm.count(poolKey));
        cm.remove(conn);
        Assert.assertEquals(0, cm.count(poolKey));
    }

    @Test
    public void testRemoveAddress() {
        String poolKey = ip + ":" + port;
        final Url addr = new Url(ip, port);

        try {
            this.addressParser.initUrlArgs(addr);
            Connection conn = cm.getAndCreateIfAbsent(addr);
            try {
                cm.check(conn);
            } catch (Exception e) {
                Assert.assertTrue(false);
            }
            conn = cm.getAndCreateIfAbsent(addr);
            cm.check(conn);
            Assert.assertTrue(true);
            cm.remove(poolKey);
            Assert.assertEquals(0, cm.count(poolKey));
        } catch (Exception e) {
            Assert.assertTrue(false);
        }
    }

    @Test
    public void testConnectionCloseAndConnectionManagerRemove() throws RemotingException,
                                                               InterruptedException {
        final Url addr = new Url(ip, port);

        this.addressParser.initUrlArgs(addr);
        Connection conn = null;
        try {
            conn = cm.getAndCreateIfAbsent(addr);
        } catch (InterruptedException e) {
            Assert.fail("InterruptedException!");
        }

        Assert.assertEquals(1, cm.count(addr.getUniqueKey()));
        conn.close();
        Thread.sleep(100);
        Assert.assertEquals(0, cm.count(addr.getUniqueKey()));
    }

    /**
     * get a connection
     *
     * @return
     */
    private Connection getAConn() {
        try {
            return cm.create(ip, port, 1000);
        } catch (RemotingException e) {
            logger.error("Create connection failed!");
        }
        return null;
    }
}
