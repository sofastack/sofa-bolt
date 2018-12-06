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

import com.alipay.remoting.AbstractRemotingProcessor;
import com.alipay.remoting.Connection;
import com.alipay.remoting.InvokeFuture;
import com.alipay.remoting.RemotingCommand;
import com.alipay.remoting.RemotingContext;
import com.alipay.remoting.log.BoltLoggerFactory;
import com.alipay.remoting.rpc.ResponseCommand;
import com.alipay.remoting.util.RemotingUtil;
import org.slf4j.Logger;

import java.util.concurrent.ExecutorService;

/**
 * Processor to process RpcResponse.
 *
 * @author jiangping
 * @version $Id: RpcResponseProcessor.java, v 0.1 2015-10-1 PM11:06:52 tao Exp $
 */
// TODO: 2018/4/24 by zmyer
public class RpcResponseProcessor extends AbstractRemotingProcessor<RemotingCommand> {
    //日志
    private static final Logger logger = BoltLoggerFactory.getLogger("RpcRemoting");

    /**
     * Default constructor.
     */
    // TODO: 2018/6/22 by zmyer
    public RpcResponseProcessor() {

    }

    /**
     * @param executor
     */
    public RpcResponseProcessor(ExecutorService executor) {
        super(executor);
    }

    /**
     * @see com.alipay.remoting.AbstractRemotingProcessor#doProcess
     */
    // TODO: 2018/4/24 by zmyer
    @Override
    public void doProcess(RemotingContext ctx, RemotingCommand cmd) {
        //获取连接对象
        Connection conn = ctx.getChannelContext().channel().attr(Connection.CONNECTION).get();
        //从连接对象中删除本次调用
        InvokeFuture future = conn.removeInvokeFuture(cmd.getId());
        ClassLoader oldClassLoader = null;
        try {
            if (future != null) {
                if (future.getAppClassLoader() != null) {
                    //记录线程旧的类加载器
                    oldClassLoader = Thread.currentThread().getContextClassLoader();
                    //设置临时类加载器
                    Thread.currentThread().setContextClassLoader(future.getAppClassLoader());
                }
                //设置调用对象应答结果
                future.putResponse((ResponseCommand) cmd);
                //取消调用对象超时
                future.cancelTimeout();
                try {
                    //开始回调
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
                //恢复线程的类加载器
                Thread.currentThread().setContextClassLoader(oldClassLoader);
            }
        }
    }
}
