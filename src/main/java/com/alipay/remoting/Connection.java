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
package com.alipay.remoting;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;

import com.alipay.remoting.log.BoltLoggerFactory;
import com.alipay.remoting.rpc.protocol.RpcProtocolV2;
import com.alipay.remoting.util.ConcurrentHashSet;
import com.alipay.remoting.util.RemotingUtil;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.AttributeKey;

/**
 * An abstraction of socket channel.
 *
 * @author yunliang.shi
 * @version $Id: Connection.java, v 0.1 Mar 10, 2016 11:30:54 AM yunliang.shi Exp $
 */
public class Connection {

    private static final Logger                                                   logger           = BoltLoggerFactory
                                                                                                       .getLogger("CommonDefault");

    private Channel                                                               channel;

    private final ConcurrentHashMap<Integer, InvokeFuture>                        invokeFutureMap  = new ConcurrentHashMap<Integer, InvokeFuture>(
                                                                                                       4);

    /** Attribute key for connection */
    public static final AttributeKey<Connection>                                  CONNECTION       = AttributeKey
                                                                                                       .valueOf("connection");
    /** Attribute key for heartbeat count */
    public static final AttributeKey<Integer>                                     HEARTBEAT_COUNT  = AttributeKey
                                                                                                       .valueOf("heartbeatCount");

    /** Attribute key for heartbeat switch for each connection */
    public static final AttributeKey<Boolean>                                     HEARTBEAT_SWITCH = AttributeKey
                                                                                                       .valueOf("heartbeatSwitch");

    /** Attribute key for protocol */
    public static final AttributeKey<ProtocolCode>                                PROTOCOL         = AttributeKey
                                                                                                       .valueOf("protocol");
    private ProtocolCode                                                          protocolCode;

    /** Attribute key for version */
    public static final AttributeKey<Byte>                                        VERSION          = AttributeKey
                                                                                                       .valueOf("version");
    private byte                                                                  version          = RpcProtocolV2.PROTOCOL_VERSION_1;

    private Url                                                                   url;

    private final ConcurrentHashMap<Integer/* id */, String/* poolKey */>       id2PoolKey       = new ConcurrentHashMap<Integer, String>(
                                                                                                       256);

    private Set<String>                                                           poolKeys         = new ConcurrentHashSet<String>();

    private AtomicBoolean                                                         closed           = new AtomicBoolean(
                                                                                                       false);

    private final ConcurrentHashMap<String/* attr key*/, Object /*attr value*/> attributes       = new ConcurrentHashMap<String, Object>();

    /** the reference count used for this connection. If equals 2, it means this connection has been referenced 2 times */
    private final AtomicInteger                                                   referenceCount   = new AtomicInteger();

    /** no reference of the current connection */
    private static final int                                                      NO_REFERENCE     = 0;

    /**
     * Constructor
     *
     * @param channel
     */
    public Connection(Channel channel) {
        this.channel = channel;
        this.channel.attr(CONNECTION).set(this);
    }

    /**
     * Constructor
     *
     * @param channel
     * @param url
     */
    public Connection(Channel channel, Url url) {
        this(channel);
        this.url = url;
        this.poolKeys.add(url.getUniqueKey());
    }

    /**
     * Constructor
     *
     * @param channel
     * @param protocolCode
     * @param url
     */
    public Connection(Channel channel, ProtocolCode protocolCode, Url url) {
        this(channel, url);
        this.protocolCode = protocolCode;
        this.init();
    }

    /**
     * Constructor
     *
     * @param channel
     * @param protocolCode
     * @param url
     */
    public Connection(Channel channel, ProtocolCode protocolCode, byte version, Url url) {
        this(channel, url);
        this.protocolCode = protocolCode;
        this.version = version;
        this.init();
    }

    /**
     * Initialization.
     */
    private void init() {
        this.channel.attr(HEARTBEAT_COUNT).set(new Integer(0));
        this.channel.attr(PROTOCOL).set(this.protocolCode);
        this.channel.attr(VERSION).set(this.version);
        this.channel.attr(HEARTBEAT_SWITCH).set(true);
    }

    /**
     * to check whether the connection is fine to use
     *
     * @return
     */
    public boolean isFine() {
        return this.channel != null && this.channel.isActive();
    }

    /**
     * increase the reference count
     */
    public void increaseRef() {
        this.referenceCount.getAndIncrement();
    }

    /**
     * decrease the reference count
     */
    public void decreaseRef() {
        this.referenceCount.getAndDecrement();
    }

    /**
     * to check whether the reference count is 0
     *
     * @return
     */
    public boolean noRef() {
        return this.referenceCount.get() == NO_REFERENCE;
    }

    /**
     * Get the address of the remote peer.
     *
     * @return
     */
    public InetSocketAddress getRemoteAddress() {
        return (InetSocketAddress) this.channel.remoteAddress();
    }

    /**
     * Get the remote IP.
     *
     * @return
     */
    public String getRemoteIP() {
        return RemotingUtil.parseRemoteIP(this.channel);
    }

