/**
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

import com.alipay.remoting.rpc.protocol.UserProcessor;

/**
 * Factory that creates connections.
 * 
 * @author jiangping
 * @version $Id: ConnectionFactory.java, v 0.1 2015-9-21 PM7:47:46 tao Exp $
 */

public interface ConnectionFactory {
    /**
     * Initialize the factory.
     */
    public void init(ConnectionEventHandler connectionEventHandler);

    /**
     * Create a connection use #BoltUrl
     * 
     * @param url
     * @return
     * @throws Exception
     */
    public Connection createConnection(Url url) throws Exception;

    /**
     * Create a connection according to the IP and port.
     * Note: The default protocol is RpcProtocol.
     * 
     * @param targetIP
     * @param targetPort
     * @param connectTimeout
     * @return
     * @throws Exception
     */
    public Connection createConnection(String targetIP, int targetPort, int connectTimeout)
                                                                                           throws Exception;

    /**
     * Create a connection according to the IP and port.
     *
     * Note: The default protocol is RpcProtocolV2, and you can specify the version
     *
     * @param targetIP
     * @param targetPort
     * @param version
     * @param connectTimeout
     * @return
     * @throws Exception
     */
    public Connection createConnection(String targetIP, int targetPort, byte version,
                                       int connectTimeout) throws Exception;

    /**
     * Register User processor
     * 
     * @param processor
     */
    public void registerUserProcessor(UserProcessor<?> processor);

    /**
     * Shutdown the EventLoopGroup
     */
    public void shutdown();
}
