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

import com.alipay.remoting.config.Configuration;
import com.alipay.remoting.exception.RemotingException;
import com.alipay.remoting.rpc.RpcClient;
import com.alipay.remoting.rpc.RpcConfigs;
import com.alipay.remoting.rpc.RpcResponseFuture;
import com.alipay.remoting.rpc.protocol.UserProcessor;
import com.alipay.remoting.rpc.protocol.UserProcessorRegisterHelper;

import java.util.List;
import java.util.Map;

/**
 * Bolt client interface.
 *
 * @author chengyi (mark.lx@antfin.com) 2018-11-07 11:56
 */
public interface BoltClient extends Configuration, LifeCycle {

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
     * @param address target address
     * @param request request
     */
    void oneway(final String address, final Object request) throws RemotingException,
                                                           InterruptedException;

    /**
     * Oneway invocation with a {@link InvokeContext}, common api notice please see {@link #oneway(Connection, Object)}
     *
     * @param address target address
     * @param request request
     * @param invokeContext invoke context
     */
    void oneway(final String address, final Object request, final InvokeContext invokeContext)
                                                                                              throws RemotingException,
                                                                                              InterruptedException;

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
     * @param url target url
     * @param request object
     */
    void oneway(final Url url, final Object request) throws RemotingException, InterruptedException;

    /**
     * Oneway invocation with a {@link InvokeContext}, common api notice please see {@link #oneway(Url, Object)}
     *
     * @param url url
     * @param request request
     * @param invokeContext invoke context
     */
    void oneway(final Url url, final Object request, final InvokeContext invokeContext)
                                                                                       throws RemotingException,
                                                                                       InterruptedException;

    /**
     * One way invocation using a {@link Connection} <br>
     * <p>
     * Notice:<br>
     *   <b>DO NOT modify the request object concurrently when this method is called.</b>
     *
     * @param conn target connection
     * @param request request
     */
    void oneway(final Connection conn, final Object request) throws RemotingException;

    /**
     * Oneway invocation with a {@link InvokeContext}, common api notice please see {@link #oneway(Connection, Object)}
     *
     * @param conn target connection
     * @param request request
     * @param invokeContext invoke context
     */
    void oneway(final Connection conn, final Object request, final InvokeContext invokeContext)
                                                                                               throws RemotingException;

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
     * @param address target address
     * @param request request
     * @param timeoutMillis timeout millisecond
     * @return result object
     */
    Object invokeSync(final String address, final Object request, final int timeoutMillis)
                                                                                          throws RemotingException,
                                                                                          InterruptedException;

    /**
     * Synchronous invocation with a {@link InvokeContext}, common api notice please see {@link #invokeSync(String, Object, int)}
     *
     * @param address target address
     * @param request request
     * @param invokeContext invoke context
     * @param timeoutMillis timeout in millisecond
     * @return result object
     */
    Object invokeSync(final String address, final Object request,
                      final InvokeContext invokeContext, final int timeoutMillis)
                                                                                 throws RemotingException,
                                                                                 InterruptedException;

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
     * @param url target url
     * @param request request
     * @param timeoutMillis timeout in millisecond
     * @return Object
     */
    Object invokeSync(final Url url, final Object request, final int timeoutMillis)
                                                                                   throws RemotingException,
                                                                                   InterruptedException;

    /**
     * Synchronous invocation with a {@link InvokeContext}, common api notice please see {@link #invokeSync(Url, Object, int)}
     *
     * @param url target url
     * @param request request
     * @param invokeContext invoke context
     * @param timeoutMillis timeout in millisecond
     * @return result object
     */
    Object invokeSync(final Url url, final Object request, final InvokeContext invokeContext,
                      final int timeoutMillis) throws RemotingException, InterruptedException;

    /**
     * Synchronous invocation using a {@link Connection} <br>
     * <p>
     * Notice:<br>
     *   <b>DO NOT modify the request object concurrently when this method is called.</b>
     *
     * @param conn target connection
     * @param request request
     * @param timeoutMillis timeout in millisecond
     * @return Object
     */
    Object invokeSync(final Connection conn, final Object request, final int timeoutMillis)
                                                                                           throws RemotingException,
                                                                                           InterruptedException;

