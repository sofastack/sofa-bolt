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
package com.alipay.remoting.rpc;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;

import com.alipay.remoting.Connection;
import com.alipay.remoting.ConnectionEventHandler;
import com.alipay.remoting.ConnectionEventListener;
import com.alipay.remoting.ConnectionEventProcessor;
import com.alipay.remoting.ConnectionEventType;
import com.alipay.remoting.ConnectionMonitorStrategy;
import com.alipay.remoting.ConnectionPool;
import com.alipay.remoting.ConnectionSelectStrategy;
import com.alipay.remoting.DefaultConnectionManager;
import com.alipay.remoting.DefaultConnectionMonitor;
import com.alipay.remoting.InvokeCallback;
import com.alipay.remoting.InvokeContext;
import com.alipay.remoting.RandomSelectStrategy;
import com.alipay.remoting.ReconnectManager;
import com.alipay.remoting.RemotingAddressParser;
import com.alipay.remoting.ScheduledDisconnectStrategy;
import com.alipay.remoting.Url;
import com.alipay.remoting.config.AbstractConfigurableInstance;
import com.alipay.remoting.config.configs.ConfigType;
import com.alipay.remoting.config.switches.GlobalSwitch;
import com.alipay.remoting.connection.ConnectionFactory;
import com.alipay.remoting.exception.RemotingException;
import com.alipay.remoting.log.BoltLoggerFactory;
import com.alipay.remoting.rpc.protocol.UserProcessor;
import com.alipay.remoting.rpc.protocol.UserProcessorRegisterHelper;

/**
 * Client for Rpc.
 * 
 * @author jiangping
 * @version $Id: RpcClient.java, v 0.1 2015-9-23 PM4:03:28 tao Exp $
 */
public class RpcClient extends AbstractConfigurableInstance {

    /** logger */
    private static final Logger                         logger                   = BoltLoggerFactory
                                                                                     .getLogger("RpcRemoting");

    private ConcurrentHashMap<String, UserProcessor<?>> userProcessors           = new ConcurrentHashMap<String, UserProcessor<?>>();
    /** connection factory */
    private ConnectionFactory                           connectionFactory        = new RpcConnectionFactory(
                                                                                     userProcessors,
                                                                                     this);

    /** connection event handler */
    private ConnectionEventHandler                      connectionEventHandler   = new RpcConnectionEventHandler(
                                                                                     switches());

    /** reconnect manager */
    private ReconnectManager                            reconnectManager;

    /** connection event listener */
    private ConnectionEventListener                     connectionEventListener  = new ConnectionEventListener();

    /** address parser to get custom args */
    private RemotingAddressParser                       addressParser;

    /** connection select strategy */
    private ConnectionSelectStrategy                    connectionSelectStrategy = new RandomSelectStrategy(
                                                                                     switches());

    /** connection manager */
    private DefaultConnectionManager                    connectionManager        = new DefaultConnectionManager(
                                                                                     connectionSelectStrategy,
                                                                                     connectionFactory,
                                                                                     connectionEventHandler,
                                                                                     connectionEventListener,
                                                                                     switches());

    /** rpc remoting */
    protected RpcRemoting                               rpcRemoting;

    /** task scanner */
    private RpcTaskScanner                              taskScanner              = new RpcTaskScanner();

    /** connection monitor */
    private DefaultConnectionMonitor                    connectionMonitor;

    /** connection monitor strategy */
    private ConnectionMonitorStrategy                   monitorStrategy;

    public RpcClient() {
        super(ConfigType.CLIENT_SIDE);
    }

    public void init() {
        if (this.addressParser == null) {
            this.addressParser = new RpcAddressParser();
        }
        this.connectionManager.setAddressParser(this.addressParser);
        this.connectionManager.init();
        this.rpcRemoting = new RpcClientRemoting(new RpcCommandFactory(), this.addressParser,
            this.connectionManager);
        this.taskScanner.add(this.connectionManager);
        this.taskScanner.start();

        if (switches().isOn(GlobalSwitch.CONN_MONITOR_SWITCH)) {
            if (monitorStrategy == null) {
                ScheduledDisconnectStrategy strategy = new ScheduledDisconnectStrategy();
                connectionMonitor = new DefaultConnectionMonitor(strategy, this.connectionManager);
            } else {
                connectionMonitor = new DefaultConnectionMonitor(monitorStrategy,
                    this.connectionManager);
            }
            connectionMonitor.start();
            logger.warn("Switch on connection monitor");
        }
        if (switches().isOn(GlobalSwitch.CONN_RECONNECT_SWITCH)) {
            reconnectManager = new ReconnectManager(connectionManager);
            connectionEventHandler.setReconnectManager(reconnectManager);
            logger.warn("Switch on reconnect manager");
        }
    }

