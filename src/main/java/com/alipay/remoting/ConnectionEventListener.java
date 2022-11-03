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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Listen and dispatch connection events.
 * @author jiangping
 * @version $Id: DefaultConnectionEventListener.java, v 0.1 Mar 5, 2016 10:56:20 AM tao Exp $
 */
public class ConnectionEventListener {

    private final ReentrantLock lock = new ReentrantLock();

    private final ConcurrentHashMap<ConnectionEventType, List<ConnectionEventProcessor>> processors = new ConcurrentHashMap<ConnectionEventType, List<ConnectionEventProcessor>>(
                                                                                                  3);

    /**
     * Dispatch events.
     * 
     * @param type ConnectionEventType
     * @param remoteAddress remoting address
     * @param connection Connection
     */
    public void onEvent(ConnectionEventType type, String remoteAddress, Connection connection) {
        List<ConnectionEventProcessor> processorList = this.processors.get(type);
        if (processorList != null) {
            for (ConnectionEventProcessor processor : processorList) {
                processor.onEvent(remoteAddress, connection);
            }
        }
    }

    /**
     * Add event processor.
     * 
     * @param type ConnectionEventType
     * @param processor ConnectionEventProcessor
     */
    public void addConnectionEventProcessor(ConnectionEventType type,
                                            ConnectionEventProcessor processor) {
        lock.lock();
        try {
            List<ConnectionEventProcessor> processorList = this.processors.get(type);
            if (null == processorList) {
                processorList = new ArrayList<ConnectionEventProcessor>(1);
                this.processors.put(type, processorList);
            }
            processorList.add(processor);
        } finally {
            lock.unlock();
        }
    }
}
