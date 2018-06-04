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

/**
 * Exception to represent the run method of a future task has not been called.
 * 
 * @author tsui
 * @version $Id: FutureTaskNotRunYetException.java, v 0.1 2017-07-31 16:29 tsui Exp $
 */
public class FutureTaskNotRunYetException extends Exception {
    /** For serialization */
    private static final long serialVersionUID = 2929126204324060632L;

    /**
     * Constructor.
     */
    public FutureTaskNotRunYetException() {
    }

    /**
     * Constructor.
     */
    public FutureTaskNotRunYetException(String message) {
        super(message);
    }

    /**
     * Constructor.
     */
    public FutureTaskNotRunYetException(String message, Throwable cause) {
        super(message, cause);
    }
}