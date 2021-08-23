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
import java.util.Random;

import com.alipay.remoting.config.BoltClientOption;
import com.alipay.remoting.config.Configuration;
import org.slf4j.Logger;

import com.alipay.remoting.config.Configs;
import com.alipay.remoting.log.BoltLoggerFactory;
import com.alipay.remoting.util.StringUtils;

/**
 * Select a connection randomly
 *
 * @author yunliang.shi
 * @version $Id: RandomSelectStrategy.java, v 0.1 Mar 30, 2016 8:38:40 PM yunliang.shi Exp $
 */
public class RandomSelectStrategy implements ConnectionSelectStrategy {

    private static final Logger logger    = BoltLoggerFactory.getLogger("CommonDefault");

    private static final int    MAX_TIMES = 5;
    private final Random        random    = new Random();
    private final Configuration configuration;

    public RandomSelectStrategy(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public Connection select(List<Connection> connections) {
        try {
            if (connections == null) {
                return null;
            }
            int size = connections.size();
            if (size == 0) {
                return null;
            }

            Connection result;
            if (configuration != null && configuration.option(BoltClientOption.CONN_MONITOR_SWITCH)) {
                List<Connection> serviceStatusOnConnections = new ArrayList<Connection>();
                for (Connection conn : connections) {
                    String serviceStatus = (String) conn.getAttribute(Configs.CONN_SERVICE_STATUS);
                    if (!StringUtils.equals(serviceStatus, Configs.CONN_SERVICE_STATUS_OFF)) {
                        serviceStatusOnConnections.add(conn);
                    }
                }
                if (serviceStatusOnConnections.size() == 0) {
                    throw new Exception(
                        "No available connection when select in RandomSelectStrategy.");
                }
                result = randomGet(serviceStatusOnConnections);
            } else {
                result = randomGet(connections);
            }
            return result;
        } catch (Throwable e) {
            logger.error("Choose connection failed using RandomSelectStrategy!", e);
            return null;
        }
    }

    /**
     * get one connection randomly
     * 
     * @param connections source connections
     * @return result connection
     */
    private Connection randomGet(List<Connection> connections) {
        if (null == connections || connections.isEmpty()) {
            return null;
        }

        int size = connections.size();
        int tries = 0;
        Connection result = null;
        while ((result == null || !result.isFine()) && tries++ < MAX_TIMES) {
            result = connections.get(this.random.nextInt(size));
        }

        if (result != null && !result.isFine()) {
            result = null;
        }
        return result;
    }
}
