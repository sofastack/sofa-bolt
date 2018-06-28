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
package com.alipay.remoting.connection;

import com.alipay.remoting.*;
import com.alipay.remoting.codec.Codec;
import com.alipay.remoting.log.BoltLoggerFactory;
import com.alipay.remoting.rpc.protocol.RpcProtocol;
import com.alipay.remoting.rpc.protocol.RpcProtocolV2;
import com.alipay.remoting.util.NettyEventLoopUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * ConnectionFactory to create connection.
 *
 * @author chengyi (mark.lx@antfin.com) 2018-06-20 14:29
 */
public abstract class AbstractConnectionFactory implements ConnectionFactory {

    private static final Logger     logger = BoltLoggerFactory
                                               .getLogger(AbstractConnectionFactory.class);

    private final Codec             codec;
    private final ChannelHandler    heartbeatHandler;
    private final ChannelHandler    handler;
    private final PropertiesManager propertiesManager;
    protected EventLoopGroup        workerGroup;
    protected Bootstrap             bootstrap;

    public AbstractConnectionFactory(int threads, NamedThreadFactory threadFactory, Codec codec,
                                     ChannelHandler heartbeatHandler, ChannelHandler handler,
                                     PropertiesManager propertiesManager) {
        if (threads <= 0) {
            throw new IllegalArgumentException("threads must be positive, given: " + threads);
        }
        if (threadFactory == null) {
            throw new IllegalArgumentException("null threadFactory");
        }
        if (codec == null) {
            throw new IllegalArgumentException("null codec");
        }
        if (handler == null) {
            throw new IllegalArgumentException("null handler");
        }
        if (propertiesManager == null) {
            throw new IllegalArgumentException("null propertiesManager");
        }
        this.codec = codec;
        this.heartbeatHandler = heartbeatHandler;
        this.handler = handler;
        this.propertiesManager = propertiesManager;
      
        this.workerGroup = NettyEventLoopUtil.newEventLoopGroup(threads, threadFactory);
    }

    @Override
    public void init(final ConnectionEventHandler connectionEventHandler) {
        bootstrap = new Bootstrap();
        bootstrap.group(workerGroup).channel(NettyEventLoopUtil.getClientSocketChannelClass())
            .option(ChannelOption.TCP_NODELAY, propertiesManager.tcp_nodelay())
            .option(ChannelOption.SO_REUSEADDR, propertiesManager.tcp_so_reuseaddr())
            .option(ChannelOption.SO_KEEPALIVE, propertiesManager.tcp_so_keepalive());

        // init netty write buffer water mark
        initWriteBufferWaterMark();

        // init byte buf allocator
        if (propertiesManager.netty_buffer_pooled()) {
            this.bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        } else {
            this.bootstrap.option(ChannelOption.ALLOCATOR, UnpooledByteBufAllocator.DEFAULT);
        }

        bootstrap.handler(new ChannelInitializer<SocketChannel>() {

            protected void initChannel(SocketChannel channel) {
                ChannelPipeline pipeline = channel.pipeline();
                pipeline.addLast("decoder", codec.newDecoder());
                pipeline.addLast("encoder", codec.newEncoder());

                boolean idleSwitch = propertiesManager.tcp_idle_switch();
                if (idleSwitch) {
                    pipeline.addLast(
                        "idleStateHandler",
                        new IdleStateHandler(propertiesManager.tcp_idle(), propertiesManager
                            .tcp_idle(), 0, TimeUnit.MILLISECONDS));
                    pipeline.addLast("heartbeatHandler", heartbeatHandler);
                }

                pipeline.addLast("connectionEventHandler", connectionEventHandler);
                pipeline.addLast("handler", handler);
            }
        });
    }

    @Override
    public Connection createConnection(Url url) throws Exception {
        Channel channel = doCreateConnection(url.getIp(), url.getPort(), url.getConnectTimeout());
        Connection conn = new Connection(channel, ProtocolCode.fromBytes(url.getProtocol()),
            url.getVersion(), url);
        channel.pipeline().fireUserEventTriggered(ConnectionEventType.CONNECT);
        return conn;
    }

    @Override
    public Connection createConnection(String targetIP, int targetPort, int connectTimeout)
                                                                                           throws Exception {
        Channel channel = doCreateConnection(targetIP, targetPort, connectTimeout);
        Connection conn = new Connection(channel,
            ProtocolCode.fromBytes(RpcProtocol.PROTOCOL_CODE), RpcProtocolV2.PROTOCOL_VERSION_1,
            new Url(targetIP, targetPort));
        channel.pipeline().fireUserEventTriggered(ConnectionEventType.CONNECT);
        return conn;
    }

    @Override
    public Connection createConnection(String targetIP, int targetPort, byte version,
                                       int connectTimeout) throws Exception {
        Channel channel = doCreateConnection(targetIP, targetPort, connectTimeout);
        Connection conn = new Connection(channel,
            ProtocolCode.fromBytes(RpcProtocolV2.PROTOCOL_CODE), version, new Url(targetIP,
                targetPort));
        channel.pipeline().fireUserEventTriggered(ConnectionEventType.CONNECT);
        return conn;
    }

    /**
     * init netty write buffer water mark
     */
    private void initWriteBufferWaterMark() {
        int lowWaterMark = propertiesManager.netty_buffer_low_watermark();
        int highWaterMark = propertiesManager.netty_buffer_high_watermark();
        if (lowWaterMark > highWaterMark) {
            throw new IllegalArgumentException(

                String
                    .format(
                        "[client side] bolt netty high water mark {%s} should not be smaller than low water mark {%s} bytes)",
                        highWaterMark, lowWaterMark));
        } else {
            logger.warn(
                "[client side] bolt netty low water mark is {} bytes, high water mark is {} bytes",
                lowWaterMark, highWaterMark);
        }
        this.bootstrap.option(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(
            lowWaterMark, highWaterMark));
    }

    protected Channel doCreateConnection(String targetIP, int targetPort, int connectTimeout)
                                                                                             throws Exception {
        // prevent unreasonable value, at least 1000
        connectTimeout = Math.max(connectTimeout, 1000);
        String address = targetIP + ":" + targetPort;
        if (logger.isDebugEnabled()) {
            logger.debug("connectTimeout of address [{}] is [{}].", address, connectTimeout);
        }
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout);
        ChannelFuture future = bootstrap.connect(new InetSocketAddress(targetIP, targetPort));

        future.awaitUninterruptibly();
        if (!future.isDone()) {
            String errMsg = "Create connection to " + address + " timeout!";
            logger.warn(errMsg);
            throw new Exception(errMsg);
        }
        if (future.isCancelled()) {
            String errMsg = "Create connection to " + address + " cancelled by user!";
            logger.warn(errMsg);
            throw new Exception(errMsg);
        }
        if (!future.isSuccess()) {
            String errMsg = "Create connection to " + address + " error!";
            logger.warn(errMsg);
            throw new Exception(errMsg, future.cause());
        }
        return future.channel();
    }
}