    /**
     * Shutdown.
     * <p>
     * Notice:<br>
     *   <li>Rpc client can not be used any more after shutdown.
     *   <li>If you need, you should destroy it, and instantiate another one.
     */
    public void shutdown() {
        this.connectionManager.removeAll();
        logger.warn("Close all connections from client side!");
        this.taskScanner.shutdown();
        logger.warn("Rpc client shutdown!");
        if (reconnectManager != null) {
            reconnectManager.stop();
        }
        if (connectionMonitor != null) {
            connectionMonitor.destroy();
        }
    }

    /**
     * One way invocation using a string address, address format example - 127.0.0.1:12200?key1=value1&key2=value2 <br>
     * <p>
     * Notice:<br>
     *   <ol>
     *   <li><b>DO NOT modify the request object concurrently when this method is called.</b></li>
     *   <li>When do invocation, use the string address to find a available connection, if none then create one.</li> 
     *      <ul>
     *        <li>You can use {@link RpcConfigs#CONNECT_TIMEOUT_KEY} to specify connection timeout, time unit is milliseconds, e.g [127.0.0.1:12200?_CONNECTTIMEOUT=3000]
     *        <li>You can use {@link RpcConfigs#CONNECTION_NUM_KEY} to specify connection number for each ip and port, e.g [127.0.0.1:12200?_CONNECTIONNUM=30]
     *        <li>You can use {@link RpcConfigs#CONNECTION_WARMUP_KEY} to specify whether need warmup all connections for the first time you call this method, e.g [127.0.0.1:12200?_CONNECTIONWARMUP=false]
     *      </ul>
     *   <li>You should use {@link #closeConnection(String addr)} to close it if you want.
     *   </ol>
     * 
     * @param addr
     * @param request
     * @throws RemotingException
     * @throws InterruptedException
     */
    public void oneway(final String addr, final Object request) throws RemotingException,
                                                               InterruptedException {
        this.rpcRemoting.oneway(addr, request, null);
    }

    /**
     * Oneway invocation with a {@link InvokeContext}, common api notice please see {@link #oneway(Connection, Object)}
     *
     * @param addr
     * @param request
     * @param invokeContext
     * @throws RemotingException
     * @throws InterruptedException
     */
    public void oneway(final String addr, final Object request, final InvokeContext invokeContext)
                                                                                                  throws RemotingException,
                                                                                                  InterruptedException {
        this.rpcRemoting.oneway(addr, request, invokeContext);
    }

    /**
     * One way invocation using a parsed {@link Url} <br>
     * <p>
     * Notice:<br>
     *   <ol>
     *   <li><b>DO NOT modify the request object concurrently when this method is called.</b></li>
     *   <li>When do invocation, use the parsed {@link Url} to find a available connection, if none then create one.</li> 
     *      <ul>
     *        <li>You can use {@link Url#setConnectTimeout} to specify connection timeout, time unit is milliseconds.
     *        <li>You can use {@link Url#setConnNum} to specify connection number for each ip and port.
     *        <li>You can use {@link Url#setConnWarmup} to specify whether need warmup all connections for the first time you call this method.
     *      </ul>
     *   <li>You should use {@link #closeConnection(Url url)} to close it if you want.
     *   </ol>
     * 
     * @param url
     * @param request
     * @throws RemotingException
     * @throws InterruptedException
     */
    public void oneway(final Url url, final Object request) throws RemotingException,
                                                           InterruptedException {
        this.rpcRemoting.oneway(url, request, null);
    }

    /**
     * Oneway invocation with a {@link InvokeContext}, common api notice please see {@link #oneway(Url, Object)}
     *
     * @param url
     * @param request
     * @param invokeContext
     * @throws RemotingException
     * @throws InterruptedException
     */
    public void oneway(final Url url, final Object request, final InvokeContext invokeContext)
                                                                                              throws RemotingException,
                                                                                              InterruptedException {
        this.rpcRemoting.oneway(url, request, invokeContext);
    }

