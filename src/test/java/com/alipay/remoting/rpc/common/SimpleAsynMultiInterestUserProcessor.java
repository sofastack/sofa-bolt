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

import com.alipay.remoting.AsyncContext;
import com.alipay.remoting.BizContext;
import com.alipay.remoting.NamedThreadFactory;
import com.alipay.remoting.rpc.protocol.AsynMultiInterestUserProcessor;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * a demo asyn multi interest user processor for rpc server
 *
 * @author lollapalooza1989
 * @version $Id: SimpleAsynMultiInterestUserProcessor.java, v 0.1 Oct 10, 2019 2:01:40 PM lollapalooza1989 Exp $
 */
public class SimpleAsynMultiInterestUserProcessor extends
                                                 AsynMultiInterestUserProcessor<RequestBody> {

    /**
     * logger
     */
    private static final Logger logger = LoggerFactory
                                           .getLogger(SimpleAsynMultiInterestUserProcessor.class);

    /**
     * delay milliseconds
     */
    private long                delayMs;

    /**
     * whether delay or not
     */
    private boolean             delaySwitch;

    /**
     * whether exception
     */
    private boolean             isException;

    /**
     * whether null
     */
    private boolean             isNull;
    /**
     * executor
     */
    private ThreadPoolExecutor  executor;

    private ThreadPoolExecutor  asyncExecutor;

    public SimpleAsynMultiInterestUserProcessor() {
        this.executor = new ThreadPoolExecutor(1, 3, 60, TimeUnit.SECONDS,
            new ArrayBlockingQueue<Runnable>(4), new NamedThreadFactory("Request-process-pool"));
        this.asyncExecutor = new ThreadPoolExecutor(1, 3, 60, TimeUnit.SECONDS,
            new ArrayBlockingQueue<Runnable>(4), new NamedThreadFactory(
                "Another-aysnc-process-pool"));
    }

    @Override
    public void handleRequest(BizContext bizCtx, AsyncContext asyncCtx, RequestBody request) {
        this.asyncExecutor.execute(new SimpleAsynMultiInterestUserProcessor.InnerTask(bizCtx,
            asyncCtx, request));
    }

    @Override
    public List<String> multiInterest() {
        return Arrays.asList(RequestBody.class.getName());
    }

    class InnerTask implements Runnable {
        private BizContext   bizCtx;
        private AsyncContext asyncCtx;
        private RequestBody  request;

        public InnerTask(BizContext bizCtx, AsyncContext asyncCtx, RequestBody request) {
            this.bizCtx = bizCtx;
            this.asyncCtx = asyncCtx;
            this.request = request;
        }

        public void run() {
            logger.warn("Request received:" + request);
            logger.warn("Server User processor say, remote address is [" + "remoteAddr" + "].");
            Assert.assertEquals(RequestBody.class, request.getClass());
            if (isException) {
                this.asyncCtx.sendResponse(new IllegalArgumentException("Exception test"));
            } else if (isNull) {
                this.asyncCtx.sendResponse(null);
            } else {
                if (!delaySwitch) {
                    this.asyncCtx.sendResponse(RequestBody.DEFAULT_SERVER_RETURN_STR);
                    return;
                }
                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                this.asyncCtx.sendResponse(RequestBody.DEFAULT_SERVER_RETURN_STR);
            }
        }
    }
}