    /**
     * Get the remote port.
     *
     * @return
     */
    public int getRemotePort() {
        return RemotingUtil.parseRemotePort(this.channel);
    }

    /**
     * Get the address of the local peer.
     *
     * @return
     */
    public InetSocketAddress getLocalAddress() {
        return (InetSocketAddress) this.channel.localAddress();
    }

    /**
     * Get the local IP.
     *
     * @return
     */
    public String getLocalIP() {
        return RemotingUtil.parseLocalIP(this.channel);
    }

    /**
     * Get the local port.
     *
     * @return
     */
    public int getLocalPort() {
        return RemotingUtil.parseLocalPort(this.channel);
    }

    /**
     * Get the netty channel of the connection.
     *
     * @return
     */
    public Channel getChannel() {
        return this.channel;
    }

    /**
     * Get the InvokeFuture with invokeId of id.
     *
     * @param id
     * @return
     */
    public InvokeFuture getInvokeFuture(int id) {
        return this.invokeFutureMap.get(id);
    }

    /**
     * Add an InvokeFuture
     *
     * @param future
     * @return
     */
    public InvokeFuture addInvokeFuture(InvokeFuture future) {
        return this.invokeFutureMap.putIfAbsent(future.invokeId(), future);
    }

    /**
     * Remove InvokeFuture who's invokeId is id
     *
     * @param id
     * @return
     */
    public InvokeFuture removeInvokeFuture(int id) {
        return this.invokeFutureMap.remove(id);
    }

    /**
     * Do something when closing.
     */
    public void onClose() {
        Iterator<Entry<Integer, InvokeFuture>> iter = invokeFutureMap.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<Integer, InvokeFuture> entry = iter.next();
            iter.remove();
            InvokeFuture future = entry.getValue();
            if (future != null) {
                future.putResponse(future.createConnectionClosedResponse(this.getRemoteAddress()));
                future.cancelTimeout();
                future.tryAsyncExecuteInvokeCallbackAbnormally();
            }
        }
    }

    /**
     * Close the connection.
     */
    public void close() {
        if (closed.compareAndSet(false, true)) {
            try {
                if (this.getChannel() != null) {
                    this.getChannel().close().addListener(new ChannelFutureListener() {

                        @Override
                        public void operationComplete(ChannelFuture future) throws Exception {
                            if (logger.isInfoEnabled()) {
                                logger
                                    .info(
                                        "Close the connection to remote address={}, result={}, cause={}",
                                        RemotingUtil.parseRemoteAddress(Connection.this
                                            .getChannel()), future.isSuccess(), future.cause());
                            }
                        }

                    });
                }
            } catch (Exception e) {
                logger.warn("Exception caught when closing connection {}",
                    RemotingUtil.parseRemoteAddress(Connection.this.getChannel()), e);
            }
        }
    }

    /**
    * Whether invokeFutures is completed
    *
    */
    public boolean isInvokeFutureMapFinish() {
        return invokeFutureMap.isEmpty();
    }

    /**
     * add a pool key to list
     *
     * @param poolKey
     */
    public void addPoolKey(String poolKey) {
        poolKeys.add(poolKey);
    }

    /**
     * get all pool keys
     */
    public Set<String> getPoolKeys() {
        return new HashSet<String>(poolKeys);
    }

    /**
     * remove pool key
     *
     * @param poolKey
     */
    public void removePoolKey(String poolKey) {
        poolKeys.remove(poolKey);
    }

    /**
     * Getter method for property <tt>url</tt>.
     *
     * @return property value of url
     */
    public Url getUrl() {
        return url;
    }

    /**
     * add Id to group Mapping
     *
     * @param id
     * @param poolKey
     */
    public void addIdPoolKeyMapping(Integer id, String poolKey) {
        this.id2PoolKey.put(id, poolKey);
    }

    /**
     * remove id to group Mapping
     *
     * @param id
     * @return
     */
    public String removeIdPoolKeyMapping(Integer id) {
        return this.id2PoolKey.remove(id);
    }

    /**
     * Set attribute key=value.
     *
     * @param key
     * @param value
     */
    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    /**
     * set attribute if key absent.
     *
     * @param key
     * @param value
     * @return
     */
    public Object setAttributeIfAbsent(String key, Object value) {
        return attributes.putIfAbsent(key, value);
    }

    /**
     * Remove attribute.
     *
     * @param key
     */
    public void removeAttribute(String key) {
        attributes.remove(key);
    }

    /**
     * Get attribute.
     *
     * @param key
     * @return
     */
    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    /**
     * Clear attribute.
     */
    public void clearAttributes() {
        attributes.clear();
    }

    /**
     * Getter method for property <tt>invokeFutureMap</tt>.
     *
     * @return property value of invokeFutureMap
     */
    public ConcurrentHashMap<Integer, InvokeFuture> getInvokeFutureMap() {
        return invokeFutureMap;
    }
}