    /**
     * One way invocation using a {@link Connection} <br>
     * <p>
     * Notice:<br>
     *   <b>DO NOT modify the request object concurrently when this method is called.</b>
     * 
     * @param conn
     * @param request
     * @throws RemotingException
     */
    public void oneway(final Connection conn, final Object request) throws RemotingException {
        this.rpcRemoting.oneway(conn, request, null);
    }

    /**
     * Oneway invocation with a {@link InvokeContext}, common api notice please see {@link #oneway(Connection, Object)}
     *
     * @param conn
     * @param request
     * @param invokeContext
     * @throws RemotingException
     */
    public void oneway(final Connection conn, final Object request,
                       final InvokeContext invokeContext) throws RemotingException {
        this.rpcRemoting.oneway(conn, request, invokeContext);
    }

    /**
     * Synchronous invocation using a string address, address format example - 127.0.0.1:12200?key1=value1&key2=value2 <br>
     * <p>
     * Notice:<br>
     *   <ol>
     *   <li><b>DO NOT modify the request object concurrently when this method is called.</b></li>
     *   <li>When do invocation, use the string address to find a available connection, if none then create one.</li> 
     *      <ul>
     *        <li>You can use {@link RpcConfigs#CONNECT_TIMEOUT_KEY} to specify connection timeout, time unit is milliseconds, e.g [127.0.0.1:12200?_CONNECTTIMEOUT=3000]
     *        <li>You can use {@link RpcConfigs#CONNECTION_NUM_KEY} to specify connection number for each ip and port, e.g [127.0.0.1:12200?_CONNECTIONNUM=30]
     *        <li>You can use {@link RpcConfigs#CONNECTION_WARMUP_KEY} to specify whether need warmup all connections for the first time you call this method, e.g [127.0.0.1:12200?_CONNECTIONWARMUP=false]
     *      </ul>
     *   <li>You should use {@link #closeConnection(String addr)} to close it if you want.
     *   </ol>
     * 
     * @param addr
     * @param request
     * @param timeoutMillis
     * @return Object
     * @throws RemotingException
     * @throws InterruptedException
     */
    public Object invokeSync(final String addr, final Object request, final int timeoutMillis)
                                                                                              throws RemotingException,
                                                                                              InterruptedException {
        return this.rpcRemoting.invokeSync(addr, request, null, timeoutMillis);
    }

    /**
     * Synchronous invocation with a {@link InvokeContext}, common api notice please see {@link #invokeSync(String, Object, int)}
     *
     * @param addr
     * @param request
     * @param invokeContext
     * @param timeoutMillis
     * @return
     * @throws RemotingException
     * @throws InterruptedException
     */
    public Object invokeSync(final String addr, final Object request,
                             final InvokeContext invokeContext, final int timeoutMillis)
                                                                                        throws RemotingException,
                                                                                        InterruptedException {
        return this.rpcRemoting.invokeSync(addr, request, invokeContext, timeoutMillis);
    }

    /**
     * Synchronous invocation using a parsed {@link Url} <br>
     * <p>
     * Notice:<br>
     *   <ol>
     *   <li><b>DO NOT modify the request object concurrently when this method is called.</b></li>
     *   <li>When do invocation, use the parsed {@link Url} to find a available connection, if none then create one.</li> 
     *      <ul>
     *        <li>You can use {@link Url#setConnectTimeout} to specify connection timeout, time unit is milliseconds.
     *        <li>You can use {@link Url#setConnNum} to specify connection number for each ip and port.
     *        <li>You can use {@link Url#setConnWarmup} to specify whether need warmup all connections for the first time you call this method.
     *      </ul>
     *   <li>You should use {@link #closeConnection(Url url)} to close it if you want.
     *   </ol>
     * 
     * @param url
     * @param request
     * @param timeoutMillis
     * @return Object
     * @throws RemotingException
     * @throws InterruptedException
     */
    public Object invokeSync(final Url url, final Object request, final int timeoutMillis)
                                                                                          throws RemotingException,
                                                                                          InterruptedException {
        return this.invokeSync(url, request, null, timeoutMillis);
    }

