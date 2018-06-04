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
package com.alipay.remoting.rpc.userprocessor.executorselector;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alipay.remoting.BizContext;
import com.alipay.remoting.InvokeContext;
import com.alipay.remoting.rpc.common.RequestBody;
import com.alipay.remoting.rpc.protocol.SyncUserProcessor;

/**
 * a demo user processor for rpc server
 * 
 * @author xiaomin.cxm
 * @version $Id: SimpleServerUserProcessor.java, v 0.1 Jan 7, 2016 3:01:49 PM xiaomin.cxm Exp $
 */
public class SpecificServerUserProcessor extends SyncUserProcessor<RequestBody> {

    /** logger */
    private static final Logger logger        = LoggerFactory
                                                  .getLogger(SpecificServerUserProcessor.class);

    /** delay milliseconds */
    private long                delayMs;

    /** whether delay or not */
    private boolean             delaySwitch;

    private AtomicInteger       invokeTimes   = new AtomicInteger();

    private AtomicInteger       onewayTimes   = new AtomicInteger();
    private AtomicInteger       syncTimes     = new AtomicInteger();
    private AtomicInteger       futureTimes   = new AtomicInteger();
    private AtomicInteger       callbackTimes = new AtomicInteger();

    private String              remoteAddr;
    private CountDownLatch      latch         = new CountDownLatch(1);

    public SpecificServerUserProcessor() {
        this.delaySwitch = false;
        this.delayMs = 0;
    }

    public SpecificServerUserProcessor(long delay) {
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
        Assert.assertTrue(threadName.contains("Rpc-specific0-executor"));

        logger.warn("Request received:" + request);
        this.remoteAddr = bizCtx.getRemoteAddress();

        long waittime = (Long) bizCtx.getInvokeContext().get(InvokeContext.BOLT_PROCESS_WAIT_TIME);
        logger.warn("Server User processor process wait time [" + waittime + "].");

        latch.countDown();
        logger.warn("Server User processor say, remote address is [" + this.remoteAddr + "].");
        Assert.assertEquals(RequestBody.class, request.getClass());
        processTimes(request);
        if (!delaySwitch) {
            return RequestBody.DEFAULT_SERVER_RETURN_STR;
        }
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return RequestBody.DEFAULT_SERVER_RETURN_STR;
    }

    private void processTimes(RequestBody req) {
        this.invokeTimes.incrementAndGet();
        if (req.getMsg().equals(RequestBody.DEFAULT_ONEWAY_STR)) {
            this.onewayTimes.incrementAndGet();
        } else if (req.getMsg().equals(RequestBody.DEFAULT_SYNC_STR)) {
            this.syncTimes.incrementAndGet();
        } else if (req.getMsg().equals(RequestBody.DEFAULT_FUTURE_STR)) {
            this.futureTimes.incrementAndGet();
        } else if (req.getMsg().equals(RequestBody.DEFAULT_CALLBACK_STR)) {
            this.callbackTimes.incrementAndGet();
        }
    }

    @Override
    public String interest() {
        return RequestBody.class.getName();
    }

    public int getInvokeTimes() {
        return this.invokeTimes.get();
    }

    public int getInvokeTimesEachCallType(RequestBody.InvokeType type) {
        return new int[] { this.onewayTimes.get(), this.syncTimes.get(), this.futureTimes.get(),
                this.callbackTimes.get() }[type.ordinal()];
    }

    public String getRemoteAddr() throws InterruptedException {
        latch.await(100, TimeUnit.MILLISECONDS);
        return this.remoteAddr;
    }
}