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

import java.net.InetSocketAddress;

import io.netty.util.Timeout;

/**
 * The future of an invocation.
 * 
 * @author jiangping
 * @version $Id: InvokeFuture.java, v 0.1 2015-9-21 PM5:30:35 tao Exp $
 */
public interface InvokeFuture {
    /**
     * Wait response with timeout.
     */
    RemotingCommand waitResponse(final long timeoutMillis) throws InterruptedException;

    /**
     * Wait response with unlimit timeout
     */
    RemotingCommand waitResponse() throws InterruptedException;

    /**
     * Create a remoting command response when connection closed
     */
    RemotingCommand createConnectionClosedResponse(InetSocketAddress responseHost);

    /**
     * Put the response to the future.
     */
    void putResponse(final RemotingCommand response);

    /**
     * Get the id of the invocation.
     */
    int invokeId();

    /**
     * Execute the callback.
     */
    void executeInvokeCallback();

    /**
     * Asynchronous execute the callback abnormally.
     */
    void tryAsyncExecuteInvokeCallbackAbnormally();

    /**
     * Set the cause if exception caught.
     */
    void setCause(Throwable cause);

    /**
     * Get the cause of exception of the future.
     */
    Throwable getCause();

    /**
     * Get the application callback of the future.
     */
    InvokeCallback getInvokeCallback();

    /**
     * Add timeout for the future.
     */
    void addTimeout(Timeout timeout);

    /**
     * Cancel the timeout.
     */
    void cancelTimeout();

    /**
     * Whether the future is done.
     */
    boolean isDone();

    /**
     * Get application classloader.
     */
    ClassLoader getAppClassLoader();

    /**
     * Get the protocol code of command.
     */
    byte getProtocolCode();

    /**
     * set invoke context
     */
    void setInvokeContext(InvokeContext invokeContext);

    /**
     * get invoke context
     */
    InvokeContext getInvokeContext();
}
