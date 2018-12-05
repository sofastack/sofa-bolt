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

import java.util.concurrent.Executor;

/**
 * Invoke callback.
 * 
 * @author jiangping
 * @version $Id: InvokeCallback.java, v 0.1 2015-9-30 AM10:24:26 tao Exp $
 */
public interface InvokeCallback {

    /**
     * Response received.
     * 
     * @param result
     */
    void onResponse(final Object result);

    /**
     * Exception caught.
     * 
     * @param e
     */
    void onException(final Throwable e);

    /**
     * User defined executor.
     * 
     * @return
     */
    Executor getExecutor();

}
