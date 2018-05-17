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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;

import com.alipay.remoting.NamedThreadFactory;
import com.alipay.remoting.rpc.protocol.UserProcessor;
import com.alipay.remoting.util.StringUtils;

/**
 * Default Executor Selector
 *
 * @author tsui
 * @version $Id: DefaultExecutorSelector.java, v 0.1 2017-04-24 15:51 tsui Exp $
 */
public class DefaultExecutorSelector implements UserProcessor.ExecutorSelector {
    public static final String EXECUTOR0 = "executor0";
    public static final String EXECUTOR1 = "executor1";
    private String             chooseExecutorStr;
    /** executor */
    private ThreadPoolExecutor executor0;
    private ThreadPoolExecutor executor1;

    public DefaultExecutorSelector(String chooseExecutorStr) {
        this.chooseExecutorStr = chooseExecutorStr;
        this.executor0 = new ThreadPoolExecutor(1, 3, 60, TimeUnit.SECONDS,
            new ArrayBlockingQueue<Runnable>(4), new NamedThreadFactory("Rpc-specific0-executor"));
        this.executor1 = new ThreadPoolExecutor(1, 3, 60, TimeUnit.SECONDS,
            new ArrayBlockingQueue<Runnable>(4), new NamedThreadFactory("Rpc-specific1-executor"));
    }

    @Override
    public Executor select(String requestClass, Object requestHeader) {
        Assert.assertNotNull(requestClass);
        Assert.assertNotNull(requestHeader);
        if (StringUtils.equals(chooseExecutorStr, (String) requestHeader)) {
            return executor1;
        } else {
            return executor0;
        }
    }
}