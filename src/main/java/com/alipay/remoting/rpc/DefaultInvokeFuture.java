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
package com.alipay.remoting.rpc;

import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;

import com.alipay.remoting.CommandFactory;
import com.alipay.remoting.CommandHandler;
import com.alipay.remoting.InvokeCallback;
import com.alipay.remoting.InvokeCallbackListener;
import com.alipay.remoting.InvokeContext;
import com.alipay.remoting.InvokeFuture;
import com.alipay.remoting.Protocol;
import com.alipay.remoting.ProtocolCode;
import com.alipay.remoting.ProtocolManager;
import com.alipay.remoting.RemotingCommand;
import com.alipay.remoting.log.BoltLoggerFactory;

import io.netty.util.Timeout;

/**
 * The default implementation of InvokeFuture.
 * 
 * @author jiangping
 * @version $Id: DefaultInvokeFuture.java, v 0.1 2015-9-27 PM6:30:22 tao Exp $
 */
public class DefaultInvokeFuture implements InvokeFuture {

    private static final Logger      logger                  = BoltLoggerFactory
                                                                 .getLogger("RpcRemoting");

    private int                      invokeId;

    private InvokeCallbackListener   callbackListener;

    private InvokeCallback           callback;

    private volatile ResponseCommand responseCommand;

    private final CountDownLatch     countDownLatch          = new CountDownLatch(1);

    private final AtomicBoolean      executeCallbackOnlyOnce = new AtomicBoolean(false);

    private Timeout                  timeout;

    private Throwable                cause;

    private ClassLoader              classLoader;

    private byte                     protocol;

    private InvokeContext            invokeContext;

    private CommandFactory           commandFactory;

    /**
     * Constructor.
     *
     * @param invokeId invoke id
     * @param callbackListener callback listener
     * @param callback callback
     * @param protocol protocol code
     * @param commandFactory command factory
     */
    public DefaultInvokeFuture(int invokeId, InvokeCallbackListener callbackListener,
                               InvokeCallback callback, byte protocol, CommandFactory commandFactory) {
        this.invokeId = invokeId;
        this.callbackListener = callbackListener;
        this.callback = callback;
        this.classLoader = Thread.currentThread().getContextClassLoader();
        this.protocol = protocol;
        this.commandFactory = commandFactory;
    }

    /**
     * Constructor.
     *
     * @param invokeId invoke id
     * @param callbackListener callback listener
     * @param callback callback
     * @param protocol protocol
     * @param commandFactory command factory
     * @param invokeContext invoke context
     */
    public DefaultInvokeFuture(int invokeId, InvokeCallbackListener callbackListener,
                               InvokeCallback callback, byte protocol,
                               CommandFactory commandFactory, InvokeContext invokeContext) {
        this(invokeId, callbackListener, callback, protocol, commandFactory);
        this.invokeContext = invokeContext;
    }

    @Override
    public ResponseCommand waitResponse(long timeoutMillis) throws InterruptedException {
        this.countDownLatch.await(timeoutMillis, TimeUnit.MILLISECONDS);
        return this.responseCommand;
    }

    @Override
    public ResponseCommand waitResponse() throws InterruptedException {
        this.countDownLatch.await();
        return this.responseCommand;
    }

    @Override
    public RemotingCommand createConnectionClosedResponse(InetSocketAddress responseHost) {
        return this.commandFactory.createConnectionClosedResponse(responseHost, null);
    }

    /** 
     * @see com.alipay.remoting.InvokeFuture#putResponse(com.alipay.remoting.RemotingCommand)
     */
    @Override
    public void putResponse(RemotingCommand response) {
        this.responseCommand = (ResponseCommand) response;
        this.countDownLatch.countDown();
    }

    /**
     * 
     * @see com.alipay.remoting.InvokeFuture#isDone()
     */
    @Override
    public boolean isDone() {
        return this.countDownLatch.getCount() <= 0;
    }

    @Override
    public ClassLoader getAppClassLoader() {
        return this.classLoader;
    }

    /** 
     * @see com.alipay.remoting.InvokeFuture#invokeId()
     */
    @Override
    public int invokeId() {
        return this.invokeId;
    }

    @Override
    public void executeInvokeCallback() {
        if (callbackListener != null) {
            if (this.executeCallbackOnlyOnce.compareAndSet(false, true)) {
                callbackListener.onResponse(this);
            }
        }
    }

    /** 
     * @see com.alipay.remoting.InvokeFuture#getInvokeCallback()
     */
    @Override
    public InvokeCallback getInvokeCallback() {
        return this.callback;
    }

    /** 
     * @see com.alipay.remoting.InvokeFuture#addTimeout(io.netty.util.Timeout)
     */
    @Override
    public void addTimeout(Timeout timeout) {
        this.timeout = timeout;
    }

    /** 
     * @see com.alipay.remoting.InvokeFuture#cancelTimeout()
     */
    @Override
    public void cancelTimeout() {
        if (this.timeout != null) {
            this.timeout.cancel();
        }
    }

    /** 
     * @see com.alipay.remoting.InvokeFuture#setCause(java.lang.Throwable)
     */
    @Override
    public void setCause(Throwable cause) {
        this.cause = cause;
    }

    /** 
     * @see com.alipay.remoting.InvokeFuture#getCause()
     */
    @Override
    public Throwable getCause() {
        return this.cause;
    }

    /** 
     * @see com.alipay.remoting.InvokeFuture#getProtocolCode()
     */
    @Override
    public byte getProtocolCode() {
        return this.protocol;
    }

    /**
     * @see InvokeFuture#getInvokeContext()
     */
    @Override
    public void setInvokeContext(InvokeContext invokeContext) {
        this.invokeContext = invokeContext;
    }

    /**
     * @see InvokeFuture#setInvokeContext(InvokeContext)
     */
    @Override
    public InvokeContext getInvokeContext() {
        return invokeContext;
    }

    /** 
     * @see com.alipay.remoting.InvokeFuture#tryAsyncExecuteInvokeCallbackAbnormally()
     */
    @Override
    public void tryAsyncExecuteInvokeCallbackAbnormally() {
        try {
            Protocol protocol = ProtocolManager.getProtocol(ProtocolCode.fromBytes(this.protocol));
            if (null != protocol) {
                CommandHandler commandHandler = protocol.getCommandHandler();
                if (null != commandHandler) {
                    ExecutorService executor = commandHandler.getDefaultExecutor();
                    if (null != executor) {
                        executor.execute(new Runnable() {
                            @Override
                            public void run() {
                                ClassLoader oldClassLoader = null;
                                try {
                                    if (DefaultInvokeFuture.this.getAppClassLoader() != null) {
                                        oldClassLoader = Thread.currentThread()
                                            .getContextClassLoader();
                                        Thread.currentThread().setContextClassLoader(
                                            DefaultInvokeFuture.this.getAppClassLoader());
                                    }
                                    DefaultInvokeFuture.this.executeInvokeCallback();
                                } finally {
                                    if (null != oldClassLoader) {
                                        Thread.currentThread()
                                            .setContextClassLoader(oldClassLoader);
                                    }
                                }
                            }
                        });
                    }
                } else {
                    logger.error("Executor null in commandHandler of protocolCode [{}].",
                        this.protocol);
                }
            } else {
                logger.error("protocolCode [{}] not registered!", this.protocol);
            }
        } catch (Exception e) {
            logger.error("Exception caught when executing invoke callback abnormally.", e);
        }
    }

}
