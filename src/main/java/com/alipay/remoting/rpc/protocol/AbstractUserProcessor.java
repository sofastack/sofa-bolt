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
package com.alipay.remoting.rpc.protocol;

import java.util.concurrent.Executor;

import com.alipay.remoting.BizContext;
import com.alipay.remoting.DefaultBizContext;
import com.alipay.remoting.RemotingContext;

/**
 * Implements common function and provide default value.
 * 
 * @author xiaomin.cxm
 * @version $Id: AbstractUserProcessor.java, v 0.1 May 19, 2016 3:38:22 PM xiaomin.cxm Exp $
 */
public abstract class AbstractUserProcessor<T> implements UserProcessor<T> {

    /** executor selector, default null unless provide one using its setter method */
    protected ExecutorSelector executorSelector;

    /**
     * Provide a default - {@link DefaultBizContext} implementation of {@link BizContext}.
     * 
     * @see com.alipay.remoting.rpc.protocol.UserProcessor#preHandleRequest(com.alipay.remoting.RemotingContext, java.lang.Object)
     */
    @Override
    public BizContext preHandleRequest(RemotingContext remotingCtx, T request) {
        return new DefaultBizContext(remotingCtx);
    }

    /**
     * By default return null.
     *
     * @see UserProcessor#getExecutor()
     */
    @Override
    public Executor getExecutor() {
        return null;
    }

    /**
     * @see UserProcessor#getExecutorSelector()
     */
    @Override
    public ExecutorSelector getExecutorSelector() {
        return this.executorSelector;
    }

    /**
     * @see UserProcessor#setExecutorSelector(ExecutorSelector)
     */
    @Override
    public void setExecutorSelector(ExecutorSelector executorSelector) {
        this.executorSelector = executorSelector;
    }

    /**
     * By default, return false, means not deserialize and process biz logic in io thread
     *
     * @see UserProcessor#processInIOThread() 
     */
    @Override
    public boolean processInIOThread() {
        return false;
    }

    /**
     * By default, return true, means discard requests which timeout already.
     */
    @Override
    public boolean timeoutDiscard() {
        return true;
    }
}