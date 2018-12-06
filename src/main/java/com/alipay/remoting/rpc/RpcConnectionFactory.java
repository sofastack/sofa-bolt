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
import com.alipay.remoting.ConnectionEventType;
import com.alipay.remoting.ConnectionFactory;
import com.alipay.remoting.NamedThreadFactory;
import com.alipay.remoting.ProtocolCode;
import com.alipay.remoting.SystemProperties;
import com.alipay.remoting.Url;
import com.alipay.remoting.codec.ProtocolCodeBasedEncoder;
import com.alipay.remoting.log.BoltLoggerFactory;
import com.alipay.remoting.rpc.protocol.RpcProtocol;
import com.alipay.remoting.rpc.protocol.RpcProtocolDecoder;
import com.alipay.remoting.rpc.protocol.RpcProtocolManager;
import com.alipay.remoting.rpc.protocol.RpcProtocolV2;
import com.alipay.remoting.rpc.protocol.UserProcessor;
import com.alipay.remoting.util.StringUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Rpc connection factory, create rpc connections. And generate user triggered event.
 *
 * @author jiangping
 * @version $Id: RpcConnectionFactory.java, v 0.1 2015-9-23 PM3:20:57 tao Exp $
 */
// TODO: 2018/4/23 by zmyer
public class RpcConnectionFactory implements ConnectionFactory {

    /** logger */
    private static final Logger                         logger         = BoltLoggerFactory
                                                                           .getLogger("RpcRemoting");

    //事件循环处理器
    private static final EventLoopGroup                 workerGroup    = new NioEventLoopGroup(
                                                                           Runtime
                                                                               .getRuntime()
                                                                               .availableProcessors() + 1,
                                                                           new NamedThreadFactory(
                                                                               "Rpc-netty-client-worker"));

    //引导对象
    private Bootstrap                                   bootstrap;

    //用户自定义处理器
    private ConcurrentHashMap<String, UserProcessor<?>> userProcessors = new ConcurrentHashMap<String, UserProcessor<?>>(
                                                                           4);

