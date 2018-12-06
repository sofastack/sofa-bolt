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

import com.alipay.remoting.exception.RemotingException;
import com.alipay.remoting.log.BoltLoggerFactory;
import com.alipay.remoting.util.RemotingUtil;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import org.slf4j.Logger;

import java.util.concurrent.TimeUnit;

/**
 * Base remoting capability.
 *
 * @author jiangping
 * @version $Id: BaseRemoting.java, v 0.1 Mar 4, 2016 12:09:56 AM tao Exp $
 */
// TODO: 2018/4/23 by zmyer
public abstract class BaseRemoting {
    /** logger */
    private static final Logger logger = BoltLoggerFactory.getLogger("CommonDefault");

    //命令工厂
    private CommandFactory      commandFactory;

    // TODO: 2018/4/23 by zmyer
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
    // TODO: 2018/4/23 by zmyer
    protected RemotingCommand invokeSync(final Connection conn, final RemotingCommand request,
                                         final int timeoutMillis) throws RemotingException,
                                                                 InterruptedException {
        //创建异步调用对象
        final InvokeFuture future = createInvokeFuture(request, request.getInvokeContext());
        //将当前调用注册到连接对象中
        conn.addInvokeFuture(future);
        try {
            //开始发送请求
            conn.getChannel().writeAndFlush(request).addListener(new ChannelFutureListener() {

                @Override
                public void operationComplete(ChannelFuture f) throws Exception {
                    if (!f.isSuccess()) {
                        //如果请求发送失败，则需要将当前调用从连接对象中删除
                        conn.removeInvokeFuture(request.getId());
                        //返回发送失败应答
                        future.putResponse(commandFactory.createSendFailedResponse(
                            conn.getRemoteAddress(), f.cause()));
                        logger.error("Invoke send failed, id={}", request.getId(), f.cause());
                    }
                }

            });
        } catch (Exception e) {
            conn.removeInvokeFuture(request.getId());
            if (future != null) {
                future.putResponse(commandFactory.createSendFailedResponse(conn.getRemoteAddress(),
                    e));
            }
            logger.error("Exception caught when sending invocation, id={}", request.getId(), e);
        }
        //等待应答
        RemotingCommand response = future.waitResponse(timeoutMillis);

        if (response == null) {
            //请求发送超时，直接删除当前请求
            conn.removeInvokeFuture(request.getId());
            //返回超时应答
            response = this.commandFactory.createTimeoutResponse(conn.getRemoteAddress());
            logger.warn("Wait response, request id={} timeout!", request.getId());
        }
        //返回结果
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
    // TODO: 2018/4/23 by zmyer
    protected void invokeWithCallback(final Connection conn, final RemotingCommand request,
                                      final InvokeCallback invokeCallback, final int timeoutMillis) {
        //创建调用异步对象
        final InvokeFuture future = createInvokeFuture(conn, request, request.getInvokeContext(),
            invokeCallback);
        //将当前调用插入到连接对象中
        conn.addInvokeFuture(future);

        try {
            //add timeout
            Timeout timeout = TimerHolder.getTimer().newTimeout(new TimerTask() {
                @Override
                public void run(Timeout timeout) throws Exception {
                    //如果超时，则直接从连接中删除当前调用对象
                    InvokeFuture future = conn.removeInvokeFuture(request.getId());
                    if (future != null) {
                        //返回超时应答
                        future.putResponse(commandFactory.createTimeoutResponse(conn
                            .getRemoteAddress()));
                        //开始执行future回调
                        future.tryAsyncExecuteInvokeCallbackAbnormally();
                    }
                }

            }, timeoutMillis, TimeUnit.MILLISECONDS);
            //设置超时定时器
            future.addTimeout(timeout);
            //开始写入请求
            conn.getChannel().writeAndFlush(request).addListener(new ChannelFutureListener() {

                @Override
                public void operationComplete(ChannelFuture cf) throws Exception {
                    if (!cf.isSuccess()) {
                        //如果发送失败，则从连接对象中删除调用对象
                        InvokeFuture f = conn.removeInvokeFuture(request.getId());
                        if (f != null) {
                            //取消超时机制
                            f.cancelTimeout();
                            //应答发送失败消息
                            f.putResponse(commandFactory.createSendFailedResponse(
                                conn.getRemoteAddress(), cf.cause()));
                            //回调
                            f.tryAsyncExecuteInvokeCallbackAbnormally();
                        }
                        logger.error("Invoke send failed. The address is {}",
                            RemotingUtil.parseRemoteAddress(conn.getChannel()), cf.cause());
                    }
                }

            });
        } catch (Exception e) {
            InvokeFuture f = conn.removeInvokeFuture(request.getId());
            if (f != null) {
                f.cancelTimeout();
                f.putResponse(commandFactory.createSendFailedResponse(conn.getRemoteAddress(), e));
                f.tryAsyncExecuteInvokeCallbackAbnormally();
            }
            logger.error("Exception caught when sending invocation. The address is {}",
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
    // TODO: 2018/4/23 by zmyer
    protected InvokeFuture invokeWithFuture(final Connection conn, final RemotingCommand request,
                                            final int timeoutMillis) {
        //创建调用对象
        final InvokeFuture future = createInvokeFuture(request, request.getInvokeContext());
        //注册调用对象
        conn.addInvokeFuture(future);
        try {
            //add timeout
            //创建超时定时器
            Timeout timeout = TimerHolder.getTimer().newTimeout(new TimerTask() {
                @Override
                public void run(Timeout timeout) throws Exception {
                    InvokeFuture future = conn.removeInvokeFuture(request.getId());
                    if (future != null) {
                        future.putResponse(commandFactory.createTimeoutResponse(conn
                            .getRemoteAddress()));
                    }
                }

            }, timeoutMillis, TimeUnit.MILLISECONDS);
            //设置超时定时器
            future.addTimeout(timeout);

            //写入请求
            conn.getChannel().writeAndFlush(request).addListener(new ChannelFutureListener() {

                @Override
                public void operationComplete(ChannelFuture cf) throws Exception {
                    if (!cf.isSuccess()) {
                        InvokeFuture f = conn.removeInvokeFuture(request.getId());
                        if (f != null) {
                            f.cancelTimeout();
                            f.putResponse(commandFactory.createSendFailedResponse(
                                conn.getRemoteAddress(), cf.cause()));
                        }
                        logger.error("Invoke send failed. The address is {}",
                            RemotingUtil.parseRemoteAddress(conn.getChannel()), cf.cause());
                    }
                }

            });
        } catch (Exception e) {
            InvokeFuture f = conn.removeInvokeFuture(request.getId());
            if (f != null) {
                f.cancelTimeout();
                f.putResponse(commandFactory.createSendFailedResponse(conn.getRemoteAddress(), e));
            }
            logger.error("Exception caught when sending invocation. The address is {}",
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
    // TODO: 2018/4/23 by zmyer
    protected void oneway(final Connection conn, final RemotingCommand request) {
        try {
            //直接写入请求
            conn.getChannel().writeAndFlush(request).addListener(new ChannelFutureListener() {

                @Override
                public void operationComplete(ChannelFuture f) throws Exception {
                    if (!f.isSuccess()) {
                        logger.error("Invoke send failed. The address is {}",
                            RemotingUtil.parseRemoteAddress(conn.getChannel()), f.cause());
                    }
                }

            });
        } catch (Exception e) {
            if (null == conn) {
                logger.error("Conn is null");
            } else {
                logger.error("Exception caught when sending invocation. The address is {}",
                    RemotingUtil.parseRemoteAddress(conn.getChannel()), e);
            }
        }
    }

    /**
     * Create invoke future with {@link InvokeContext}.
     * @param request
     * @param invokeContext
     * @return
     */
    // TODO: 2018/4/23 by zmyer
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
    // TODO: 2018/4/23 by zmyer
    protected abstract InvokeFuture createInvokeFuture(final Connection conn,
                                                       final RemotingCommand request,
                                                       final InvokeContext invokeContext,
                                                       final InvokeCallback invokeCallback);

    // TODO: 2018/4/23 by zmyer
    protected CommandFactory getCommandFactory() {
        return commandFactory;
    }
}
