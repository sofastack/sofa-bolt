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

import com.alipay.remoting.CommandFactory;
import com.alipay.remoting.Connection;
import com.alipay.remoting.DefaultConnectionManager;
import com.alipay.remoting.InvokeCallback;
import com.alipay.remoting.InvokeCallbackListener;
import com.alipay.remoting.InvokeContext;
import com.alipay.remoting.InvokeFuture;
import com.alipay.remoting.RemotingAddressParser;
import com.alipay.remoting.RemotingCommand;
import com.alipay.remoting.ResponseStatus;
import com.alipay.remoting.TimerHolder;
import com.alipay.remoting.Url;
import com.alipay.remoting.exception.RemotingException;
import com.alipay.remoting.log.BoltLoggerFactory;
import com.alipay.remoting.rpc.exception.InvokeException;
import com.alipay.remoting.util.RemotingUtil;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import org.slf4j.Logger;

import java.util.concurrent.TimeUnit;

/**
 * Rpc server remoting
 * 
 * @author xiaomin.cxm
 * @version $Id: RpcServerRemoting.java, v 0.1 Apr 14, 2016 12:00:39 PM xiaomin.cxm Exp $
 */
public class RpcServerRemoting extends RpcRemoting {
    private static final Logger logger                 = BoltLoggerFactory.getLogger("RpcRemoting");


    /**
     * default constructor
     */
    public RpcServerRemoting(CommandFactory commandFactory) {
        super(commandFactory);
    }

    /**
     * @param addressParser
     * @param connectionManager
     */
    public RpcServerRemoting(CommandFactory commandFactory, RemotingAddressParser addressParser,
                             DefaultConnectionManager connectionManager) {
        super(commandFactory, addressParser, connectionManager);
    }

    /**
     * @see com.alipay.remoting.rpc.RpcRemoting#invokeSync(com.alipay.remoting.Url, java.lang.Object, InvokeContext, int)
     */
    @Override
    public Object invokeSync(Url url, Object request, InvokeContext invokeContext, int timeoutMillis)
                                                                                                     throws RemotingException,
                                                                                                     InterruptedException {
        Connection conn = this.connectionManager.get(url.getUniqueKey());
        if (null == conn) {
            throw new RemotingException("Client address [" + url.getUniqueKey()
                                        + "] not connected yet!");
        }
        this.connectionManager.check(conn);
        return this.invokeSync(conn, request, invokeContext, timeoutMillis);
    }

    /**
     * @see com.alipay.remoting.rpc.RpcRemoting#oneway(com.alipay.remoting.Url, java.lang.Object, InvokeContext)
     */
    @Override
    public void oneway(Url url, Object request, InvokeContext invokeContext)
                                                                            throws RemotingException {
        Connection conn = this.connectionManager.get(url.getUniqueKey());
        if (null == conn) {
            throw new RemotingException("Client address [" + url.getOriginUrl()
                                        + "] not connected yet!");
        }
        this.connectionManager.check(conn);
        this.oneway(conn, request, invokeContext);
    }

    /**
     * @see com.alipay.remoting.rpc.RpcRemoting#invokeWithFuture(com.alipay.remoting.Url, java.lang.Object, InvokeContext, int)
     */
    @Override
    public RpcResponseFuture invokeWithFuture(Url url, Object request, InvokeContext invokeContext,
                                              int timeoutMillis) throws RemotingException {
        Connection conn = this.connectionManager.get(url.getUniqueKey());
        if (null == conn) {
            throw new RemotingException("Client address [" + url.getUniqueKey()
                                        + "] not connected yet!");
        }
        this.connectionManager.check(conn);
        return this.invokeWithFuture(conn, request, invokeContext, timeoutMillis);
    }

