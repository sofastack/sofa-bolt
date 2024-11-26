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
package com.alipay.remoting.rpc.prehandle;

import com.alipay.remoting.ConnectionEventType;
import com.alipay.remoting.ExtendedNettyChannelHandler;
import com.alipay.remoting.config.BoltClientOption;
import com.alipay.remoting.exception.RemotingException;
import com.alipay.remoting.rpc.BasicUsageTest;
import com.alipay.remoting.rpc.RpcClient;
import com.alipay.remoting.rpc.common.BoltServer;
import com.alipay.remoting.rpc.common.CONNECTEventProcessor;
import com.alipay.remoting.rpc.common.DISCONNECTEventProcessor;
import com.alipay.remoting.rpc.common.PortScan;
import com.alipay.remoting.rpc.common.RequestBody;
import com.alipay.remoting.rpc.common.SimpleClientUserProcessor;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * @author chengyi (mark.lx@antfin.com) 2021-07-19 17:21
 */
public class ExtendHandlerTest {
    static Logger             logger                    = LoggerFactory
                                                            .getLogger(BasicUsageTest.class);

    BoltServer                server;
    RpcClient                 client;

    int                       port                      = PortScan.select();
    String                    addr                      = "127.0.0.1:" + port;

    PreHandleUserProcessor    serverUserProcessor       = new PreHandleUserProcessor();
    SimpleClientUserProcessor clientUserProcessor       = new SimpleClientUserProcessor();
    CONNECTEventProcessor     clientConnectProcessor    = new CONNECTEventProcessor();
    CONNECTEventProcessor     serverConnectProcessor    = new CONNECTEventProcessor();
    DISCONNECTEventProcessor  clientDisConnectProcessor = new DISCONNECTEventProcessor();
    DISCONNECTEventProcessor  serverDisConnectProcessor = new DISCONNECTEventProcessor();

    private final Boolean[]   serverPreHandlerResult    = new Boolean[1];
    private final Boolean[]   serverPostHandlerResult   = new Boolean[1];
    private final Boolean[]   clientPreHandlerResult    = new Boolean[1];
    private final Boolean[]   clientPostHandlerResult   = new Boolean[1];