    /**
     * @see com.alipay.remoting.ConnectionFactory#init(ConnectionEventHandler)
     */
    // TODO: 2018/4/23 by zmyer
    @Override
    public void init(final ConnectionEventHandler connectionEventHandler) {
        bootstrap = new Bootstrap();
        bootstrap.group(workerGroup).channel(NioSocketChannel.class)
            .option(ChannelOption.TCP_NODELAY, SystemProperties.tcp_nodelay())
            .option(ChannelOption.SO_REUSEADDR, SystemProperties.tcp_so_reuseaddr())
            .option(ChannelOption.SO_KEEPALIVE, SystemProperties.tcp_so_keepalive());

        /**
         * init netty write buffer water mark
         */
        //初始化写入缓冲区水位
        initWriteBufferWaterMark();

        boolean pooledBuffer = SystemProperties.netty_buffer_pooled();
        if (pooledBuffer) {
            bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        }

        final boolean idleSwitch = SystemProperties.tcp_idle_switch();
        final int idleTime = SystemProperties.tcp_idle();
        //rpc事件处理器
        final RpcHandler rpcHandler = new RpcHandler(userProcessors);
        //心跳处理器
        final HeartbeatHandler heartbeatHandler = new HeartbeatHandler();
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {

            protected void initChannel(SocketChannel channel) throws Exception {
                //流水线对象
                ChannelPipeline pipeline = channel.pipeline();
                //添加解码器
                pipeline.addLast("decoder", new RpcProtocolDecoder(
                    RpcProtocolManager.DEFAULT_PROTOCOL_CODE_LENGTH));
                //添加编码器
                pipeline.addLast(
                    "encoder",
                    new ProtocolCodeBasedEncoder(ProtocolCode
                        .fromBytes(RpcProtocolV2.PROTOCOL_CODE)));
                if (idleSwitch) {
                    //添加空闲状态处理器
                    pipeline.addLast("idleStateHandler", new IdleStateHandler(idleTime, idleTime,
                        0, TimeUnit.MILLISECONDS));
                    //添加心跳处理器
                    pipeline.addLast("heartbeatHandler", heartbeatHandler);
                }
                //添加链接事件管理器
                pipeline.addLast("connectionEventHandler", connectionEventHandler);
                //添加rpc处理器
                pipeline.addLast("handler", rpcHandler);
            }

        });
    }

    /**
     * @see com.alipay.remoting.ConnectionFactory#createConnection(com.alipay.remoting.Url)
     */
    // TODO: 2018/4/23 by zmyer
    @Override
    public Connection createConnection(Url url) throws Exception {
        //创建新的连接
        ChannelFuture future = doCreateConnection(url.getIp(), url.getPort(),
            url.getConnectTimeout());
        //建立连接对象
        Connection conn = new Connection(future.channel(),
            ProtocolCode.fromBytes(url.getProtocol()), url.getVersion(), url);
        //触发连接事件
        future.channel().pipeline().fireUserEventTriggered(ConnectionEventType.CONNECT);
        return conn;
    }

    /**
     * @see com.alipay.remoting.ConnectionFactory#createConnection(java.lang.String, int, int)
     */
    // TODO: 2018/4/23 by zmyer
    @Override
    public Connection createConnection(String targetIP, int targetPort, int connectTimeout)
                                                                                           throws Exception {
        ChannelFuture future = doCreateConnection(targetIP, targetPort, connectTimeout);
        Connection conn = new Connection(future.channel(),
            ProtocolCode.fromBytes(RpcProtocol.PROTOCOL_CODE), RpcProtocolV2.PROTOCOL_VERSION_1,
            new Url(targetIP, targetPort));
        future.channel().pipeline().fireUserEventTriggered(ConnectionEventType.CONNECT);
        return conn;
    }

    /**
     * @see com.alipay.remoting.ConnectionFactory#createConnection(String, int, byte, int)
     */
    // TODO: 2018/4/23 by zmyer
    @Override
    public Connection createConnection(String targetIP, int targetPort, byte version,
                                       int connectTimeout) throws Exception {
        ChannelFuture future = doCreateConnection(targetIP, targetPort, connectTimeout);
        Connection conn = new Connection(future.channel(),
            ProtocolCode.fromBytes(RpcProtocolV2.PROTOCOL_CODE), version, new Url(targetIP,
                targetPort));
        future.channel().pipeline().fireUserEventTriggered(ConnectionEventType.CONNECT);
        return conn;
    }

    /**
     * @param targetIP
     * @param targetPort
     * @param connectTimeout
     * @return
     * @throws Exception
     */
    // TODO: 2018/4/23 by zmyer
    protected ChannelFuture doCreateConnection(String targetIP, int targetPort, int connectTimeout)
                                                                                                   throws Exception {
        // prevent unreasonable value, at least 1000
        connectTimeout = Math.max(connectTimeout, 1000);
        String addr = targetIP + ":" + targetPort;
        if (logger.isDebugEnabled()) {
            logger.debug("connectTimeout of address [{}] is [{}].", addr, connectTimeout);
        }
        //设置连接超时时间
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout);
        //开始连接
        ChannelFuture future = bootstrap.connect(new InetSocketAddress(targetIP, targetPort));
        //等待连接结果
        future.awaitUninterruptibly();
        if (!future.isDone()) {
            String errMsg = "Create connection to " + addr + " timeout!";
            logger.warn(errMsg);
            throw new Exception(errMsg);
        }
        if (future.isCancelled()) {
            String errMsg = "Create connection to " + addr + " cancelled by user!";
            logger.warn(errMsg);
            throw new Exception(errMsg);
        }
        if (!future.isSuccess()) {
            String errMsg = "Create connection to " + addr + " error!";
            logger.warn(errMsg);
            throw new Exception(errMsg, future.cause());
        }
        //返回结果
        return future;
    }

    /**
     * @see com.alipay.remoting.ConnectionFactory#registerUserProcessor(com.alipay.remoting.rpc.protocol.UserProcessor)
     */
    // TODO: 2018/4/23 by zmyer
    @Override
    public void registerUserProcessor(UserProcessor<?> processor) {
        if (processor == null || StringUtils.isBlank(processor.interest())) {
            throw new RuntimeException("User processor or processor interest should not be blank!");
        }
        //注册用户自定义处理器
        UserProcessor<?> preProcessor = this.userProcessors.putIfAbsent(processor.interest(),
            processor);
        if (preProcessor != null) {
            String errMsg = "Processor with interest key ["
                            + processor.interest()
                            + "] has already been registered to rpc client, can not register again!";
            throw new RuntimeException(errMsg);
        }
    }

    /**
     * init netty write buffer water mark
     */
    // TODO: 2018/4/23 by zmyer
    private void initWriteBufferWaterMark() {
        int lowWaterMark = SystemProperties.netty_buffer_low_watermark();
        int highWaterMark = SystemProperties.netty_buffer_high_watermark();
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
        //设置水位
        this.bootstrap.option(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(
            lowWaterMark, highWaterMark));
    }
}