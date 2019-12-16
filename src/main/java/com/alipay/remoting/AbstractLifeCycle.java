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

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author chengyi (mark.lx@antfin.com) 2018-11-05 14:43
 */
public abstract class AbstractLifeCycle implements LifeCycle {

    private final AtomicBoolean isStarted = new AtomicBoolean(false);

    @Override
    public void startup() throws LifeCycleException {
        if (isStarted.compareAndSet(false, true)) {
            return;
        }
        throw new LifeCycleException("this component has started");
    }

    @Override
    public void shutdown() throws LifeCycleException {
        if (isStarted.compareAndSet(true, false)) {
            return;
        }
        throw new LifeCycleException("this component has closed");
    }

    @Override
    public boolean isStarted() {
        return isStarted.get();
    }

    /**
     * ensure the component has been startup before providing service.
     */
    protected void ensureStarted() {
        if (!isStarted()) {
            throw new LifeCycleException(String.format(
                "Component(%s) has not been started yet, please startup first!", getClass()
                    .getSimpleName()));
        }
    }
}
