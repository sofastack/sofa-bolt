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
package com.alipay.remoting.rpc.userprocessor.processinio;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alipay.remoting.BizContext;
import com.alipay.remoting.InvokeContext;
import com.alipay.remoting.NamedThreadFactory;
import com.alipay.remoting.rpc.common.RequestBody;
import com.alipay.remoting.rpc.protocol.SyncUserProcessor;

/**
 * a demo specific user processor for rpc client
 * 
 * @author xiaomin.cxm
 * @version $Id: SimpleClientUserProcessor.java, v 0.1 Jan 7, 2016 3:01:49 PM xiaomin.cxm Exp $
 */
public class SpecificClientUserProcessor extends SyncUserProcessor<RequestBody> {

    /** logger */
    private static final Logger logger      = LoggerFactory
                                                .getLogger(SpecificClientUserProcessor.class);

    /** delay milliseconds */
    private long                delayMs;

    /** whether delay or not */
    private boolean             delaySwitch;

    private ThreadPoolExecutor  executor;

    private AtomicInteger       invokeTimes = new AtomicInteger();

    public SpecificClientUserProcessor() {
        this.delaySwitch = false;
        this.delayMs = 0;
        this.executor = new ThreadPoolExecutor(1, 3, 60, TimeUnit.SECONDS,
            new ArrayBlockingQueue<Runnable>(4), new NamedThreadFactory("Rpc-common-executor"));
    }

    public SpecificClientUserProcessor(long delay) {
        this();
        if (delay < 0) {
            throw new IllegalArgumentException("delay time illegal!");
        }
        this.delaySwitch = true;
        this.delayMs = delay;
    }

    @Override
    public Object handleRequest(BizContext bizCtx, RequestBody request) throws Exception {
        String threadName = Thread.currentThread().getName();
        Assert.assertTrue(threadName.contains("bolt-netty-client-worker"));

        logger.warn("Request received:" + request);
        Assert.assertEquals(RequestBody.class, request.getClass());

        long waittime = (Long) bizCtx.getInvokeContext().get(InvokeContext.BOLT_PROCESS_WAIT_TIME);
        logger.warn("Client User processor process wait time [" + waittime + "].");

        invokeTimes.incrementAndGet();
        if (!delaySwitch) {
            return RequestBody.DEFAULT_CLIENT_RETURN_STR;
        }
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return RequestBody.DEFAULT_CLIENT_RETURN_STR;
    }

    @Override
    public String interest() {
        return RequestBody.class.getName();
    }

    @Override
    public boolean processInIOThread() {
        return true;
    }

    public int getInvokeTimes() {
        return this.invokeTimes.get();
    }
}
