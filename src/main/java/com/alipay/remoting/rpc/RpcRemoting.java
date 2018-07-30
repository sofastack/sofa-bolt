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

import org.slf4j.Logger;

import com.alipay.remoting.BaseRemoting;
import com.alipay.remoting.CommandFactory;
import com.alipay.remoting.Connection;
import com.alipay.remoting.DefaultConnectionManager;
import com.alipay.remoting.InvokeCallback;
import com.alipay.remoting.InvokeContext;
import com.alipay.remoting.InvokeFuture;
import com.alipay.remoting.RemotingAddressParser;
import com.alipay.remoting.RemotingCommand;
import com.alipay.remoting.Url;
import com.alipay.remoting.config.switches.ProtocolSwitch;
import com.alipay.remoting.exception.CodecException;
import com.alipay.remoting.exception.RemotingException;
import com.alipay.remoting.exception.SerializationException;
import com.alipay.remoting.log.BoltLoggerFactory;
import com.alipay.remoting.rpc.protocol.RpcProtocolManager;
import com.alipay.remoting.rpc.protocol.RpcRequestCommand;
import com.alipay.remoting.util.RemotingUtil;

/**
 * Rpc remoting capability.
 * 
 * @author jiangping
 * @version $Id: RpcRemoting.java, v 0.1 Mar 6, 2016 9:09:48 PM tao Exp $
 */
public abstract class RpcRemoting extends BaseRemoting {
    static {
        RpcProtocolManager.initProtocols();
    }
    /** logger */
    private static final Logger        logger = BoltLoggerFactory.getLogger("RpcRemoting");

    /** address parser to get custom args */
    protected RemotingAddressParser    addressParser;

    /** connection manager */
    protected DefaultConnectionManager connectionManager;

    /**
     * default constructor
     */
    public RpcRemoting(CommandFactory commandFactory) {
        super(commandFactory);
    }

    /**
     * @param addressParser
     * @param connectionManager
     */
    public RpcRemoting(CommandFactory commandFactory, RemotingAddressParser addressParser,
                       DefaultConnectionManager connectionManager) {
        this(commandFactory);
        this.addressParser = addressParser;
        this.connectionManager = connectionManager;
    }

    /**
     * Oneway rpc invocation.<br>
     * Notice! DO NOT modify the request object concurrently when this method is called.
     * 
     * @param addr
     * @param request
     * @param invokeContext
     * @throws RemotingException
     * @throws InterruptedException
     */
    public void oneway(final String addr, final Object request, final InvokeContext invokeContext)
                                                                                                  throws RemotingException,
                                                                                                  InterruptedException {
        Url url = this.addressParser.parse(addr);
        this.oneway(url, request, invokeContext);
    }

    /**
     * Oneway rpc invocation.<br>
     * Notice! DO NOT modify the request object concurrently when this method is called.
     * 
     * @param url
     * @param request
     * @param invokeContext
     * @throws RemotingException
     * @throws InterruptedException
     */
    public abstract void oneway(final Url url, final Object request,
                                final InvokeContext invokeContext) throws RemotingException,
                                                                  InterruptedException;

    /**
     * Oneway rpc invocation.<br>
     * Notice! DO NOT modify the request object concurrently when this method is called.
     * 
     * @param conn
     * @param request
     * @param invokeContext 
     * @throws RemotingException
     */
    public void oneway(final Connection conn, final Object request,
                       final InvokeContext invokeContext) throws RemotingException {
        RequestCommand requestCommand = (RequestCommand) toRemotingCommand(request, conn,
            invokeContext, -1);
        requestCommand.setType(RpcCommandType.REQUEST_ONEWAY);
        preProcessInvokeContext(invokeContext, requestCommand, conn);
        super.oneway(conn, requestCommand);
    }

    /**
     * Synchronous rpc invocation.<br>
     * Notice! DO NOT modify the request object concurrently when this method is called.
     * 
     * @param addr
     * @param request
     * @param invokeContext
     * @param timeoutMillis
     * @return
     * @throws RemotingException 
     * @throws InterruptedException 
     */
    public Object invokeSync(final String addr, final Object request,
                             final InvokeContext invokeContext, final int timeoutMillis)
                                                                                        throws RemotingException,
                                                                                        InterruptedException {
        Url url = this.addressParser.parse(addr);
        return this.invokeSync(url, request, invokeContext, timeoutMillis);
    }

