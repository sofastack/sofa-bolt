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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;

import com.alipay.remoting.config.ConfigManager;
import com.alipay.remoting.config.Configs;
import com.alipay.remoting.log.BoltLoggerFactory;
import com.alipay.remoting.util.FutureTaskUtil;
import com.alipay.remoting.util.RemotingUtil;
import com.alipay.remoting.util.RunStateRecordedFutureTask;

/**
 * An implemented strategy to monitor connections:
 *   <lu>
 *       <li>each time scheduled, filter connections with {@link Configs#CONN_SERVICE_STATUS_OFF} at first.</li>
 *       <li>then close connections.</li>
 *   </lu>
 *
 * @author tsui
 * @version $Id: ScheduledDisconnectStrategy.java, v 0.1 2017-02-21 14:14 tsui Exp $
 */
public class ScheduledDisconnectStrategy implements ConnectionMonitorStrategy {
    private static final Logger     logger                 = BoltLoggerFactory
                                                               .getLogger("CommonDefault");

    /** the connections threshold of each {@link Url#uniqueKey} */
    private static final int        CONNECTION_THRESHOLD   = ConfigManager.conn_threshold();

    /** fresh select connections to be closed */
    private Map<String, Connection> freshSelectConnections = new ConcurrentHashMap<String, Connection>();

    /** Retry detect period for ScheduledDisconnectStrategy*/
    private static int              RETRY_DETECT_PERIOD    = ConfigManager.retry_detect_period();

    /** random */
    private Random                  random                 = new Random();

    /**
     * Filter connections to monitor
     *
     * @param connections
     */
    @Override
    public Map<String, List<Connection>> filter(List<Connection> connections) {
        List<Connection> serviceOnConnections = new ArrayList<Connection>();
        List<Connection> serviceOffConnections = new ArrayList<Connection>();
        Map<String, List<Connection>> filteredConnections = new ConcurrentHashMap<String, List<Connection>>();

        for (Connection connection : connections) {
            String serviceStatus = (String) connection.getAttribute(Configs.CONN_SERVICE_STATUS);
            if (serviceStatus != null) {
                if (connection.isInvokeFutureMapFinish()
                    && !freshSelectConnections.containsValue(connection)) {
                    serviceOffConnections.add(connection);
                }
            } else {
                serviceOnConnections.add(connection);
            }
        }

        filteredConnections.put(Configs.CONN_SERVICE_STATUS_ON, serviceOnConnections);
        filteredConnections.put(Configs.CONN_SERVICE_STATUS_OFF, serviceOffConnections);
        return filteredConnections;
    }

    /**
     * Monitor connections and close connections with status is off
     *
     * @param connPools
     */
    @Override
    public void monitor(Map<String, RunStateRecordedFutureTask<ConnectionPool>> connPools) {
        try {
            if (null != connPools && !connPools.isEmpty()) {
                Iterator<Map.Entry<String, RunStateRecordedFutureTask<ConnectionPool>>> iter = connPools
                    .entrySet().iterator();

                while (iter.hasNext()) {
                    Map.Entry<String, RunStateRecordedFutureTask<ConnectionPool>> entry = iter
                        .next();
                    String poolKey = entry.getKey();
                    ConnectionPool pool = FutureTaskUtil.getFutureTaskResult(entry.getValue(),
                        logger);

                    List<Connection> connections = pool.getAll();
                    Map<String, List<Connection>> filteredConnectons = this.filter(connections);
                    List<Connection> serviceOnConnections = filteredConnectons
                        .get(Configs.CONN_SERVICE_STATUS_ON);
                    List<Connection> serviceOffConnections = filteredConnectons
                        .get(Configs.CONN_SERVICE_STATUS_OFF);
                    if (serviceOnConnections.size() > CONNECTION_THRESHOLD) {
                        Connection freshSelectConnect = serviceOnConnections.get(random
                            .nextInt(serviceOnConnections.size()));
                        freshSelectConnect.setAttribute(Configs.CONN_SERVICE_STATUS,
                            Configs.CONN_SERVICE_STATUS_OFF);

                        Connection lastSelectConnect = freshSelectConnections.remove(poolKey);
                        freshSelectConnections.put(poolKey, freshSelectConnect);

                        closeFreshSelectConnections(lastSelectConnect, serviceOffConnections);

                    } else {
                        if (freshSelectConnections.containsKey(poolKey)) {
                            Connection lastSelectConnect = freshSelectConnections.remove(poolKey);
                            closeFreshSelectConnections(lastSelectConnect, serviceOffConnections);
                        }
                        if (logger.isInfoEnabled()) {
                            logger
                                .info(
                                    "the size of serviceOnConnections [{}] reached CONNECTION_THRESHOLD [{}].",
                                    serviceOnConnections.size(), CONNECTION_THRESHOLD);
                        }
                    }

                    for (Connection offConn : serviceOffConnections) {
                        if (offConn.isFine()) {
                            offConn.close();
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("ScheduledDisconnectStrategy monitor error", e);
        }
    }

    /**
     * close the connection of the fresh select connections
     *
     * @param lastSelectConnect
     * @param serviceOffConnections
     * @throws InterruptedException
     */
    private void closeFreshSelectConnections(Connection lastSelectConnect,
                                             List<Connection> serviceOffConnections)
                                                                                    throws InterruptedException {
        if (null != lastSelectConnect) {
            if (lastSelectConnect.isInvokeFutureMapFinish()) {
                serviceOffConnections.add(lastSelectConnect);
            } else {
                Thread.sleep(RETRY_DETECT_PERIOD);
                if (lastSelectConnect.isInvokeFutureMapFinish()) {
                    serviceOffConnections.add(lastSelectConnect);
                } else {
                    if (logger.isInfoEnabled()) {
                        logger.info("Address={} won't close at this schedule turn",
                            RemotingUtil.parseRemoteAddress(lastSelectConnect.getChannel()));
                    }
                }
            }
        }
    }
}
