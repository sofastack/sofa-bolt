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

import java.util.concurrent.ConcurrentHashMap;

import com.alipay.remoting.rpc.RpcCommandType;
import com.alipay.remoting.rpc.protocol.UserProcessor;
import com.alipay.remoting.util.ConnectionUtil;
import com.alipay.remoting.util.StringUtils;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;

/**
 * Wrap the ChannelHandlerContext.
 * 
 * @author jiangping
 * @version $Id: RemotingContext.java, v 0.1 2015-9-6 PM5:50:07 tao Exp $
 */
public class RemotingContext {

    private ChannelHandlerContext                       channelContext;

    private boolean                                     serverSide     = false;

    /** whether need handle request timeout, if true, request will be discarded. The default value is true */
    private boolean                                     timeoutDiscard = true;

    /** request arrive time stamp */
    private long                                        arriveTimestamp;

    /** request timeout setting by invoke side */
    private int                                         timeout;

    /** rpc command type */
    private int                                         rpcCommandType;

    private ConcurrentHashMap<String, UserProcessor<?>> userProcessors;

    private InvokeContext                               invokeContext;

    /**
     * Constructor.
     * 
     * @param ctx
     */
    public RemotingContext(ChannelHandlerContext ctx) {
        this.channelContext = ctx;
    }

    /**
     * Constructor.
     * @param ctx
     * @param serverSide
     */
    public RemotingContext(ChannelHandlerContext ctx, boolean serverSide) {
        this.channelContext = ctx;
        this.serverSide = serverSide;
    }

    /**
     * Constructor.
     * @param ctx
     * @param serverSide
     * @param userProcessors
     */
    public RemotingContext(ChannelHandlerContext ctx, boolean serverSide,
                           ConcurrentHashMap<String, UserProcessor<?>> userProcessors) {
        this.channelContext = ctx;
        this.serverSide = serverSide;
        this.userProcessors = userProcessors;
    }

    /**
     * Constructor.
     * @param ctx
     * @param invokeContext
     * @param serverSide
     * @param userProcessors
     */
    public RemotingContext(ChannelHandlerContext ctx, InvokeContext invokeContext,
                           boolean serverSide,
                           ConcurrentHashMap<String, UserProcessor<?>> userProcessors) {
        this.channelContext = ctx;
        this.serverSide = serverSide;
        this.userProcessors = userProcessors;
        this.invokeContext = invokeContext;
    }

    /**
     * Wrap the writeAndFlush method.
     * 
     * @param msg
     * @return
     */
    public ChannelFuture writeAndFlush(RemotingCommand msg) {
        return this.channelContext.writeAndFlush(msg);
    }

    /**
     * whether this request already timeout
     * 
     * @return
     */
    public boolean isRequestTimeout() {
        if (this.timeout > 0 && (this.rpcCommandType != RpcCommandType.REQUEST_ONEWAY)
            && (System.currentTimeMillis() - this.arriveTimestamp) > this.timeout) {
            return true;
        }
        return false;
    }

    /**
     * The server side
     * 
     * @return
     */
    public boolean isServerSide() {
        return this.serverSide;
    }

    /** 
     * Get user processor for class name.
     * 
     * @param className
     * @return
     */
    public UserProcessor<?> getUserProcessor(String className) {
        return StringUtils.isBlank(className) ? null : this.userProcessors.get(className);
    }

    /**
     * Get connection from channel
     * 
     * @return
     */
    public Connection getConnection() {
        return ConnectionUtil.getConnectionFromChannel(channelContext.channel());
    }

    /**
     * Get the channel handler context.
     *
     * @return
     */
    public ChannelHandlerContext getChannelContext() {
        return channelContext;
    }

    /**
     * Set the channel handler context.
     *
     * @param ctx
     */
    public void setChannelContext(ChannelHandlerContext ctx) {
        this.channelContext = ctx;
    }

    /**
     * Getter method for property <tt>invokeContext</tt>.
     *
     * @return property value of invokeContext
     */
    public InvokeContext getInvokeContext() {
        return invokeContext;
    }

    /**
     * Setter method for property <tt>arriveTimestamp<tt>.
     *
     * @param arriveTimestamp value to be assigned to property arriveTimestamp
     */
    public void setArriveTimestamp(long arriveTimestamp) {
        this.arriveTimestamp = arriveTimestamp;
    }

    /**
     * Getter method for property <tt>arriveTimestamp</tt>.
     *
     * @return property value of arriveTimestamp
     */
    public long getArriveTimestamp() {
        return arriveTimestamp;
    }

    /**
     * Setter method for property <tt>timeout<tt>.
     *
     * @param timeout value to be assigned to property timeout
     */
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    /**
     * Getter method for property <tt>timeout</tt>.
     *
     * @return property value of timeout
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * Setter method for property <tt>rpcCommandType<tt>.
     *
     * @param rpcCommandType value to be assigned to property rpcCommandType
     */
    public void setRpcCommandType(int rpcCommandType) {
        this.rpcCommandType = rpcCommandType;
    }

    public boolean isTimeoutDiscard() {
        return timeoutDiscard;
    }

    public RemotingContext setTimeoutDiscard(boolean failFastEnabled) {
        this.timeoutDiscard = failFastEnabled;
        return this;
    }
}
