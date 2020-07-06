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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;

import com.alipay.remoting.log.BoltLoggerFactory;

/**
 * Connection pool
 * 
 * @author xiaomin.cxm
 * @version $Id: ConnectionPool.java, v 0.1 Mar 8, 2016 11:04:54 AM xiaomin.cxm Exp $
 */
public class ConnectionPool implements Scannable {

    private static final Logger              logger = BoltLoggerFactory.getLogger("CommonDefault");

    private CopyOnWriteArrayList<Connection> connections;
    private ConnectionSelectStrategy         strategy;
    private volatile long                    lastAccessTimestamp;
    private volatile boolean                 asyncCreationDone;

    /**
     * Constructor
     * 
     * @param strategy ConnectionSelectStrategy
     */
    public ConnectionPool(ConnectionSelectStrategy strategy) {
        this.strategy = strategy;
        this.connections = new CopyOnWriteArrayList<Connection>();
        this.lastAccessTimestamp = System.currentTimeMillis();
        this.asyncCreationDone = true;
    }

    /**
     * add a connection
     * 
     * @param connection Connection
     */
    public void add(Connection connection) {
        markAccess();
        if (null == connection) {
            return;
        }
        boolean res = connections.addIfAbsent(connection);
        if (res) {
            connection.increaseRef();
        }
    }

    /**
     * check weather a connection already added
     * 
     * @param connection Connection
     * @return whether this pool contains the target connection
     */
    public boolean contains(Connection connection) {
        return connections.contains(connection);
    }

    /**
     * removeAndTryClose a connection
     * 
     * @param connection Connection
     */
    public void removeAndTryClose(Connection connection) {
        if (null == connection) {
            return;
        }
        boolean res = connections.remove(connection);
        if (res) {
            connection.decreaseRef();
        }
        if (connection.noRef()) {
            connection.close();
        }
    }

    /**
     * remove all connections
     */
    public void removeAllAndTryClose() {
        for (Connection conn : connections) {
            removeAndTryClose(conn);
        }
        connections.clear();
    }

    /**
     * get a connection
     * 
     * @return Connection
     */
    public Connection get() {
        if (null != connections) {
            List<Connection> snapshot = new ArrayList<Connection>(connections);
            if (snapshot.size() > 0) {
                return strategy.select(snapshot);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * get all connections
     * 
     * @return Connection List
     */
    public List<Connection> getAll() {
        return new ArrayList<Connection>(connections);
    }

    /**
     * connection pool size
     *
     * @return pool size
     */
    public int size() {
        return connections.size();
    }

    /**
     * is connection pool empty
     *
     * @return true if this connection pool has no connection
     */
    public boolean isEmpty() {
        return connections.isEmpty();
    }

    /**
     * Getter method for property <tt>lastAccessTimestamp</tt>.
     *
     * @return property value of lastAccessTimestamp
     */
    public long getLastAccessTimestamp() {
        return lastAccessTimestamp;
    }

    /**
     * do mark the time stamp when access this pool
     */
    private void markAccess() {
        lastAccessTimestamp = System.currentTimeMillis();
    }

    /**
     * is async create connection done
     * @return true if async create connection done
     */
    public boolean isAsyncCreationDone() {
        return asyncCreationDone;
    }

    /**
     * do mark async create connection done
     */
    public void markAsyncCreationDone() {
        asyncCreationDone = true;
    }

    /**
     * do mark async create connection start
     */
    public void markAsyncCreationStart() {
        asyncCreationDone = false;
    }

    @Override
    public void scan() {
        if (null != connections && !connections.isEmpty()) {
            for (Connection conn : connections) {
                if (!conn.isFine()) {
                    logger.warn(
                        "Remove bad connection when scanning conns of ConnectionPool - {}:{}",
                        conn.getRemoteIP(), conn.getRemotePort());
                    conn.close();
                    removeAndTryClose(conn);
                }
            }
        }
    }
}