    /**
     * Synchronous rpc invocation.<br>
     * Notice! DO NOT modify the request object concurrently when this method is called.
     * 
     * @param url
     * @param request
     * @param invokeContext 
     * @param timeoutMillis
     * @return
     * @throws RemotingException
     * @throws InterruptedException
     */
    public abstract Object invokeSync(final Url url, final Object request,
                                      final InvokeContext invokeContext, final int timeoutMillis)
                                                                                                 throws RemotingException,
                                                                                                 InterruptedException;

    /**
     * Synchronous rpc invocation.<br>
     * Notice! DO NOT modify the request object concurrently when this method is called.
     * 
     * @param conn
     * @param request
     * @param invokeContext 
     * @param timeoutMillis
     * @return
     * @throws RemotingException
     * @throws InterruptedException
     */
    public Object invokeSync(final Connection conn, final Object request,
                             final InvokeContext invokeContext, final int timeoutMillis)
                                                                                        throws RemotingException,
                                                                                        InterruptedException {
        RemotingCommand requestCommand = toRemotingCommand(request, conn, invokeContext,
            timeoutMillis);
        preProcessInvokeContext(invokeContext, requestCommand, conn);
        ResponseCommand responseCommand = (ResponseCommand) super.invokeSync(conn, requestCommand,
            timeoutMillis);
        responseCommand.setInvokeContext(invokeContext);

        Object responseObject = RpcResponseResolver.resolveResponseObject(responseCommand,
            RemotingUtil.parseRemoteAddress(conn.getChannel()));
        return responseObject;
    }

    /**
     * Rpc invocation with future returned.<br>
     * Notice! DO NOT modify the request object concurrently when this method is called.
     * 
     * @param addr
     * @param request
     * @param invokeContext 
     * @param timeoutMillis
     * @return
     * @throws RemotingException
     * @throws InterruptedException
     */
    public RpcResponseFuture invokeWithFuture(final String addr, final Object request,
                                              final InvokeContext invokeContext, int timeoutMillis)
                                                                                                   throws RemotingException,
                                                                                                   InterruptedException {
        Url url = this.addressParser.parse(addr);
        return this.invokeWithFuture(url, request, invokeContext, timeoutMillis);
    }

    /**
     * Rpc invocation with future returned.<br>
     * Notice! DO NOT modify the request object concurrently when this method is called.
     * 
     * @param url
     * @param request
     * @param invokeContext
     * @param timeoutMillis
     * @return
     * @throws RemotingException
     * @throws InterruptedException
     */
    public abstract RpcResponseFuture invokeWithFuture(final Url url, final Object request,
                                                       final InvokeContext invokeContext,
                                                       final int timeoutMillis)
                                                                               throws RemotingException,
                                                                               InterruptedException;

    /**
     * Rpc invocation with future returned.<br>
     * Notice! DO NOT modify the request object concurrently when this method is called.
     * 
     * @param conn
     * @param request
     * @param invokeContext
     * @param timeoutMillis
     * @return
     * @throws RemotingException
     */
    public RpcResponseFuture invokeWithFuture(final Connection conn, final Object request,
                                              final InvokeContext invokeContext,
                                              final int timeoutMillis) throws RemotingException {

        RemotingCommand requestCommand = toRemotingCommand(request, conn, invokeContext,
            timeoutMillis);

        preProcessInvokeContext(invokeContext, requestCommand, conn);
        InvokeFuture future = super.invokeWithFuture(conn, requestCommand, timeoutMillis);
        return new RpcResponseFuture(RemotingUtil.parseRemoteAddress(conn.getChannel()), future);
    }

    /**
     * Rpc invocation with callback.<br>
     * Notice! DO NOT modify the request object concurrently when this method is called.
     * 
     * @param addr
     * @param request
     * @param invokeContext 
     * @param invokeCallback
     * @param timeoutMillis
     * @throws RemotingException
     * @throws InterruptedException
     */
    public void invokeWithCallback(String addr, Object request, final InvokeContext invokeContext,
                                   InvokeCallback invokeCallback, int timeoutMillis)
                                                                                    throws RemotingException,
                                                                                    InterruptedException {
        Url url = this.addressParser.parse(addr);
        this.invokeWithCallback(url, request, invokeContext, invokeCallback, timeoutMillis);
    }

    /**
     * Rpc invocation with callback.<br>
     * Notice! DO NOT modify the request object concurrently when this method is called.
     * 
     * @param url
     * @param request
     * @param invokeContext
     * @param invokeCallback
     * @param timeoutMillis
     * @throws RemotingException
     * @throws InterruptedException
     */
    public abstract void invokeWithCallback(final Url url, final Object request,
                                            final InvokeContext invokeContext,
                                            final InvokeCallback invokeCallback,
                                            final int timeoutMillis) throws RemotingException,
                                                                    InterruptedException;