    /**
     * @see com.alipay.remoting.rpc.RpcRemoting#invokeWithCallback(com.alipay.remoting.Url, java.lang.Object, InvokeContext, com.alipay.remoting.InvokeCallback, int)
     */
    @Override
    public void invokeWithCallback(Url url, Object request, InvokeContext invokeContext,
                                   InvokeCallback invokeCallback, int timeoutMillis)
                                                                                    throws RemotingException {
        Connection conn = this.connectionManager.get(url.getUniqueKey());
        if (null == conn) {
            throw new RemotingException("Client address [" + url.getUniqueKey()
                                        + "] not connected yet!");
        }
        this.connectionManager.check(conn);
        this.invokeWithCallback(conn, request, invokeContext, invokeCallback, timeoutMillis);
    }

    @Override
    protected void preProcessInvokeContext(InvokeContext invokeContext, RemotingCommand cmd,
                                           Connection connection) {
        if (null != invokeContext) {
            invokeContext.putIfAbsent(InvokeContext.SERVER_REMOTE_IP,
                RemotingUtil.parseRemoteIP(connection.getChannel()));
            invokeContext.putIfAbsent(InvokeContext.SERVER_REMOTE_PORT,
                RemotingUtil.parseRemotePort(connection.getChannel()));
            invokeContext.putIfAbsent(InvokeContext.SERVER_LOCAL_IP,
                RemotingUtil.parseLocalIP(connection.getChannel()));
            invokeContext.putIfAbsent(InvokeContext.SERVER_LOCAL_PORT,
                RemotingUtil.parseLocalPort(connection.getChannel()));
            invokeContext.putIfAbsent(InvokeContext.BOLT_INVOKE_REQUEST_ID, cmd.getId());
        }
    }
    /**/
    public void goaway(final Url url, int timeoutMillis) throws RemotingException, InterruptedException {

        final Connection conn = this.connectionManager.get(url.getUniqueKey());
        if (null == conn) {
            logger.warn("Client address [" + url.getUniqueKey() + "] not connected yet!");
            return;
        }

        this.connectionManager.check(conn);

        final GoAwayCommand goaway = new GoAwayCommand();
        final InvokeFuture future = createInvokeFuture(goaway, goaway.getInvokeContext());
        conn.addInvokeFuture(future);

        final int goawayId = goaway.getId();
        if (logger.isDebugEnabled()) {
            logger.debug("Send goaway Id={}, to remoteAddr={}", goawayId, conn.getRemoteAddress().toString());
        }
        try {
            conn.getChannel().writeAndFlush(goaway).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture f) throws Exception {
                    if (!f.isSuccess()) {
                        conn.removeInvokeFuture(goawayId);
                        future.putResponse(commandFactory.createSendFailedResponse(
                                conn.getRemoteAddress(), f.cause()));
                        logger.error("Invoke send failed, id={}", goawayId, f.cause());
                    }
                }
            });
        } catch (Exception e) {
            conn.removeInvokeFuture(goawayId);
            future.putResponse(commandFactory.createSendFailedResponse(conn.getRemoteAddress(), e));
            logger.error("Exception caught when sending invocation, id={}", goawayId, e);
        }

        RemotingCommand response = future.waitResponse(timeoutMillis);

        if (response == null) {
            conn.removeInvokeFuture(goawayId);
            response = this.commandFactory.createTimeoutResponse(conn.getRemoteAddress());
            logger.warn("Wait goaway response, request id={} timeout!", goaway);
        }
        ResponseCommand responseCommand = (ResponseCommand) response;
        if (responseCommand.getResponseStatus() != ResponseStatus.SUCCESS) {

            String msg = String.format("Rpc invocation exception: %s, the address is %s, id=%s",
                    responseCommand.getResponseStatus(), conn.getRemoteAddress(), responseCommand.getId());
            logger.warn(msg);
            if (responseCommand.getCause() != null) {
                throw new InvokeException(msg, responseCommand.getCause());
            } else {
                throw new InvokeException(msg + ", please check the server log for more.");
            }
        }
    }
}
