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
package com.alipay.remoting.exception;

/**
 * Exception when codec problems occur
 * 
 * @author xiaomin.cxm
 * @version $Id: CodecException.java, v 0.1 2016-1-3 PM 6:26:12 xiaomin.cxm Exp $
 */
public class CodecException extends RemotingException {

    /** For serialization */
    private static final long serialVersionUID = -7513762648815278960L;

    /**
     * Constructor.
     */
    public CodecException() {
    }

    /**
     * Constructor.
     *
     * @param message the detail message.
     */
    public CodecException(String message) {
        super(message);
    }

    /**
     * Constructor.
     *
     * @param message the detail message
     * @param cause the cause
     */
    public CodecException(String message, Throwable cause) {
        super(message, cause);
    }

}