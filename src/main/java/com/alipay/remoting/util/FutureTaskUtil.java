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

import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;

/**
 * Utils for future task
 *
 * @author tsui
 * @version $Id: FutureTaskUtil.java, v 0.1 2017-07-24 17:07 tsui Exp $
 */
public class FutureTaskUtil {
    /**
     * get the result of a future task
     *
     * Notice: the run method of this task should have been called at first.
     *
     * @param task
     * @param <T>
     * @return
     */
    public static <T> T getFutureTaskResult(RunStateRecordedFutureTask<T> task, Logger logger) {
        T t = null;
        if (null != task) {
            try {
                t = task.getAfterRun();
            } catch (InterruptedException e) {
                logger.error("Future task interrupted!", e);
            } catch (ExecutionException e) {
                logger.error("Future task execute failed!", e);
            } catch (FutureTaskNotRunYetException e) {
                logger.error("Future task has not run yet!", e);
            } catch (FutureTaskNotCompleted e) {
                logger.error("Future task has not completed!", e);
            }
        }
        return t;
    }

    /**
     * launder the throwable
     *
     * @param t
     */
    public static void launderThrowable(Throwable t) {
        if (t instanceof RuntimeException) {
            throw (RuntimeException) t;
        } else if (t instanceof Error) {
            throw (Error) t;
        } else {
            throw new IllegalStateException("Not unchecked!", t);
        }
    }
}