    /**
     * Rpc invocation with callback.<br>
     * Notice! DO NOT modify the request object concurrently when this method is called.
     * 
     * @param conn
     * @param request
     * @param invokeContext 
     * @param invokeCallback
     * @param timeoutMillis
     * @throws RemotingException
     */
    public void invokeWithCallback(final Connection conn, final Object request,
                                   final InvokeContext invokeContext,
                                   final InvokeCallback invokeCallback, final int timeoutMillis)
                                                                                                throws RemotingException {
        RemotingCommand requestCommand = toRemotingCommand(request, conn, invokeContext,
            timeoutMillis);
        preProcessInvokeContext(invokeContext, requestCommand, conn);
        super.invokeWithCallback(conn, requestCommand, invokeCallback, timeoutMillis);
    }

    /**
     * Convert application request object to remoting request command.
     * 
     * @param request
     * @param conn
     * @param timeoutMillis
     * @return
     * @throws CodecException
     */
    protected RemotingCommand toRemotingCommand(Object request, Connection conn,
                                                InvokeContext invokeContext, int timeoutMillis)
                                                                                               throws SerializationException {
        RpcRequestCommand command = this.getCommandFactory().createRequestCommand(request);

        if (null != invokeContext) {
            // set client custom serializer for request command if not null
            Object clientCustomSerializer = invokeContext.get(InvokeContext.BOLT_CUSTOM_SERIALIZER);
            if (null != clientCustomSerializer) {
                try {
                    command.setSerializer((Byte) clientCustomSerializer);
                } catch (ClassCastException e) {
                    throw new IllegalArgumentException(
                        "Illegal custom serializer [" + clientCustomSerializer
                                + "], the type of value should be [byte], but now is ["
                                + clientCustomSerializer.getClass().getName() + "].");
                }
            }

            // enable crc by default, user can disable by set invoke context `false` for key `InvokeContext.BOLT_CRC_SWITCH`
            Boolean crcSwitch = invokeContext.get(InvokeContext.BOLT_CRC_SWITCH,
                ProtocolSwitch.CRC_SWITCH_DEFAULT_VALUE);
            if (null != crcSwitch && crcSwitch) {
                command.setProtocolSwitch(ProtocolSwitch
                    .create(new int[] { ProtocolSwitch.CRC_SWITCH_INDEX }));
            }
        } else {
            // enable crc by default, if there is no invoke context.
            command.setProtocolSwitch(ProtocolSwitch
                .create(new int[] { ProtocolSwitch.CRC_SWITCH_INDEX }));
        }
        command.setTimeout(timeoutMillis);
        command.setRequestClass(request.getClass().getName());
        command.setInvokeContext(invokeContext);
        command.serialize();
        logDebugInfo(command);
        return command;
    }

    protected abstract void preProcessInvokeContext(InvokeContext invokeContext,
                                                    RemotingCommand cmd, Connection connection);

    /**
     * @param requestCommand
     */
    private void logDebugInfo(RemotingCommand requestCommand) {
        if (logger.isDebugEnabled()) {
            logger.debug("Send request, requestId=" + requestCommand.getId());
        }
    }

    /**
     * @see com.alipay.remoting.BaseRemoting#createInvokeFuture(com.alipay.remoting.RemotingCommand, com.alipay.remoting.InvokeContext)
     */
    @Override
    protected InvokeFuture createInvokeFuture(RemotingCommand request, InvokeContext invokeContext) {
        return new DefaultInvokeFuture(request.getId(), null, null, request.getProtocolCode()
            .getFirstByte(), this.getCommandFactory(), invokeContext);
    }

    /**
     * @see com.alipay.remoting.BaseRemoting#createInvokeFuture(Connection, RemotingCommand, InvokeContext, InvokeCallback)
     */
    @Override
    protected InvokeFuture createInvokeFuture(Connection conn, RemotingCommand request,
                                              InvokeContext invokeContext,
                                              InvokeCallback invokeCallback) {
        return new DefaultInvokeFuture(request.getId(), new RpcInvokeCallbackListener(
            RemotingUtil.parseRemoteAddress(conn.getChannel())), invokeCallback, request
            .getProtocolCode().getFirstByte(), this.getCommandFactory(), invokeContext);
    }
}
