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

/**
 * Connection heart beat manager, operate heart beat whether enabled for a certain connection at runtime
 * 
 * @author xiaomin.cxm
 * @version $Id: ConnectionHeartbeatManager.java, v 0.1 Apr 12, 2016 6:55:56 PM xiaomin.cxm Exp $
 */
public interface ConnectionHeartbeatManager {

    /**
     * disable heart beat for a certain connection
     * 
     * @param connection
     */
    void disableHeartbeat(Connection connection);

    /**
     * enable heart beat for a certain connection
     * 
     * @param connection
     */
    void enableHeartbeat(Connection connection);
}
