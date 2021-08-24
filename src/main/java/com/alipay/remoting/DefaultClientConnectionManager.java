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

import com.alipay.remoting.connection.ConnectionFactory;

/**
 * Do some preparatory work in order to refactor the ConnectionManager in the next version.
 *
 * @author chengyi (mark.lx@antfin.com) 2019-03-07 14:27
 */
public class DefaultClientConnectionManager extends DefaultConnectionManager implements
                                                                            ClientConnectionManager {

    public DefaultClientConnectionManager(ConnectionSelectStrategy connectionSelectStrategy,
                                          ConnectionFactory connectionFactory,
                                          ConnectionEventHandler connectionEventHandler,
                                          ConnectionEventListener connectionEventListener) {
        super(connectionSelectStrategy, connectionFactory, connectionEventHandler,
            connectionEventListener);
    }

    @Override
    public void startup() throws LifeCycleException {
        super.startup();

        this.connectionEventHandler.setConnectionManager(this);
        this.connectionEventHandler.setConnectionEventListener(connectionEventListener);
        this.connectionFactory.init(connectionEventHandler);
    }

}
