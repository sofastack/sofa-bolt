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
package com.alipay.remoting.util;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A customized FutureTask which can record whether the run method has been called.
 * @author tsui
 * @version $Id: RunStateRecordedFutureTask.java, v 0.1 2017-07-31 16:28 tsui Exp $
 */
public class RunStateRecordedFutureTask<V> extends FutureTask<V> {
    private AtomicBoolean hasRun = new AtomicBoolean();

    public RunStateRecordedFutureTask(Callable<V> callable) {
        super(callable);
    }

    @Override
    public void run() {
        this.hasRun.set(true);
        super.run();
    }

    public V getAfterRun() throws InterruptedException, ExecutionException,
                          FutureTaskNotRunYetException, FutureTaskNotCompleted {
        if (!hasRun.get()) {
            throw new FutureTaskNotRunYetException();
        }

        if (!isDone()) {
            throw new FutureTaskNotCompleted();
        }

        return super.get();
    }
}