    /**
     * Synchronous invocation with a {@link InvokeContext}, common api notice please see {@link #invokeSync(Url, Object, int)}
     *
     * @param url
     * @param request
     * @param invokeContext
     * @param timeoutMillis
     * @return
     * @throws RemotingException
     * @throws InterruptedException
     */
    public Object invokeSync(final Url url, final Object request,
                             final InvokeContext invokeContext, final int timeoutMillis)
                                                                                        throws RemotingException,
                                                                                        InterruptedException {
        return this.rpcRemoting.invokeSync(url, request, invokeContext, timeoutMillis);
    }

    /**
     * Synchronous invocation using a {@link Connection} <br>
     * <p>
     * Notice:<br>
     *   <b>DO NOT modify the request object concurrently when this method is called.</b>
     * 
     * @param conn
     * @param request
     * @param timeoutMillis
     * @return Object
     * @throws RemotingException
     * @throws InterruptedException
     */
    public Object invokeSync(final Connection conn, final Object request, final int timeoutMillis)
                                                                                                  throws RemotingException,
                                                                                                  InterruptedException {
        return this.rpcRemoting.invokeSync(conn, request, null, timeoutMillis);
    }

    /**
     * Synchronous invocation with a {@link InvokeContext}, common api notice please see {@link #invokeSync(Connection, Object, int)}
     *
     * @param conn
     * @param request
     * @param invokeContext
     * @param timeoutMillis
     * @return Object
     * @throws RemotingException
     * @throws InterruptedException
     */
    public Object invokeSync(final Connection conn, final Object request,
                             final InvokeContext invokeContext, final int timeoutMillis)
                                                                                        throws RemotingException,
                                                                                        InterruptedException {
        return this.rpcRemoting.invokeSync(conn, request, invokeContext, timeoutMillis);
    }

    /**
     * Future invocation using a string address, address format example - 127.0.0.1:12200?key1=value1&key2=value2 <br>
     * You can get result use the returned {@link RpcResponseFuture}.
     * <p>
     * Notice:<br>
     *   <ol>
     *   <li><b>DO NOT modify the request object concurrently when this method is called.</b></li>
     *   <li>When do invocation, use the string address to find a available connection, if none then create one.</li> 
     *      <ul>
     *        <li>You can use {@link RpcConfigs#CONNECT_TIMEOUT_KEY} to specify connection timeout, time unit is milliseconds, e.g [127.0.0.1:12200?_CONNECTTIMEOUT=3000]
     *        <li>You can use {@link RpcConfigs#CONNECTION_NUM_KEY} to specify connection number for each ip and port, e.g [127.0.0.1:12200?_CONNECTIONNUM=30]
     *        <li>You can use {@link RpcConfigs#CONNECTION_WARMUP_KEY} to specify whether need warmup all connections for the first time you call this method, e.g [127.0.0.1:12200?_CONNECTIONWARMUP=false]
     *      </ul>
     *   <li>You should use {@link #closeConnection(String addr)} to close it if you want.
     *   </ol>
     * 
     * @param addr
     * @param request
     * @param timeoutMillis
     * @return RpcResponseFuture
     * @throws RemotingException
     * @throws InterruptedException
     */
    public RpcResponseFuture invokeWithFuture(final String addr, final Object request,
                                              final int timeoutMillis) throws RemotingException,
                                                                      InterruptedException {
        return this.rpcRemoting.invokeWithFuture(addr, request, null, timeoutMillis);
    }

    /**
     * Future invocation with a {@link InvokeContext}, common api notice please see {@link #invokeWithFuture(String, Object, int)}
     *
     * @param addr
     * @param request
     * @param invokeContext
     * @param timeoutMillis
     * @return RpcResponseFuture
     * @throws RemotingException
     * @throws InterruptedException
     */
    public RpcResponseFuture invokeWithFuture(final String addr, final Object request,
                                              final InvokeContext invokeContext,
                                              final int timeoutMillis) throws RemotingException,
                                                                      InterruptedException {
        return this.rpcRemoting.invokeWithFuture(addr, request, invokeContext, timeoutMillis);
    }

