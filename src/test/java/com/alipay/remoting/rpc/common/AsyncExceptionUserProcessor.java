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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alipay.remoting.AsyncContext;
import com.alipay.remoting.BizContext;
import com.alipay.remoting.NamedThreadFactory;
import com.alipay.remoting.rpc.protocol.AsyncUserProcessor;

/**
 * a demo user processor
 * 
 * @author xiaomin.cxm
 * @version $Id: SimpleUserProcessor.java, v 0.1 Jan 7, 2016 3:01:49 PM xiaomin.cxm Exp $
 */
public class AsyncExceptionUserProcessor extends AsyncUserProcessor<RequestBody> {

    /** logger */
    private static final Logger logger      = LoggerFactory
                                                .getLogger(AsyncExceptionUserProcessor.class);

    /** delay milliseconds */
    private long                delayMs;

    /** whether delay or not */
    private boolean             delaySwitch;

    /** executor */
    private ThreadPoolExecutor  executor    = new ThreadPoolExecutor(1, 3, 60, TimeUnit.SECONDS,
                                                new ArrayBlockingQueue<Runnable>(4),
                                                new NamedThreadFactory("Request-process-pool"));

    private AtomicInteger       invokeTimes = new AtomicInteger();

    public AsyncExceptionUserProcessor() {
        this.delaySwitch = false;
        this.delayMs = 0;
    }

    public AsyncExceptionUserProcessor(long delay) {
        if (delay < 0) {
            throw new IllegalArgumentException("delay time illegal!");
        }
        this.delaySwitch = true;
        this.delayMs = delay;
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

    @Override
    public void handleRequest(BizContext bizCtx, AsyncContext asyncCtx, RequestBody request) {
        logger.warn("Request received:" + request);
        invokeTimes.incrementAndGet();
        if (!delaySwitch) {
            throw new RuntimeException("Hello exception!");
        }
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("Hello exception!");
    }
}
