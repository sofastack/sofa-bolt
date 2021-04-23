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
 * InvokeCallback which support {@link RejectedExecutionPolicy} is able to process the task-rejected situation.
 *
 * @author muyun
 * @version $Id: RejectionProcessableInvokeCallback.java, v 0.1 2019年12月05日 9:16 PM muyun Exp $
 */
public interface RejectionProcessableInvokeCallback extends InvokeCallback {

    /**
     * when user executor rejected the {@link com.alipay.remoting.rpc.RpcInvokeCallbackListener.CallbackTask},
     * bolt will handle the rejected task according to this {@link RejectedExecutionPolicy}
     * @return rejectedExecution Policy
     * @see com.alipay.remoting.rpc.RpcInvokeCallbackListener#onResponse(InvokeFuture)
     */
    RejectedExecutionPolicy rejectedExecutionPolicy();
}
