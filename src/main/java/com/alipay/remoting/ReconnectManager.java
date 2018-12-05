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

import com.alipay.remoting.exception.RemotingException;
import com.alipay.remoting.log.BoltLoggerFactory;

/**
 * Reconnect manager.
 * 
 * @author yunliang.shi
 * @version $Id: ReconnectManager.java, v 0.1 Mar 11, 2016 5:20:50 PM yunliang.shi Exp $
 */
public class ReconnectManager {
    private static final Logger logger = BoltLoggerFactory.getLogger("CommonDefault");

    class ReconnectTask {
        Url url;
    }

    private final LinkedBlockingQueue<ReconnectTask> tasks                  = new LinkedBlockingQueue<ReconnectTask>();

    protected final List<Url/* url */>              canceled               = new CopyOnWriteArrayList<Url>();
    private volatile boolean                         started;

    private int                                      healConnectionInterval = 1000;

    private final Thread                             healConnectionThreads;

    private ConnectionManager                        connectionManager;

    public ReconnectManager(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
        this.healConnectionThreads = new Thread(new HealConnectionRunner());
        this.healConnectionThreads.start();
        this.started = true;
    }

    private void doReconnectTask(ReconnectTask task) throws InterruptedException, RemotingException {
        connectionManager.createConnectionAndHealIfNeed(task.url);
    }

    private void addReconnectTask(ReconnectTask task) {
        tasks.add(task);
    }

    public void addCancelUrl(Url url) {
        canceled.add(url);
    }

    public void removeCancelUrl(Url url) {
        canceled.remove(url);
    }

    /**
     * add reconnect task
     * 
     * @param url
     */
    public void addReconnectTask(Url url) {
        ReconnectTask task = new ReconnectTask();
        task.url = url;
        tasks.add(task);
    }

    /**
     * Check task whether is valid, if canceled, is not valid
     * 
     * @param task
     * @return
     */
    private boolean isValidTask(ReconnectTask task) {
        return !canceled.contains(task.url);
    }

    /**
     * stop reconnect thread
     */
    public void stop() {
        if (!this.started) {
            return;
        }
        this.started = false;
        healConnectionThreads.interrupt();
        this.tasks.clear();
        this.canceled.clear();
    }

    /**
     * heal connection thread
     * 
     * @author yunliang.shi
     * @version $Id: ReconnectManager.java, v 0.1 Mar 11, 2016 5:24:08 PM yunliang.shi Exp $
     */
    private final class HealConnectionRunner implements Runnable {
        private long lastConnectTime = -1;

        @Override
        public void run() {
            while (ReconnectManager.this.started) {
                long start = -1;
                ReconnectTask task = null;
                try {
                    if (this.lastConnectTime > 0
                        && this.lastConnectTime < ReconnectManager.this.healConnectionInterval
                        || this.lastConnectTime < 0) {
                        Thread.sleep(ReconnectManager.this.healConnectionInterval);
                    }
                    try {
                        task = ReconnectManager.this.tasks.take();
                    } catch (InterruptedException e) {
                        // ignore
                    }

                    start = System.currentTimeMillis();
                    if (ReconnectManager.this.isValidTask(task)) {
                        try {
                            ReconnectManager.this.doReconnectTask(task);
                        } catch (InterruptedException e) {
                            throw e;
                        }
                    } else {
                        logger.warn("Invalid reconnect request task {}, cancel list size {}",
                            task.url, canceled.size());
                    }
                    this.lastConnectTime = System.currentTimeMillis() - start;
                } catch (Exception e) {
                    retryWhenException(start, task, e);
                }
            }
        }

        private void retryWhenException(long start, ReconnectTask task, Exception e) {
            if (start != -1) {
                this.lastConnectTime = System.currentTimeMillis() - start;
            }
            if (task != null) {
                logger.warn("reconnect target: {} failed.", task.url, e);
                ReconnectManager.this.addReconnectTask(task);
            }
        }
    }
}
