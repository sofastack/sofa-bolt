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

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;

import com.alipay.remoting.exception.RemotingException;
import com.alipay.remoting.log.BoltLoggerFactory;
import com.alipay.remoting.util.RemotingUtil;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;

/**
 * Base remoting capability.
 * 
 * @author jiangping
 * @version $Id: BaseRemoting.java, v 0.1 Mar 4, 2016 12:09:56 AM tao Exp $
 */
public abstract class BaseRemoting {

    private final static Logger LOGGER                       = BoltLoggerFactory
                                                                 .getLogger("CommonDefault");
    private final static long   ABANDONING_REQUEST_THRESHOLD = 0L;

    protected CommandFactory    commandFactory;

    public BaseRemoting(CommandFactory commandFactory) {
        this.commandFactory = commandFactory;
    }

    /**
     * Synchronous invocation
     * 
     * @param conn
     * @param request
     * @param timeoutMillis
     * @return
     * @throws InterruptedException 
     * @throws RemotingException
     */
    protected RemotingCommand invokeSync(final Connection conn, final RemotingCommand request,
                                         final int timeoutMillis) throws RemotingException,
                                                                 InterruptedException {
        int remainingTime = remainingTime(request, timeoutMillis);
        if (remainingTime <= ABANDONING_REQUEST_THRESHOLD) {
            // already timeout
            LOGGER
                .warn(
                    "already timeout before writing to the network, requestId: {}, remoting address: {}",
                    request.getId(),
                    conn.getUrl() != null ? conn.getUrl() : RemotingUtil.parseRemoteAddress(conn
                        .getChannel()));
            return this.getCommandFactory(conn).createTimeoutResponse(conn.getRemoteAddress());
        }

        final InvokeFuture future = createInvokeFuture(request, request.getInvokeContext());
        conn.addInvokeFuture(future);
        final int requestId = request.getId();
        InvokeContext invokeContext = request.getInvokeContext();
        if (null != invokeContext) {
            invokeContext.put(InvokeContext.BOLT_PROCESS_CLIENT_BEFORE_SEND, System.nanoTime());
        }
        try {
            conn.getChannel().writeAndFlush(request).addListener(new ChannelFutureListener() {

                @Override
                public void operationComplete(ChannelFuture f) throws Exception {
                    if (!f.isSuccess()) {
                        conn.removeInvokeFuture(requestId);
                        future.putResponse(getCommandFactory(conn).createSendFailedResponse(
                            conn.getRemoteAddress(), f.cause()));
                        LOGGER.error("Invoke send failed, id={}", requestId, f.cause());
                    }
                }

            });
            if (null != invokeContext) {
                invokeContext.put(InvokeContext.BOLT_PROCESS_CLIENT_AFTER_SEND, System.nanoTime());
            }
        } catch (Exception e) {
            conn.removeInvokeFuture(requestId);
            future.putResponse(getCommandFactory(conn).createSendFailedResponse(
                conn.getRemoteAddress(), e));
            LOGGER.error("Exception caught when sending invocation, id={}", requestId, e);
        }
        RemotingCommand response = future.waitResponse(remainingTime);

        if (null != invokeContext) {
            invokeContext.put(InvokeContext.BOLT_PROCESS_CLIENT_RECEIVED, System.nanoTime());
        }

        if (response == null) {
            conn.removeInvokeFuture(requestId);
            response = this.getCommandFactory(conn).createTimeoutResponse(conn.getRemoteAddress());
            LOGGER.warn("Wait response, request id={} timeout!", requestId);
        }

        return response;
    }