    /**
     * Future invocation using a parsed {@link Url} <br>
     * You can get result use the returned {@link RpcResponseFuture}.
     * <p>
     * Notice:<br>
     *   <ol>
     *   <li><b>DO NOT modify the request object concurrently when this method is called.</b></li>
     *   <li>When do invocation, use the parsed {@link Url} to find a available connection, if none then create one.</li> 
     *      <ul>
     *        <li>You can use {@link Url#setConnectTimeout} to specify connection timeout, time unit is milliseconds.
     *        <li>You can use {@link Url#setConnNum} to specify connection number for each ip and port.
     *        <li>You can use {@link Url#setConnWarmup} to specify whether need warmup all connections for the first time you call this method.
     *      </ul>
     *   <li>You should use {@link #closeConnection(Url url)} to close it if you want.
     *   </ol>
     * 
     * @param url
     * @param request
     * @param timeoutMillis
     * @return
     * @throws RemotingException
     * @throws InterruptedException
     */
    public RpcResponseFuture invokeWithFuture(final Url url, final Object request,
                                              final int timeoutMillis) throws RemotingException,
                                                                      InterruptedException {
        return this.rpcRemoting.invokeWithFuture(url, request, null, timeoutMillis);
    }

    /**
     * Future invocation with a {@link InvokeContext}, common api notice please see {@link #invokeWithFuture(Url, Object, int)}
     *
     * @param url
     * @param request
     * @param invokeContext
     * @param timeoutMillis
     * @return
     * @throws RemotingException
     * @throws InterruptedException
     */
    public RpcResponseFuture invokeWithFuture(final Url url, final Object request,
                                              final InvokeContext invokeContext,
                                              final int timeoutMillis) throws RemotingException,
                                                                      InterruptedException {
        return this.rpcRemoting.invokeWithFuture(url, request, invokeContext, timeoutMillis);
    }

    /**
     * Future invocation using a {@link Connection} <br>
     * You can get result use the returned {@link RpcResponseFuture}.
     * <p>
     * Notice:<br>
     *   <b>DO NOT modify the request object concurrently when this method is called.</b>
     * 
     * @param conn
     * @param request
     * @param timeoutMillis
     * @return
     * @throws RemotingException
     */
    public RpcResponseFuture invokeWithFuture(final Connection conn, final Object request,
                                              int timeoutMillis) throws RemotingException {
        return this.rpcRemoting.invokeWithFuture(conn, request, null, timeoutMillis);
    }

    /**
     * Future invocation with a {@link InvokeContext}, common api notice please see {@link #invokeWithFuture(Connection, Object, int)}
     *
     * @param conn
     * @param request
     * @param invokeContext
     * @param timeoutMillis
     * @return
     * @throws RemotingException
     */
    public RpcResponseFuture invokeWithFuture(final Connection conn, final Object request,
                                              final InvokeContext invokeContext, int timeoutMillis)
                                                                                                   throws RemotingException {
        return this.rpcRemoting.invokeWithFuture(conn, request, invokeContext, timeoutMillis);
    }

    /**
     * Callback invocation using a string address, address format example - 127.0.0.1:12200?key1=value1&key2=value2 <br>
     * You can specify an implementation of {@link InvokeCallback} to get the result.
     * <p>
     * Notice:<br>
     *   <ol>
     *   <li><b>DO NOT modify the request object concurrently when this method is called.</b></li>
     *   <li>When do invocation, use the string address to find a available connection, if none then create one.</li> 
     *      <ul>
     *        <li>You can use {@link RpcConfigs#CONNECT_TIMEOUT_KEY} to specify connection timeout, time unit is milliseconds, e.g [127.0.0.1:12200?_CONNECTTIMEOUT=3000]
     *        <li>You can use {@link RpcConfigs#CONNECTION_NUM_KEY} to specify connection number for each ip and port, e.g [127.0.0.1:12200?_CONNECTIONNUM=30]
     *        <li>You can use {@link RpcConfigs#CONNECTION_WARMUP_KEY} to specify whether need warmup all connections for the first time you call this method, e.g [127.0.0.1:12200?_CONNECTIONWARMUP=false]
     *      </ul>
     *   <li>You should use {@link #closeConnection(String addr)} to close it if you want.
     *   </ol>
     * 
     * @param addr
     * @param request
     * @param invokeCallback
     * @param timeoutMillis
     * @throws RemotingException
     * @throws InterruptedException
     */
    public void invokeWithCallback(final String addr, final Object request,
                                   final InvokeCallback invokeCallback, final int timeoutMillis)
                                                                                                throws RemotingException,
                                                                                                InterruptedException {
        this.rpcRemoting.invokeWithCallback(addr, request, null, invokeCallback, timeoutMillis);
    }

