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
 * RejectedExecutionPolicy determines how to deal with this situation that user executor rejected the {@link com.alipay.remoting.rpc.RpcInvokeCallbackListener.CallbackTask}.
 *
 * @author muyun
 * @version $Id: RejectedExecutionPolicy.java, v 0.1 2019年12月05日 7:38 PM muyun Exp $
 */
public enum RejectedExecutionPolicy {
    /* discard the callback task */
    DISCARD,
    /* caller runs the callback in IO-thread */
    CALLER_RUNS,
    /* caller handle the task with exception strategy user provided */
    CALLER_HANDLE_EXCEPTION
}