    /**
     * Invocation with callback.
     * 
     * @param conn
     * @param request
     * @param invokeCallback
     * @param timeoutMillis
     * @throws InterruptedException
     */
    protected void invokeWithCallback(final Connection conn, final RemotingCommand request,
                                      final InvokeCallback invokeCallback, final int timeoutMillis) {
        final InvokeFuture future = createInvokeFuture(conn, request, request.getInvokeContext(),
            invokeCallback);
        int remainingTime = remainingTime(request, timeoutMillis);
        if (remainingTime <= ABANDONING_REQUEST_THRESHOLD) {
            LOGGER
                .warn(
                    "already timeout before writing to the network, requestId: {}, remoting address: {}",
                    request.getId(),
                    conn.getUrl() != null ? conn.getUrl() : RemotingUtil.parseRemoteAddress(conn
                        .getChannel()));
            future.putResponse(getCommandFactory(conn).createTimeoutResponse(
                conn.getRemoteAddress()));
            future.tryAsyncExecuteInvokeCallbackAbnormally();
            return;
        }
        conn.addInvokeFuture(future);
        final int requestId = request.getId();
        try {
            Timeout timeout = TimerHolder.getTimer().newTimeout(new TimerTask() {
                @Override
                public void run(Timeout timeout) throws Exception {
                    InvokeFuture future = conn.removeInvokeFuture(requestId);
                    if (future != null) {
                        future.putResponse(getCommandFactory(conn).createTimeoutResponse(
                            conn.getRemoteAddress()));
                        future.tryAsyncExecuteInvokeCallbackAbnormally();
                    }
                }

            }, remainingTime, TimeUnit.MILLISECONDS);
            future.addTimeout(timeout);
            conn.getChannel().writeAndFlush(request).addListener(new ChannelFutureListener() {

                @Override
                public void operationComplete(ChannelFuture cf) throws Exception {
                    if (!cf.isSuccess()) {
                        InvokeFuture f = conn.removeInvokeFuture(requestId);
                        if (f != null) {
                            f.cancelTimeout();
                            f.putResponse(getCommandFactory(conn).createSendFailedResponse(
                                conn.getRemoteAddress(), cf.cause()));
                            f.tryAsyncExecuteInvokeCallbackAbnormally();
                        }
                        LOGGER.error("Invoke send failed. The address is {}",
                            RemotingUtil.parseRemoteAddress(conn.getChannel()), cf.cause());
                    }
                }

            });
        } catch (Exception e) {
            InvokeFuture f = conn.removeInvokeFuture(requestId);
            if (f != null) {
                f.cancelTimeout();
                f.putResponse(getCommandFactory(conn).createSendFailedResponse(
                    conn.getRemoteAddress(), e));
                f.tryAsyncExecuteInvokeCallbackAbnormally();
            }
            LOGGER.error("Exception caught when sending invocation. The address is {}",
                RemotingUtil.parseRemoteAddress(conn.getChannel()), e);
        }
    }

    /**
     * Invocation with future returned.
     * 
     * @param conn
     * @param request
     * @param timeoutMillis
     * @return
     */
    protected InvokeFuture invokeWithFuture(final Connection conn, final RemotingCommand request,
                                            final int timeoutMillis) {

        final InvokeFuture future = createInvokeFuture(request, request.getInvokeContext());
        conn.addInvokeFuture(future);
        int remainingTime = remainingTime(request, timeoutMillis);
        if (remainingTime <= ABANDONING_REQUEST_THRESHOLD) {
            LOGGER
                .warn(
                    "already timeout before writing to the network, requestId: {}, remoting address: {}",
                    request.getId(),
                    conn.getUrl() != null ? conn.getUrl() : RemotingUtil.parseRemoteAddress(conn
                        .getChannel()));
            future.putResponse(getCommandFactory(conn).createTimeoutResponse(
                conn.getRemoteAddress()));
            return future;
        }

        final int requestId = request.getId();
        try {
            Timeout timeout = TimerHolder.getTimer().newTimeout(new TimerTask() {
                @Override
                public void run(Timeout timeout) throws Exception {
                    InvokeFuture future = conn.removeInvokeFuture(requestId);
                    if (future != null) {
                        future.putResponse(getCommandFactory(conn).createTimeoutResponse(
                            conn.getRemoteAddress()));
                    }
                }

            }, remainingTime, TimeUnit.MILLISECONDS);
            future.addTimeout(timeout);

            conn.getChannel().writeAndFlush(request).addListener(new ChannelFutureListener() {

                @Override
                public void operationComplete(ChannelFuture cf) throws Exception {
                    if (!cf.isSuccess()) {
                        InvokeFuture f = conn.removeInvokeFuture(requestId);
                        if (f != null) {
                            f.cancelTimeout();
                            f.putResponse(getCommandFactory(conn).createSendFailedResponse(
                                conn.getRemoteAddress(), cf.cause()));
                        }
                        LOGGER.error("Invoke send failed. The address is {}",
                            RemotingUtil.parseRemoteAddress(conn.getChannel()), cf.cause());
                    }
                }

            });
        } catch (Exception e) {
            InvokeFuture f = conn.removeInvokeFuture(requestId);
            if (f != null) {
                f.cancelTimeout();
                f.putResponse(getCommandFactory(conn).createSendFailedResponse(
                    conn.getRemoteAddress(), e));
            }
            LOGGER.error("Exception caught when sending invocation. The address is {}",
                RemotingUtil.parseRemoteAddress(conn.getChannel()), e);
        }
        return future;
    }

