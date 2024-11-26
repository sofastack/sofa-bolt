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

import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;

import com.alipay.remoting.config.ConfigManager;
import com.alipay.remoting.log.BoltLoggerFactory;
import com.alipay.remoting.util.RunStateRecordedFutureTask;

/**
 *  A default connection monitor that handle connections with strategies
 *
 * @author tsui
 * @version $Id: DefaultConnectionMonitor.java, v 0.1 2017-02-21 12:09 tsui Exp $
 */
public class DefaultConnectionMonitor extends AbstractLifeCycle {

    private static final Logger             logger = BoltLoggerFactory.getLogger("CommonDefault");

    private final ConnectionManager         connectionManager;
    private final ConnectionMonitorStrategy strategy;

    private ScheduledThreadPoolExecutor     executor;

    public DefaultConnectionMonitor(ConnectionMonitorStrategy strategy,
                                    ConnectionManager connectionManager) {
        if (strategy == null) {
            throw new IllegalArgumentException("null strategy");
        }

        if (connectionManager == null) {
            throw new IllegalArgumentException("null connectionManager");
        }

        this.strategy = strategy;
        this.connectionManager = connectionManager;
    }

    @Override
    public void startup() throws LifeCycleException {
        super.startup();

        /* initial delay to execute schedule task, unit: ms */
        long initialDelay = ConfigManager.conn_monitor_initial_delay();

        /* period of schedule task, unit: ms*/
        long period = ConfigManager.conn_monitor_period();

        this.executor = new ScheduledThreadPoolExecutor(1, new NamedThreadFactory(
            "ConnectionMonitorThread", true), new ThreadPoolExecutor.AbortPolicy());
        this.executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    Map<String, RunStateRecordedFutureTask<ConnectionPool>> connPools = null;
                    if (connectionManager instanceof DefaultConnectionManager) {
                        connPools = ((DefaultConnectionManager) connectionManager).getConnPools();
                    }
                    strategy.monitor(connPools);
                } catch (Exception e) {
                    logger.warn("MonitorTask error", e);
                }
            }
        }, initialDelay, period, TimeUnit.MILLISECONDS);
    }

    @Override
    public void shutdown() throws LifeCycleException {
        super.shutdown();

        executor.purge();
        executor.shutdown();
    }

    /**
     * Start schedule task
     * please use {@link DefaultConnectionMonitor#startup()} instead
     */
    @Deprecated
    public void start() {
        startup();
    }

    /**
     * cancel task and shutdown executor
     * please use {@link DefaultConnectionMonitor#shutdown()} instead
     */
    @Deprecated
    public void destroy() {
        shutdown();
    }
}
