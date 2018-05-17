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

import com.alipay.remoting.util.RemotingUtil;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

/**
 * default biz context
 *
 * @author xiaomin.cxm
 * @version $Id: DefaultBizContext.java, v 0.1 Jan 7, 2016 10:42:30 AM xiaomin.cxm Exp $
 */
public class DefaultBizContext implements BizContext {
    /**
     * remoting context
     */
    private RemotingContext remotingCtx;

    /**
     * Constructor with RemotingContext
     *
     * @param remotingCtx
     */
    public DefaultBizContext(RemotingContext remotingCtx) {
        this.remotingCtx = remotingCtx;
    }

    /**
     * get remoting context
     *
     * @return RemotingContext
     */
    protected RemotingContext getRemotingCtx() {
        return this.remotingCtx;
    }

    /**
     * @see com.alipay.remoting.BizContext#getRemoteAddress()
     */
    @Override
    public String getRemoteAddress() {
        if (null != this.remotingCtx) {
            ChannelHandlerContext channelCtx = this.remotingCtx.getChannelContext();
            Channel channel = channelCtx.channel();
            if (null != channel) {
                return RemotingUtil.parseRemoteAddress(channel);
            }
        }
        return "UNKNOWN_ADDRESS";
    }

    /**
     * @see com.alipay.remoting.BizContext#getRemoteHost()
     */
    @Override
    public String getRemoteHost() {
        if (null != this.remotingCtx) {
            ChannelHandlerContext channelCtx = this.remotingCtx.getChannelContext();
            Channel channel = channelCtx.channel();
            if (null != channel) {
                return RemotingUtil.parseRemoteIP(channel);
            }
        }
        return "UNKNOWN_HOST";
    }

    /**
     * @see com.alipay.remoting.BizContext#getRemotePort()
     */
    @Override
    public int getRemotePort() {
        if (null != this.remotingCtx) {
            ChannelHandlerContext channelCtx = this.remotingCtx.getChannelContext();
            Channel channel = channelCtx.channel();
            if (null != channel) {
                return RemotingUtil.parseRemotePort(channel);
            }
        }
        return -1;
    }

    /**
     * @see BizContext#getConnection()
     */
    @Override
    public Connection getConnection() {
        if (null != this.remotingCtx) {
            return this.remotingCtx.getConnection();
        }
        return null;
    }

    /**
     * @see com.alipay.remoting.BizContext#isRequestTimeout()
     */
    @Override
    public boolean isRequestTimeout() {
        return this.remotingCtx.isRequestTimeout();
    }

    /**
     * get the timeout value from rpc client.
     *
     * @return
     */
    @Override
    public int getClientTimeout() {
        return this.remotingCtx.getTimeout();
    }

    /**
     * get the arrive time stamp
     *
     * @return
     */
    @Override
    public long getArriveTimestamp() {
        return this.remotingCtx.getArriveTimestamp();
    }

    /**
     * @see com.alipay.remoting.BizContext#put(java.lang.String, java.lang.String)
     */
    @Override
    public void put(String key, String value) {
    }

    /**
     * @see com.alipay.remoting.BizContext#get(java.lang.String)
     */
    @Override
    public String get(String key) {
        return null;
    }

    /**
     * @see BizContext#getInvokeContext()
     */
    @Override
    public InvokeContext getInvokeContext() {
        return this.remotingCtx.getInvokeContext();
    }
}
