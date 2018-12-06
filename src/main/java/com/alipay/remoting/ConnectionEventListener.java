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

/**
 * Listen and dispatch connection events.
 * @author jiangping
 * @version $Id: DefaultConnectionEventListener.java, v 0.1 Mar 5, 2016 10:56:20 AM tao Exp $
 */
// TODO: 2018/4/23 by zmyer
public class ConnectionEventListener {
    //连接事件处理器集合
    private ConcurrentHashMap<ConnectionEventType, List<ConnectionEventProcessor>> processors = new ConcurrentHashMap<ConnectionEventType, List<ConnectionEventProcessor>>(
                                                                                                  3);

    /**
     * Dispatch events.
     *
     * @param type
     * @param remoteAddr
     * @param conn
     */
    // TODO: 2018/4/23 by zmyer
    public void onEvent(ConnectionEventType type, String remoteAddr, Connection conn) {
        //根据连接事件，获取处理器列表
        List<ConnectionEventProcessor> processorList = this.processors.get(type);
        if (processorList != null) {
            for (ConnectionEventProcessor processor : processorList) {
                //开始处理每个事件
                processor.onEvent(remoteAddr, conn);
            }
        }
    }

    /**
     * Add event processor.
     *
     * @param type
     * @param processor
     */
    // TODO: 2018/4/23 by zmyer
    public void addConnectionEventProcessor(ConnectionEventType type,
                                            ConnectionEventProcessor processor) {
        List<ConnectionEventProcessor> processorList = this.processors.get(type);
        if (processorList == null) {
            this.processors.putIfAbsent(type, new ArrayList<ConnectionEventProcessor>(1));
            processorList = this.processors.get(type);
        }
        processorList.add(processor);
    }

}
