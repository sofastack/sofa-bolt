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
     * 
     * @param timeoutMillis
     * @return
     * @throws InterruptedException
     */
    public RemotingCommand waitResponse(final long timeoutMillis) throws InterruptedException;

    /**
     * Wait response with unlimit timeout
     *
     * @return
     * @throws InterruptedException
     */
    public RemotingCommand waitResponse() throws InterruptedException;

    /**
     * Create a remoting command response when connection closed
     *
     * @param responseHost
     * @return
     */
    public RemotingCommand createConnectionClosedResponse(InetSocketAddress responseHost);

    /**
     * Put the response to the future.
     * 
     * @param response
     */
    public void putResponse(final RemotingCommand response);

    /**
     * Get the id of the invocation.
     * 
     * @return
     */
    public int invokeId();

    /**
     * Execute the callback.
     */
    public void executeInvokeCallback();

    /**
     * Asynchronous execute the callback abnormally.
     */
    public void tryAsyncExecuteInvokeCallbackAbnormally();

    /**
     * Set the cause if exception caught.
     * 
     * @param cause
     */
    public void setCause(Throwable cause);

    /**
     * Get the cause of exception of the future.
     * 
     * @return
     */
    public Throwable getCause();

    /**
     * Get the application callback of the future.
     * 
     * @return
     */
    public InvokeCallback getInvokeCallback();

    /**
     * Add timeout for the future.
     * 
     * @param timeout
     */
    public void addTimeout(Timeout timeout);

    /**
     * Cancel the timeout.
     */
    public void cancelTimeout();

    /**
     * Whether the future is done.
     * 
     * @return
     */
    public boolean isDone();

    /**
     * @return application classloader
     */
    public ClassLoader getAppClassLoader();

    /**
     * Get the protocol code of command.
     * 
     * @return
     */
    public byte getProtocolCode();

    /**
     * set invoke context
     * @param invokeContext
     */
    public void setInvokeContext(InvokeContext invokeContext);

    /**
     * get invoke context
     * @return
     */
    public InvokeContext getInvokeContext();
}
