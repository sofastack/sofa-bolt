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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;

import com.alipay.remoting.log.BoltLoggerFactory;

/**
 * Reconnect manager.
 *
 * @author yunliang.shi
 * @version $Id: ReconnectManager.java, v 0.1 Mar 11, 2016 5:20:50 PM yunliang.shi Exp $
 */
public class ReconnectManager extends AbstractLifeCycle implements Reconnector {

    private static final Logger                      logger                   = BoltLoggerFactory
                                                                                  .getLogger("CommonDefault");

    private static final int                         HEAL_CONNECTION_INTERVAL = 1000;

    private final ConnectionManager                  connectionManager;
    private final LinkedBlockingQueue<ReconnectTask> tasks;
    private final List<Url>                          canceled;

    private Thread                                   healConnectionThreads;

    public ReconnectManager(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
        this.tasks = new LinkedBlockingQueue<ReconnectTask>();
        this.canceled = new CopyOnWriteArrayList<Url>();
        // call startup in the constructor to be compatible with version 1.5.x
        startup();
    }

    @Override
    public void reconnect(Url url) {
        ensureStarted();
        tasks.add(new ReconnectTask(url));
    }

    @Override
    public void disableReconnect(Url url) {
        ensureStarted();
        canceled.add(url);
    }

    @Override
    public void enableReconnect(Url url) {
        ensureStarted();
        canceled.remove(url);
    }

    @Override
    public void startup() throws LifeCycleException {
        // make the startup method idempotent to be compatible with version 1.5.x
        synchronized (this) {
            if (!isStarted()) {
                super.startup();

                this.healConnectionThreads = new Thread(new HealConnectionRunner());
                this.healConnectionThreads.setName("Bolt-heal-connection-thread");
                this.healConnectionThreads.start();
            }
        }
    }

    @Override
    public void shutdown() throws LifeCycleException {
        super.shutdown();

        healConnectionThreads.interrupt();
        this.tasks.clear();
        this.canceled.clear();
    }

    /**
     * please use {@link Reconnector#disableReconnect(Url)} instead
     */
    @Deprecated
    public void addCancelUrl(Url url) {
        ensureStarted();
        disableReconnect(url);
    }

    /**
     * please use {@link Reconnector#enableReconnect(Url)} instead
     */
    @Deprecated
    public void removeCancelUrl(Url url) {
        ensureStarted();
        enableReconnect(url);
    }

    /**
     * please use {@link Reconnector#reconnect(Url)} instead
     */
    @Deprecated
    public void addReconnectTask(Url url) {
        ensureStarted();
        reconnect(url);
    }

    /**
     * please use {@link Reconnector#shutdown()} instead
     */
    @Deprecated
    public void stop() {
        shutdown();
    }

    private final class HealConnectionRunner implements Runnable {
        private long lastConnectTime = -1;

        @Override
        public void run() {
            while (isStarted()) {
                long start = -1;
                ReconnectTask task = null;
                try {
                    if (this.lastConnectTime < HEAL_CONNECTION_INTERVAL) {
                        Thread.sleep(HEAL_CONNECTION_INTERVAL);
                    }

                    try {
                        task = ReconnectManager.this.tasks.take();
                    } catch (InterruptedException e) {
                        // ignore
                    }

                    if (task == null) {
                        continue;
                    }

                    start = System.currentTimeMillis();
                    if (!canceled.contains(task.url)) {
                        task.run();
                    } else {
                        logger.warn("Invalid reconnect request task {}, cancel list size {}",
                            task.url, canceled.size());
                    }
                    this.lastConnectTime = System.currentTimeMillis() - start;
                } catch (Exception e) {
                    if (start != -1) {
                        this.lastConnectTime = System.currentTimeMillis() - start;
                    }

                    if (task != null) {
                        logger.warn("reconnect target: {} failed.", task.url, e);
                        tasks.add(task);
                    }
                }
            }
        }
    }

    private class ReconnectTask implements Runnable {
        Url url;

        public ReconnectTask(Url url) {
            this.url = url;
        }

        @Override
        public void run() {
            try {
                connectionManager.createConnectionAndHealIfNeed(url);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
