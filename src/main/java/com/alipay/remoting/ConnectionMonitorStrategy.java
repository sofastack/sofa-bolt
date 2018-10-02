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

import com.alipay.remoting.util.RunStateRecordedFutureTask;

/**
 * The strategy of connection monitor
 *
 * @author tsui
 * @version $Id: ConnectionMonitorStrategy.java, v 0.1 2017-02-21 12:06 tsui Exp $
 */
public interface ConnectionMonitorStrategy {

    /**
     * Filter connections to monitor
     *
     * @param connections
     */
    Map<String, List<Connection>> filter(List<Connection> connections);

    /**
     * Add a set of connections to monitor.
     * <p>
     * The previous connections in monitor of this protocol,
     * will be dropped by monitor automatically.
     *
     * @param connPools
     */
    void monitor(Map<String, RunStateRecordedFutureTask<ConnectionPool>> connPools);
}