    /**
     * Synchronous invocation with a {@link InvokeContext}, common api notice please see {@link #invokeSync(Connection, Object, int)}
     *
     * @param conn target connection
     * @param request request
     * @param invokeContext invoke context
     * @param timeoutMillis timeout in millis
     * @return Object
     */
    Object invokeSync(final Connection conn, final Object request,
                      final InvokeContext invokeContext, final int timeoutMillis)
                                                                                 throws RemotingException,
                                                                                 InterruptedException;

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
     * @param address target address
     * @param request request
     * @param timeoutMillis timeout in millisecond
     * @return RpcResponseFuture
     */
    RpcResponseFuture invokeWithFuture(final String address, final Object request,
                                       final int timeoutMillis) throws RemotingException,
                                                               InterruptedException;

    /**
     * Future invocation with a {@link InvokeContext}, common api notice please see {@link #invokeWithFuture(String, Object, int)}
     *
     * @param address target address
     * @param request request
     * @param invokeContext invoke context
     * @param timeoutMillis timeout in millisecond
     * @return RpcResponseFuture
     */
    RpcResponseFuture invokeWithFuture(final String address, final Object request,
                                       final InvokeContext invokeContext, final int timeoutMillis)
                                                                                                  throws RemotingException,
                                                                                                  InterruptedException;

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
     * @param url target url
     * @param request request
     * @param timeoutMillis timeout in millisecond
     * @return RpcResponseFuture
     */
    RpcResponseFuture invokeWithFuture(final Url url, final Object request, final int timeoutMillis)
                                                                                                    throws RemotingException,
                                                                                                    InterruptedException;

    /**
     * Future invocation with a {@link InvokeContext}, common api notice please see {@link #invokeWithFuture(Url, Object, int)}
     *
     * @param url target url
     * @param request request
     * @param invokeContext invoke context
     * @param timeoutMillis timeout in millis
     * @return RpcResponseFuture
     */
    RpcResponseFuture invokeWithFuture(final Url url, final Object request,
                                       final InvokeContext invokeContext, final int timeoutMillis)
                                                                                                  throws RemotingException,
                                                                                                  InterruptedException;

    /**
     * Future invocation using a {@link Connection} <br>
     * You can get result use the returned {@link RpcResponseFuture}.
     * <p>
     * Notice:<br>
     *   <b>DO NOT modify the request object concurrently when this method is called.</b>
     *
     * @param conn target connection
     * @param request request
     * @param timeoutMillis timeout in millis
     * @return RpcResponseFuture
     */
    RpcResponseFuture invokeWithFuture(final Connection conn, final Object request,
                                       int timeoutMillis) throws RemotingException;

    /**
     * Future invocation with a {@link InvokeContext}, common api notice please see {@link #invokeWithFuture(Connection, Object, int)}
     *
     * @param conn target connection
     * @param request request
     * @param invokeContext context
     * @param timeoutMillis timeout millisecond
     * @return RpcResponseFuture
     */
    RpcResponseFuture invokeWithFuture(final Connection conn, final Object request,
                                       final InvokeContext invokeContext, int timeoutMillis)
                                                                                            throws RemotingException;

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
     * @param addr target address
     * @param request request
     * @param invokeCallback callback
     * @param timeoutMillis timeout in millisecond
     */
    void invokeWithCallback(final String addr, final Object request,
                            final InvokeCallback invokeCallback, final int timeoutMillis)
                                                                                         throws RemotingException,
                                                                                         InterruptedException;

