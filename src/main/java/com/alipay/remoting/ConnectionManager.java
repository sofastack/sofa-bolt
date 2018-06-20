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

import java.util.List;
import java.util.Map;

import com.alipay.remoting.exception.RemotingException;

/**
 * Connection manager of connection pool
 * 
 * @author xiaomin.cxm
 * @version $Id: ConnectionManager.java, v 0.1 Mar 7, 2016 2:42:46 PM xiaomin.cxm Exp $
 */
public interface ConnectionManager extends Scannable {
    /**
     * init
     */
    void init();

    /**
     * Add a connection to {@link ConnectionPool}.
     * If it contains multiple pool keys, this connection will be added to multiple {@link ConnectionPool} too.
     * 
     * @param connection an available connection, you should {@link #check(Connection)} this connection before add
     */
    void add(Connection connection);

    /**
     * Add a connection to {@link ConnectionPool} with the specified poolKey.
     * 
     * @param connection an available connection, you should {@link #check(Connection)} this connection before add
     * @param poolKey unique key of a {@link ConnectionPool}
     */
    void add(Connection connection, String poolKey);

    /**
     * Get a connection from {@link ConnectionPool} with the specified poolKey.
     *
     * @param poolKey unique key of a {@link ConnectionPool}
     * @return a {@link Connection} selected by {@link ConnectionSelectStrategy}<br>
     *   or return {@code null} if there is no {@link ConnectionPool} mapping with poolKey<br>
     *   or return {@code null} if there is no {@link Connection} in {@link ConnectionPool}.
     */
    Connection get(String poolKey);

    /**
     * Get all connections from {@link ConnectionPool} with the specified poolKey.
     * 
     * @param poolKey unique key of a {@link ConnectionPool}
     * @return a list of {@link Connection}<br>
     *   or return an empty list if there is no {@link ConnectionPool} mapping with poolKey.
     */
    List<Connection> getAll(String poolKey);

    /**
     * Get all connections of all poolKey.
     *
     * @return a map with poolKey as key and a list of connections in ConnectionPool as value
     */
    Map<String, List<Connection>> getAll();

    /**
     * Remove a {@link Connection} from all {@link ConnectionPool} with the poolKeys in {@link Connection}, and close it.
     */
    void remove(Connection connection);

    /**
     * Remove and close a {@link Connection} from {@link ConnectionPool} with the specified poolKey.
     * 
     * @param connection target connection
     * @param poolKey unique key of a {@link ConnectionPool}
     */
    void remove(Connection connection, String poolKey);

    /**
     * Remove and close all connections from {@link ConnectionPool} with the specified poolKey.
     * 
     * @param poolKey unique key of a {@link ConnectionPool}
     */
    void remove(String poolKey);

    /**
     * Remove and close all connections from all {@link ConnectionPool}.
     */
    void removeAll();

    /**
     * check a connection whether available, if not, throw RemotingException
     * 
     * @param connection target connection
     */
    void check(Connection connection) throws RemotingException;

    /**
     * Get the number of {@link Connection} in {@link ConnectionPool} with the specified pool key
     * 
     * @param poolKey unique key of a {@link ConnectionPool}
     * @return connection count
     */
    int count(String poolKey);

    /**
     * Get a connection using {@link Url}, if {@code null} then create and add into {@link ConnectionPool}.
     * The connection number of {@link ConnectionPool} is decided by {@link Url#getConnNum()}
     * 
     * @param url {@link Url} contains connect infos.
     * @return the created {@link Connection}.
     * @throws InterruptedException if interrupted
     * @throws RemotingException if create failed.
     */
    Connection getAndCreateIfAbsent(Url url) throws InterruptedException, RemotingException;

    /**
     * This method can create connection pool with connections initialized and check the number of connections.
     * The connection number of {@link ConnectionPool} is decided by {@link Url#getConnNum()}.
     * Each time call this method, will check the number of connection, if not enough, this will do the healing logic additionally.
     */
    void createConnectionAndHealIfNeed(Url url) throws InterruptedException, RemotingException;

    // ~~~ create operation

    /**
     * Create a connection using specified {@link Url}.
     * 
     * @param url {@link Url} contains connect infos.
     */
    Connection create(Url url) throws RemotingException;

    /**
     * Create a connection using specified {@link String} address.
     * 
     * @param address a {@link String} address, e.g. 127.0.0.1:1111
     * @param connectTimeout an int connect timeout value
     * @return the created {@link Connection}
     * @throws RemotingException if create failed
     */
    Connection create(String address, int connectTimeout) throws RemotingException;

    /**
     * Create a connection using specified ip and port.
     * 
     * @param ip connect ip, e.g. 127.0.0.1
     * @param port connect port, e.g. 1111
     * @param connectTimeout an int connect timeout value
     * @return the created {@link Connection}
     */
    Connection create(String ip, int port, int connectTimeout) throws RemotingException;
}
