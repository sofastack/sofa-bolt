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
package com.alipay.remoting.rpc;

/**
 * Constants for rpc.
 * 
 * @author jiangping
 * @version $Id: RpcConfigs.java, v 0.1 2015-10-10 PM3:03:47 tao Exp $
 */
public class RpcConfigs {
    /**
     * Protocol key in url.
     */
    public static final String URL_PROTOCOL                                  = "_PROTOCOL";

    /**
     * Version key in url.
     */
    public static final String URL_VERSION                                   = "_VERSION";

    /**
     * Connection timeout key in url.
     */
    public static final String CONNECT_TIMEOUT_KEY                           = "_CONNECTTIMEOUT";

    /**
     * Connection number key of each address
     */
    public static final String CONNECTION_NUM_KEY                            = "_CONNECTIONNUM";

    /**
     * whether need to warm up connections
     */
    public static final String CONNECTION_WARMUP_KEY                         = "_CONNECTIONWARMUP";

    /**
     * Whether to dispatch message list in default executor.
     */
    public static final String DISPATCH_MSG_LIST_IN_DEFAULT_EXECUTOR         = "bolt.rpc.dispatch-msg-list-in-default-executor";
    public static final String DISPATCH_MSG_LIST_IN_DEFAULT_EXECUTOR_DEFAULT = "true";
}