    /**
     * Callback invocation with a {@link InvokeContext}, common api notice please see {@link #invokeWithCallback(String, Object, InvokeCallback, int)}
     *
     * @param addr
     * @param request
     * @param invokeContext
     * @param invokeCallback
     * @param timeoutMillis
     * @throws RemotingException
     * @throws InterruptedException
     */
    public void invokeWithCallback(final String addr, final Object request,
                                   final InvokeContext invokeContext,
                                   final InvokeCallback invokeCallback, final int timeoutMillis)
                                                                                                throws RemotingException,
                                                                                                InterruptedException {
        this.rpcRemoting.invokeWithCallback(addr, request, invokeContext, invokeCallback,
            timeoutMillis);
    }

    /**
     * Callback invocation using a parsed {@link Url} <br>
     * You can specify an implementation of {@link InvokeCallback} to get the result.
     * <p>
     * Notice:<br>
     *   <ol>
     *   <li><b>DO NOT modify the request object concurrently when this method is called.</b></li>
     *   <li>When do invocation, use the parsed {@link Url} to find a available connection, if none then create one.</li> 
     *      <ul>
     *        <li>You can use {@link Url#setConnectTimeout} to specify connection timeout, time unit is milliseconds.
     *        <li>You can use {@link Url#setConnNum} to specify connection number for each ip and port.
     *        <li>You can use {@link Url#setConnWarmup} to specify whether need warmup all connections for the first time you call this method.
     *      </ul>
     *   <li>You should use {@link #closeConnection(Url url)} to close it if you want.
     *   </ol>
     * 
     * @param url
     * @param request
     * @param invokeCallback
     * @param timeoutMillis
     * @throws RemotingException
     * @throws InterruptedException
     */
    public void invokeWithCallback(final Url url, final Object request,
                                   final InvokeCallback invokeCallback, final int timeoutMillis)
                                                                                                throws RemotingException,
                                                                                                InterruptedException {
        this.rpcRemoting.invokeWithCallback(url, request, null, invokeCallback, timeoutMillis);
    }

    /**
     * Callback invocation with a {@link InvokeContext}, common api notice please see {@link #invokeWithCallback(Url, Object, InvokeCallback, int)}
     *
     * @param url
     * @param request
     * @param invokeContext
     * @param invokeCallback
     * @param timeoutMillis
     * @throws RemotingException
     * @throws InterruptedException
     */
    public void invokeWithCallback(final Url url, final Object request,
                                   final InvokeContext invokeContext,
                                   final InvokeCallback invokeCallback, final int timeoutMillis)
                                                                                                throws RemotingException,
                                                                                                InterruptedException {
        this.rpcRemoting.invokeWithCallback(url, request, invokeContext, invokeCallback,
            timeoutMillis);
    }

    /**
     * Callback invocation using a {@link Connection} <br>
     * You can specify an implementation of {@link InvokeCallback} to get the result.
     * <p>
     * Notice:<br>
     *   <b>DO NOT modify the request object concurrently when this method is called.</b>
     * 
     * @param conn
     * @param request
     * @param invokeCallback
     * @param timeoutMillis
     * @throws RemotingException
     */
    public void invokeWithCallback(final Connection conn, final Object request,
                                   final InvokeCallback invokeCallback, final int timeoutMillis)
                                                                                                throws RemotingException {
        this.rpcRemoting.invokeWithCallback(conn, request, null, invokeCallback, timeoutMillis);
    }

    /**
     * Callback invocation with a {@link InvokeContext}, common api notice please see {@link #invokeWithCallback(Connection, Object, InvokeCallback, int)}
     *
     * @param conn
     * @param request
     * @param invokeContext
     * @param invokeCallback
     * @param timeoutMillis
     * @throws RemotingException
     */
    public void invokeWithCallback(final Connection conn, final Object request,
                                   final InvokeContext invokeContext,
                                   final InvokeCallback invokeCallback, final int timeoutMillis)
                                                                                                throws RemotingException {
        this.rpcRemoting.invokeWithCallback(conn, request, invokeContext, invokeCallback,
            timeoutMillis);
    }

    /**
     * Add processor to process connection event.
     * 
     * @param type
     * @param processor
     */
    public void addConnectionEventProcessor(ConnectionEventType type,
                                            ConnectionEventProcessor processor) {
        this.connectionEventListener.addConnectionEventProcessor(type, processor);
    }

    /**
     * Use UserProcessorRegisterHelper{@link UserProcessorRegisterHelper} to help register user processor for client side.
     *
     * @param processor
     * @throws RemotingException 
     */

