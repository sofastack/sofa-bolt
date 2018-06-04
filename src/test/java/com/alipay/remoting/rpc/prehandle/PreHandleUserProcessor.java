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
package com.alipay.remoting.rpc.prehandle;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alipay.remoting.BizContext;
import com.alipay.remoting.DefaultBizContext;
import com.alipay.remoting.InvokeContext;
import com.alipay.remoting.NamedThreadFactory;
import com.alipay.remoting.RemotingContext;
import com.alipay.remoting.rpc.common.RequestBody;
import com.alipay.remoting.rpc.common.SimpleServerUserProcessor;
import com.alipay.remoting.rpc.protocol.SyncUserProcessor;

public class PreHandleUserProcessor extends SyncUserProcessor<RequestBody> {

    /** logger */
    private static final Logger logger      = LoggerFactory
                                                .getLogger(SimpleServerUserProcessor.class);

    /** executor */
    private ThreadPoolExecutor  executor    = new ThreadPoolExecutor(1, 3, 60, TimeUnit.SECONDS,
                                                new ArrayBlockingQueue<Runnable>(4),
                                                new NamedThreadFactory("Request-process-pool"));

    private AtomicInteger       invokeTimes = new AtomicInteger();

    @Override
    public BizContext preHandleRequest(RemotingContext remotingCtx, RequestBody request) {
        BizContext ctx = new MyBizContext(remotingCtx);
        ctx.put("test", "test");
        return ctx;
    }

    @Override
    public Object handleRequest(BizContext bizCtx, RequestBody request) throws Exception {
        logger.warn("Request received:" + request);
        invokeTimes.incrementAndGet();

        long waittime = (Long) bizCtx.getInvokeContext().get(InvokeContext.BOLT_PROCESS_WAIT_TIME);
        logger.warn("PreHandleUserProcessor User processor process wait time [" + waittime + "].");

        Assert.assertEquals(RequestBody.class, request.getClass());
        Assert.assertEquals("127.0.0.1", bizCtx.getRemoteHost());
        Assert.assertTrue(bizCtx.getRemotePort() != -1);
        return bizCtx.get("test");
    }

    @Override
    public String interest() {
        return RequestBody.class.getName();
    }

    @Override
    public Executor getExecutor() {
        return executor;
    }

    public int getInvokeTimes() {
        return this.invokeTimes.get();
    }

    class MyBizContext extends DefaultBizContext implements BizContext {
        /** customerized context */
        private Map<String, String> custCtx = new HashMap<String, String>();

        /**
         * Constructor
         * 
         * @param remotingCtx
         */
        public MyBizContext(RemotingContext remotingCtx) {
            super(remotingCtx);
        }

        /**
         * @see com.alipay.remoting.DefaultBizContext#put(java.lang.String, java.lang.String)
         */
        @Override
        public void put(String key, String value) {
            custCtx.put(key, value);
        }

        /**
         * @see com.alipay.remoting.DefaultBizContext#get(java.lang.String)
         */
        @Override
        public String get(String key) {
            return custCtx.get(key);
        }
    }
}
