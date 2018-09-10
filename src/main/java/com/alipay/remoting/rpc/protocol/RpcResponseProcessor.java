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
package com.alipay.remoting.rpc.protocol;

import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;

import com.alipay.remoting.AbstractRemotingProcessor;
import com.alipay.remoting.Connection;
import com.alipay.remoting.InvokeFuture;
import com.alipay.remoting.RemotingCommand;
import com.alipay.remoting.RemotingContext;
import com.alipay.remoting.log.BoltLoggerFactory;
import com.alipay.remoting.util.RemotingUtil;

/**
 * Processor to process RpcResponse.
 * 
 * @author jiangping
 * @version $Id: RpcResponseProcessor.java, v 0.1 2015-10-1 PM11:06:52 tao Exp $
 */
public class RpcResponseProcessor extends AbstractRemotingProcessor<RemotingCommand> {

    private static final Logger logger = BoltLoggerFactory.getLogger("RpcRemoting");

    /**
     * Default constructor.
     */
    public RpcResponseProcessor() {

    }

    /**
     * Constructor.
     */
    public RpcResponseProcessor(ExecutorService executor) {
        super(executor);
    }

    /**
     * @see com.alipay.remoting.AbstractRemotingProcessor#doProcess
     */
    @Override
    public void doProcess(RemotingContext ctx, RemotingCommand cmd) {

        Connection conn = ctx.getChannelContext().channel().attr(Connection.CONNECTION).get();
        InvokeFuture future = conn.removeInvokeFuture(cmd.getId());
        ClassLoader oldClassLoader = null;
        try {
            if (future != null) {
                if (future.getAppClassLoader() != null) {
                    oldClassLoader = Thread.currentThread().getContextClassLoader();
                    Thread.currentThread().setContextClassLoader(future.getAppClassLoader());
                }
                future.putResponse(cmd);
                future.cancelTimeout();
                try {
                    future.executeInvokeCallback();
                } catch (Exception e) {
                    logger.error("Exception caught when executing invoke callback, id={}",
                        cmd.getId(), e);
                }
            } else {
                logger
                    .warn("Cannot find InvokeFuture, maybe already timeout, id={}, from={} ",
                        cmd.getId(),
                        RemotingUtil.parseRemoteAddress(ctx.getChannelContext().channel()));
            }
        } finally {
            if (null != oldClassLoader) {
                Thread.currentThread().setContextClassLoader(oldClassLoader);
            }
        }

    }

}