    /**
     * Callback invocation with a {@link InvokeContext}, common api notice please see {@link #invokeWithCallback(String, Object, InvokeCallback, int)}
     *
     * @param addr target address
     * @param request request
     * @param invokeContext context
     * @param invokeCallback callback
     * @param timeoutMillis timeout in millisecond
     */
    void invokeWithCallback(final String addr, final Object request,
                            final InvokeContext invokeContext, final InvokeCallback invokeCallback,
                            final int timeoutMillis) throws RemotingException, InterruptedException;

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
     * @param url target url
     * @param request request
     * @param invokeCallback callback
     * @param timeoutMillis timeout in millisecond
     */
    void invokeWithCallback(final Url url, final Object request,
                            final InvokeCallback invokeCallback, final int timeoutMillis)
                                                                                         throws RemotingException,
                                                                                         InterruptedException;

    /**
     * Callback invocation with a {@link InvokeContext}, common api notice please see {@link #invokeWithCallback(Url, Object, InvokeCallback, int)}
     *
     * @param url target url
     * @param request request
     * @param invokeContext context
     * @param invokeCallback callback
     * @param timeoutMillis timeout in millisecond
     */
    void invokeWithCallback(final Url url, final Object request, final InvokeContext invokeContext,
                            final InvokeCallback invokeCallback, final int timeoutMillis)
                                                                                         throws RemotingException,
                                                                                         InterruptedException;

    /**
     * Callback invocation using a {@link Connection} <br>
     * You can specify an implementation of {@link InvokeCallback} to get the result.
     * <p>
     * Notice:<br>
     *   <b>DO NOT modify the request object concurrently when this method is called.</b>
     *
     * @param conn target connection
     * @param request request
     * @param invokeCallback callback
     * @param timeoutMillis timeout in millisecond
     */
    void invokeWithCallback(final Connection conn, final Object request,
                            final InvokeCallback invokeCallback, final int timeoutMillis)
                                                                                         throws RemotingException;

    /**
     * Callback invocation with a {@link InvokeContext}, common api notice please see {@link #invokeWithCallback(Connection, Object, InvokeCallback, int)}
     *
     * @param conn target connection
     * @param request request
     * @param invokeContext invoke context
     * @param invokeCallback callback
     * @param timeoutMillis timeout in millisecond
     */
    void invokeWithCallback(final Connection conn, final Object request,
                            final InvokeContext invokeContext, final InvokeCallback invokeCallback,
                            final int timeoutMillis) throws RemotingException;

    /**
     * Add processor to process connection event.
     *
     * @param type connection event type
     * @param processor connection event process
     */
    void addConnectionEventProcessor(ConnectionEventType type, ConnectionEventProcessor processor);

    /**
     * Use UserProcessorRegisterHelper{@link UserProcessorRegisterHelper} to help register user processor for client side.
     */
    void registerUserProcessor(UserProcessor<?> processor);

    /**
     * Create a stand alone connection using ip and port. <br>
     * <p>
     * Notice:<br>
     *   <li>Each time you call this method, will create a new connection.
     *   <li>Bolt will not control this connection.
     *   <li>You should use {@link #closeStandaloneConnection} to close it.
     *
     * @param ip ip
     * @param port port
     * @param connectTimeout timeout in millisecond
     */
    Connection createStandaloneConnection(String ip, int port, int connectTimeout)
                                                                                  throws RemotingException;

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
     * @param address target address
     * @param connectTimeout timeout in millisecond
     */
    Connection createStandaloneConnection(String address, int connectTimeout)
                                                                             throws RemotingException;

