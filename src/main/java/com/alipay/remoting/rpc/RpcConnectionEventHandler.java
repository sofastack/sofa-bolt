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

import com.alipay.remoting.Connection;
import com.alipay.remoting.ConnectionEventHandler;
import com.alipay.remoting.config.switches.GlobalSwitch;

import io.netty.channel.ChannelHandlerContext;

/**
 * ConnectionEventHandler for Rpc.
 * 
 * @author jiangping
 * @version $Id: RpcConnectionEventHandler.java, v 0.1 2015-10-16 PM4:41:29 tao Exp $
 */
public class RpcConnectionEventHandler extends ConnectionEventHandler {

    public RpcConnectionEventHandler() {
        super();
    }

    public RpcConnectionEventHandler(GlobalSwitch globalSwitch) {
        super(globalSwitch);
    }

    /**
     * @see com.alipay.remoting.ConnectionEventHandler#channelInactive(io.netty.channel.ChannelHandlerContext)
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Connection conn = ctx.channel().attr(Connection.CONNECTION).get();
        if (conn != null) {
            this.getConnectionManager().remove(conn);
        }
        super.channelInactive(ctx);
    }
}