    @Before
    public void init() {
        server = new BoltServer(port);
        server.getRpcServer().option(BoltClientOption.EXTENDED_NETTY_CHANNEL_HANDLER,
            new ExtendedNettyChannelHandler() {
                @Override
                public List<ChannelHandler> frontChannelHandlers() {
                    ChannelHandler channelHandler = new ChannelInboundHandler() {
                        @Override
                        public void channelRegistered(ChannelHandlerContext ctx) throws Exception {

                        }

                        @Override
                        public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {

                        }

                        @Override
                        public void channelActive(ChannelHandlerContext ctx) throws Exception {

                        }

                        @Override
                        public void channelInactive(ChannelHandlerContext ctx) throws Exception {

                        }

                        @Override
                        public void channelRead(ChannelHandlerContext ctx, Object msg)
                                                                                      throws Exception {
                            serverPreHandlerResult[0] = true;
                            ctx.fireChannelRead(msg);
                        }

                        @Override
                        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {

                        }

                        @Override
                        public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
                                                                                             throws Exception {

                        }

                        @Override
                        public void channelWritabilityChanged(ChannelHandlerContext ctx)
                                                                                        throws Exception {

                        }

                        @Override
                        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
                                                                                               throws Exception {

                        }

                        @Override
                        public void handlerAdded(ChannelHandlerContext ctx) throws Exception {

                        }

                        @Override
                        public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {

                        }
                    };
                    return Collections.singletonList(channelHandler);
                }

                @Override
                public List<ChannelHandler> backChannelHandlers() {
                    ChannelHandler channelHandler = new ChannelInboundHandler() {

                        @Override
                        public void channelRegistered(ChannelHandlerContext ctx) throws Exception {

                        }

                        @Override
                        public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {

                        }

                        @Override
                        public void channelActive(ChannelHandlerContext ctx) throws Exception {

                        }

                        @Override
                        public void channelInactive(ChannelHandlerContext ctx) throws Exception {

                        }

                        @Override
                        public void channelRead(ChannelHandlerContext ctx, Object msg)
                                                                                      throws Exception {
                            serverPostHandlerResult[0] = true;
                            ctx.fireChannelRead(msg);
                        }

                        @Override
                        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {

                        }

                        @Override
                        public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
                                                                                             throws Exception {

                        }

                        @Override
                        public void channelWritabilityChanged(ChannelHandlerContext ctx)
                                                                                        throws Exception {

                        }

                        @Override
                        public void handlerAdded(ChannelHandlerContext ctx) throws Exception {

                        }

                        @Override
                        public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {

                        }

                        @Override
                        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
                                                                                               throws Exception {

                        }
                    };
                    return Collections.singletonList(channelHandler);
                }
            });
        server.start();
        server.addConnectionEventProcessor(ConnectionEventType.CONNECT, serverConnectProcessor);
        server.addConnectionEventProcessor(ConnectionEventType.CLOSE, serverDisConnectProcessor);
        server.registerUserProcessor(serverUserProcessor);

        client = new RpcClient();
        client.option(BoltClientOption.EXTENDED_NETTY_CHANNEL_HANDLER,
            new ExtendedNettyChannelHandler() {
                @Override
                public List<ChannelHandler> frontChannelHandlers() {
                    ChannelHandler channelHandler = new ChannelInboundHandler() {
                        @Override
                        public void channelRegistered(ChannelHandlerContext ctx) throws Exception {

                        }

                        @Override
                        public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {

                        }

                        @Override
                        public void channelActive(ChannelHandlerContext ctx) throws Exception {

                        }

                        @Override
                        public void channelInactive(ChannelHandlerContext ctx) throws Exception {

                        }

                        @Override
                        public void channelRead(ChannelHandlerContext ctx, Object msg)
                                                                                      throws Exception {
                            clientPreHandlerResult[0] = true;
                            ctx.fireChannelRead(msg);
                        }

                        @Override
                        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {

                        }

                        @Override
                        public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
                                                                                             throws Exception {

                        }

                        @Override
                        public void channelWritabilityChanged(ChannelHandlerContext ctx)
                                                                                        throws Exception {

                        }

                        @Override
                        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
                                                                                               throws Exception {

                        }

                        @Override
                        public void handlerAdded(ChannelHandlerContext ctx) throws Exception {

                        }

                        @Override
                        public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {

                        }
                    };
                    return Collections.singletonList(channelHandler);
                }

                @Override
                public List<ChannelHandler> backChannelHandlers() {
                    ChannelHandler channelHandler = new ChannelInboundHandler() {

                        @Override
                        public void channelRegistered(ChannelHandlerContext ctx) throws Exception {

                        }

                        @Override
                        public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {

                        }

                        @Override
                        public void channelActive(ChannelHandlerContext ctx) throws Exception {

                        }

                        @Override
                        public void channelInactive(ChannelHandlerContext ctx) throws Exception {

                        }

                        @Override
                        public void channelRead(ChannelHandlerContext ctx, Object msg)
                                                                                      throws Exception {
                            clientPostHandlerResult[0] = true;
                            ctx.fireChannelRead(msg);
                        }

                        @Override
                        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {

                        }

                        @Override
                        public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
                                                                                             throws Exception {

                        }

                        @Override
                        public void channelWritabilityChanged(ChannelHandlerContext ctx)
                                                                                        throws Exception {

                        }

                        @Override
                        public void handlerAdded(ChannelHandlerContext ctx) throws Exception {

                        }

                        @Override
                        public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {

                        }

                        @Override
                        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
                                                                                               throws Exception {

                        }
                    };
                    return Collections.singletonList(channelHandler);
                }
            });
        client.addConnectionEventProcessor(ConnectionEventType.CONNECT, clientConnectProcessor);
        client.addConnectionEventProcessor(ConnectionEventType.CLOSE, clientDisConnectProcessor);
        client.registerUserProcessor(clientUserProcessor);
        client.init();
    }

    @After
    public void stop() {
        try {
            server.stop();
            Thread.sleep(100);
        } catch (InterruptedException e) {
            logger.error("Stop server failed!", e);
        }
    }

    @Test
    public void testSyncHandle() {
        RequestBody b1 = new RequestBody(1, "hello world sync");
        try {
            Object ret = client.invokeSync(addr, b1, 3000);
            logger.warn("Result received in sync: " + ret);
            Assert.assertEquals("test", ret);
        } catch (RemotingException e) {
            String errMsg = "RemotingException caught in testSyncPreHandle!";
            logger.error(errMsg, e);
            Assert.fail(errMsg);
        } catch (InterruptedException e) {
            String errMsg = "InterruptedException caught in testSyncPreHandle!";
            logger.error(errMsg, e);
            Assert.fail(errMsg);
        }

        Assert.assertTrue(serverPreHandlerResult[0]);
        Assert.assertTrue(serverPostHandlerResult[0]);
        Assert.assertTrue(clientPreHandlerResult[0]);
        Assert.assertTrue(clientPostHandlerResult[0]);
    }
}
