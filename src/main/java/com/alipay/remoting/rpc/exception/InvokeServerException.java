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
package com.alipay.remoting.rpc.exception;

import com.alipay.remoting.exception.RemotingException;

/**
 * Server exception caught when invoking
 * 
 * @author jiangping
 * @version $Id: InvokeServerException.java, v 0.1 2015-10-9 AM11:16:10 tao Exp $
 */
public class InvokeServerException extends RemotingException {
    /** For serialization  */
    private static final long serialVersionUID = 4480283862377034355L;

    /**
     * Default constructor.
     */
    public InvokeServerException() {
    }

    public InvokeServerException(String msg) {
        super(msg);
    }

    public InvokeServerException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