    public void registerUserProcessor(UserProcessor<?> processor) {
        UserProcessorRegisterHelper.registerUserProcessor(processor, this.userProcessors);
    }

    /**
     * Create a stand alone connection using ip and port. <br>
     * <p>
     * Notice:<br>
     *   <li>Each time you call this method, will create a new connection.
     *   <li>Bolt will not control this connection.
     *   <li>You should use {@link #closeStandaloneConnection} to close it.
     * 
     * @param ip
     * @param port
     * @param connectTimeout
     * @return
     * @throws RemotingException
     */
    public Connection createStandaloneConnection(String ip, int port, int connectTimeout)
                                                                                         throws RemotingException {
        return this.connectionManager.create(ip, port, connectTimeout);
    }

    /**
     * Create a stand alone connection using address, address format example - 127.0.0.1:12200 <br>
     * <p>
     * Notice:<br>
     *   <ol>
     *   <li>Each time you can this method, will create a new connection.
     *   <li>Bolt will not control this connection.
     *   <li>You should use {@link #closeStandaloneConnection} to close it.
     *   </ol>
     * 
     * @param addr
     * @param connectTimeout
     * @return
     * @throws RemotingException
     */
    public Connection createStandaloneConnection(String addr, int connectTimeout)
                                                                                 throws RemotingException {
        return this.connectionManager.create(addr, connectTimeout);
    }

    /**
     * Close a standalone connection
     * 
     * @param conn
     */
    public void closeStandaloneConnection(Connection conn) {
        if (null != conn) {
            conn.close();
        }
    }

    /**
     * Get a connection using address, address format example - 127.0.0.1:12200?key1=value1&key2=value2 <br>
     * <p>
     * Notice:<br>
     *   <ol>
     *   <li>Get a connection, if none then create.</li> 
     *      <ul>
     *        <li>You can use {@link RpcConfigs#CONNECT_TIMEOUT_KEY} to specify connection timeout, time unit is milliseconds, e.g [127.0.0.1:12200?_CONNECTTIMEOUT=3000]
     *        <li>You can use {@link RpcConfigs#CONNECTION_NUM_KEY} to specify connection number for each ip and port, e.g [127.0.0.1:12200?_CONNECTIONNUM=30]
     *        <li>You can use {@link RpcConfigs#CONNECTION_WARMUP_KEY} to specify whether need warmup all connections for the first time you call this method, e.g [127.0.0.1:12200?_CONNECTIONWARMUP=false]
     *      </ul>
     *   <li>Bolt will control this connection in {@link ConnectionPool}
     *   <li>You should use {@link #closeConnection(String addr)} to close it.
     *   </ol>
     * @param addr
     * @param connectTimeout this is prior to url args {@link RpcConfigs#CONNECT_TIMEOUT_KEY}
     * @return
     * @throws RemotingException
     */
    public Connection getConnection(String addr, int connectTimeout) throws RemotingException,
                                                                    InterruptedException {
        Url url = this.addressParser.parse(addr);
        return this.getConnection(url, connectTimeout);
    }

    /**
     * Get a connection using a {@link Url}.<br>
     * <p> 
     * Notice: 
     *   <ol>
     *   <li>Get a connection, if none then create.
     *   <li>Bolt will control this connection in {@link com.alipay.remoting.ConnectionPool}
     *   <li>You should use {@link #closeConnection(Url url)} to close it.
     *   </ol>
     * 
     * @param url
     * @param connectTimeout this is prior to url args {@link RpcConfigs#CONNECT_TIMEOUT_KEY}
     * @return
     * @throws RemotingException
     */
    public Connection getConnection(Url url, int connectTimeout) throws RemotingException,
                                                                InterruptedException {
        url.setConnectTimeout(connectTimeout);
        return this.connectionManager.getAndCreateIfAbsent(url);
    }

    /**
     * get all connections managed by rpc client
     *
     * @return map key is ip+port, value is a list of connections of this key.
     */
    public Map<String, List<Connection>> getAllManagedConnections() {
        return this.connectionManager.getAll();
    }

