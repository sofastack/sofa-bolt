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
package com.alipay.remoting.rpc.common;

import com.alipay.remoting.BizContext;
import com.alipay.remoting.rpc.protocol.SyncUserProcessor;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class FifoServerUserProcessor extends SyncUserProcessor<RequestBody> {
    //key point: create a single thread pool to fifo process request
    private Executor executor = Executors.newSingleThreadExecutor(r -> new Thread(r, "FifoThread"));
    private Integer previousOrder = null;

    @Override
    public Object handleRequest(BizContext bizCtx, RequestBody request) throws Exception {
        System.out.println("thread[" + Thread.currentThread().getName() + "] Request received:" + request + ", arriveTimestamp:" + bizCtx.getArriveTimestamp());
        Integer currentOrder = request.getId();
        if (previousOrder != null) {
            if (currentOrder - previousOrder != 1) {
                System.out.println("error: not in fifo");
            }
        }
        previousOrder = currentOrder;
        return RequestBody.DEFAULT_SERVER_RETURN_STR;
    }

    @Override
    public Executor getExecutor() {
        return executor;
    }

    @Override
    public String interest() {
        return RequestBody.class.getName();
    }
}