    /**
     * Close a standalone connection
     *
     * @param conn target connection
     */
    void closeStandaloneConnection(Connection conn);

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
     * @param addr target address
     * @param connectTimeout this is prior to url args {@link RpcConfigs#CONNECT_TIMEOUT_KEY}
     */
    Connection getConnection(String addr, int connectTimeout) throws RemotingException,
                                                             InterruptedException;

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
     * @param url target url
     * @param connectTimeout this is prior to url args {@link RpcConfigs#CONNECT_TIMEOUT_KEY}
     */
    Connection getConnection(Url url, int connectTimeout) throws RemotingException,
                                                         InterruptedException;

    /**
     * get all connections managed by rpc client
     *
     * @return map key is ip+port, value is a list of connections of this key.
     */
    Map<String, List<Connection>> getAllManagedConnections();

    /**
     * check connection, the address format example - 127.0.0.1:12200?key1=value1&key2=value2
     *
     * @param address target address
     * @return true if and only if there is a connection, and the connection is active and writable;else return false
     */
    boolean checkConnection(String address);

    /**
     * check connection, the address format example - 127.0.0.1:12200?key1=value1&key2=value2
     * and if there is no connection, try create new one
     *
     * @param address target address
     * @param createIfAbsent if need to create
     * @return true if and only if there is a connection, and the connection is active and writable, or can create a new one
     */
    boolean checkConnection(String address, boolean createIfAbsent);

    /**
     * check connection, the address format example - 127.0.0.1:12200?key1=value1&key2=value2
     * and if there is no connection, try create new one in asynchronous
     *
     * @param address target address
     * @param createIfAbsent if need to create
     * @param createAsync create connection asynchronous
     * @return true if and only if there is a connection, and the connection is active and writable
     */
    boolean checkConnection(String address, boolean createIfAbsent, boolean createAsync);

    /**
     * Close all connections of a address
     *
     * @param address target address
     */
    void closeConnection(String address);

    /**
     * Close all connections of a {@link Url}
     *
     * @param url target url
     */
    void closeConnection(Url url);

    /**
     * Enable heart beat for a certain connection.
     * If this address not connected, then do nothing.
     * <p>
     * Notice: this method takes no effect on a stand alone connection.
     *
     * @param address target address
     */
    void enableConnHeartbeat(String address);

    /**
     * Enable heart beat for a certain connection.
     * If this {@link Url} not connected, then do nothing.
     * <p>
     * Notice: this method takes no effect on a stand alone connection.
     *
     * @param url target url
     */
    void enableConnHeartbeat(Url url);

    /**
     * Disable heart beat for a certain connection.
     * If this addr not connected, then do nothing.
     * <p>
     * Notice: this method takes no effect on a stand alone connection.
     *
     * @param address target address
     */
    void disableConnHeartbeat(String address);

    /**
     * Disable heart beat for a certain connection.
     * If this {@link Url} not connected, then do nothing.
     * <p>
     * Notice: this method takes no effect on a stand alone connection.
     *
     * @param url target url
     */
    void disableConnHeartbeat(Url url);

    /**
     * enable connection reconnect switch on
     * <p>
     * Notice: This api should be called before {@link RpcClient#init()}
     */
    void enableReconnectSwitch();

    /**
     * disable connection reconnect switch off
     * <p>
     * Notice: This api should be called before {@link RpcClient#init()}
     */
    void disableReconnectSwith();

    /**
     * is reconnect switch on
     */
    boolean isReconnectSwitchOn();

    /**
     * enable connection monitor switch on
     */
    void enableConnectionMonitorSwitch();

    /**
     * disable connection monitor switch off
     * <p>
     * Notice: This api should be called before {@link RpcClient#init()}
     */
    void disableConnectionMonitorSwitch();

    /**
     * is connection monitor switch on
     */
    boolean isConnectionMonitorSwitchOn();

    /**
     * Getter method for property <tt>connectionManager</tt>.
     *
     * @return property value of connectionManager
     */
    ConnectionManager getConnectionManager();

    /**
     * Setter method for property <tt>connectionManager</tt>.
     *
     * @param connectionManager ConnectionManager
     */
    void setConnectionManager(ConnectionManager connectionManager);

    /**
     * Getter method for property <tt>addressParser</tt>.
     *
     * @return property value of addressParser
     */
    RemotingAddressParser getAddressParser();

    /**
     * Setter method for property <tt>addressParser</tt>.
     *
     * @param addressParser value to be assigned to property addressParser
     */
    void setAddressParser(RemotingAddressParser addressParser);

    /**
     * Setter method for property <tt>monitorStrategy<tt>.
     *
     * @param monitorStrategy value to be assigned to property monitorStrategy
     */
    void setMonitorStrategy(ConnectionMonitorStrategy monitorStrategy);
}