    /**
     * check connection, the address format example - 127.0.0.1:12200?key1=value1&key2=value2
     *
     * @param addr
     * @throws RemotingException
     * @return true if and only if there is a connection, and the connection is active and writable;else return false
     */
    public boolean checkConnection(String addr) {
        Url url = this.addressParser.parse(addr);
        Connection conn = this.connectionManager.get(url.getUniqueKey());
        try {
            this.connectionManager.check(conn);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * Close all connections of a address
     * 
     * @param addr
     */
    public void closeConnection(String addr) {
        Url url = this.addressParser.parse(addr);
        this.connectionManager.remove(url.getUniqueKey());
    }

    /**
     * Close all connections of a {@link Url}
     * 
     * @param url
     */
    public void closeConnection(Url url) {
        this.connectionManager.remove(url.getUniqueKey());
    }

    /**
     * Enable heart beat for a certain connection. 
     * If this address not connected, then do nothing.
     * <p>
     * Notice: this method takes no effect on a stand alone connection.
     * 
     * @param addr
     */
    public void enableConnHeartbeat(String addr) {
        Url url = this.addressParser.parse(addr);
        this.enableConnHeartbeat(url);
    }

    /**
     * Enable heart beat for a certain connection. 
     * If this {@link Url} not connected, then do nothing.
     * <p>
     * Notice: this method takes no effect on a stand alone connection.
     * 
     * @param url
     */
    public void enableConnHeartbeat(Url url) {
        if (null != url) {
            this.connectionManager.enableHeartbeat(this.connectionManager.get(url.getUniqueKey()));
        }
    }

    /**
     * Disable heart beat for a certain connection. 
     * If this addr not connected, then do nothing.
     * <p>
     * Notice: this method takes no effect on a stand alone connection.
     * 
     * @param addr
     */
    public void disableConnHeartbeat(String addr) {
        Url url = this.addressParser.parse(addr);
        this.disableConnHeartbeat(url);
    }

    /**
     * Disable heart beat for a certain connection. 
     * If this {@link Url} not connected, then do nothing.
     * <p>
     * Notice: this method takes no effect on a stand alone connection.
     * 
     * @param url
     */
    public void disableConnHeartbeat(Url url) {
        if (null != url) {
            this.connectionManager.disableHeartbeat(this.connectionManager.get(url.getUniqueKey()));
        }
    }

    /**
     * enable connection reconnect switch on
     * <p>
     * Notice: This api should be called before {@link RpcClient#init()}
     */
    public void enableReconnectSwitch() {
        this.switches().turnOn(GlobalSwitch.CONN_RECONNECT_SWITCH);
    }

    /**
     * disable connection reconnect switch off
     * <p>
     * Notice: This api should be called before {@link RpcClient#init()}
     */
    public void disableReconnectSwith() {
        this.switches().turnOff(GlobalSwitch.CONN_RECONNECT_SWITCH);
    }

    /**
     * is reconnect switch on
     * @return
     */
    public boolean isReconnectSwitchOn() {
        return this.switches().isOn(GlobalSwitch.CONN_RECONNECT_SWITCH);
    }

    /**
     * enable connection monitor switch on
     */
    public void enableConnectionMonitorSwitch() {
        this.switches().turnOn(GlobalSwitch.CONN_MONITOR_SWITCH);
    }

    /**
     * disable connection monitor switch off
     * <p>
     * Notice: This api should be called before {@link RpcClient#init()}
     */
    public void disableConnectionMonitorSwitch() {
        this.switches().turnOff(GlobalSwitch.CONN_MONITOR_SWITCH);
    }

    /**
     * is connection monitor switch on
     * @return
     */
    public boolean isConnectionMonitorSwitchOn() {
        return this.switches().isOn(GlobalSwitch.CONN_MONITOR_SWITCH);
    }

    // ~~~ getter and setter

    protected DefaultConnectionManager getConnectionManager() {
        return this.connectionManager;
    }

    /**
     * Getter method for property <tt>addressParser</tt>.
     * 
     * @return property value of addressParser
     */
    public RemotingAddressParser getAddressParser() {
        return this.addressParser;
    }

    /**
     * Setter method for property <tt>addressParser</tt>.
     * 
     * @param addressParser value to be assigned to property addressParser
     */
    public void setAddressParser(RemotingAddressParser addressParser) {
        this.addressParser = addressParser;
    }

    /**
     * Setter method for property <tt>monitorStrategy<tt>.
     *
     * @param monitorStrategy value to be assigned to property monitorStrategy
     */
    public void setMonitorStrategy(ConnectionMonitorStrategy monitorStrategy) {
        this.monitorStrategy = monitorStrategy;
    }
}