    /**
     * Oneway invocation.
     * 
     * @param conn
     * @param request
     * @throws InterruptedException
     */
    protected void oneway(final Connection conn, final RemotingCommand request) {
        if (conn == null) {
            LOGGER.error("conn is null");
            return;
        }

        Url url = conn.getUrl();
        if (url != null) {
            int remainingTime = remainingTime(request, url.getConnectTimeout());
            if (remainingTime <= ABANDONING_REQUEST_THRESHOLD) {
                LOGGER
                    .warn(
                        "already timeout before writing to the network, requestId: {}, remoting address: {}",
                        request.getId(),
                        conn.getUrl() != null ? conn.getUrl() : RemotingUtil
                            .parseRemoteAddress(conn.getChannel()));
                return;
            }
        }

        try {
            conn.getChannel().writeAndFlush(request).addListener(new ChannelFutureListener() {

                @Override
                public void operationComplete(ChannelFuture f) throws Exception {
                    if (!f.isSuccess()) {
                        LOGGER.error("Invoke send failed. The address is {}",
                            RemotingUtil.parseRemoteAddress(conn.getChannel()), f.cause());
                    }
                }

            });
        } catch (Exception e) {
            LOGGER.error("Exception caught when sending invocation. The address is {}",
                RemotingUtil.parseRemoteAddress(conn.getChannel()), e);
        }
    }

    /**
     * Create invoke future with {@link InvokeContext}.
     * @param request
     * @param invokeContext
     * @return
     */
    protected abstract InvokeFuture createInvokeFuture(final RemotingCommand request,
                                                       final InvokeContext invokeContext);

    /**
     * Create invoke future with {@link InvokeContext}.
     * @param conn
     * @param request
     * @param invokeContext
     * @param invokeCallback
     * @return
     */
    protected abstract InvokeFuture createInvokeFuture(final Connection conn,
                                                       final RemotingCommand request,
                                                       final InvokeContext invokeContext,
                                                       final InvokeCallback invokeCallback);

    @Deprecated
    protected CommandFactory getCommandFactory() {
        LOGGER
            .warn("The method getCommandFactory() is deprecated. Please use getCommandFactory(ProtocolCode/Connection) instead.");
        return commandFactory;
    }

    protected CommandFactory getCommandFactory(Connection conn) {
        ProtocolCode protocolCode = conn.getChannel().attr(Connection.PROTOCOL).get();
        return getCommandFactory(protocolCode);
    }

    protected CommandFactory getCommandFactory(ProtocolCode protocolCode) {
        if (protocolCode == null) {
            return getCommandFactory();
        }
        Protocol protocol = ProtocolManager.getProtocol(protocolCode);
        if (protocol == null) {
            return getCommandFactory();
        }
        return protocol.getCommandFactory();
    }

    private int remainingTime(RemotingCommand request, int timeout) {
        InvokeContext invokeContext = request.getInvokeContext();
        if (invokeContext == null) {
            return timeout;
        }
        Long cost = invokeContext.get(InvokeContext.CLIENT_CONN_CREATETIME);
        if (cost == null) {
            return timeout;
        }

        return (int) (timeout - cost);
    }
}
