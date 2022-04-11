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

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManagerFactory;

import com.alipay.remoting.config.BoltClientOption;
import com.alipay.remoting.config.BoltGenericOption;
import com.alipay.remoting.config.BoltServerOption;
import com.alipay.remoting.config.Configuration;
import com.alipay.remoting.ExtendedNettyChannelHandler;
import com.alipay.remoting.exception.RemotingException;
import org.slf4j.Logger;

import com.alipay.remoting.Connection;
import com.alipay.remoting.ConnectionEventHandler;
import com.alipay.remoting.ConnectionEventType;
import com.alipay.remoting.NamedThreadFactory;
import com.alipay.remoting.ProtocolCode;
import com.alipay.remoting.Url;
import com.alipay.remoting.codec.Codec;
import com.alipay.remoting.config.ConfigManager;
import com.alipay.remoting.constant.Constants;
import com.alipay.remoting.log.BoltLoggerFactory;
import com.alipay.remoting.rpc.RpcConfigManager;
import com.alipay.remoting.rpc.protocol.RpcProtocol;
import com.alipay.remoting.rpc.protocol.RpcProtocolV2;
import com.alipay.remoting.util.IoUtils;
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
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.flush.FlushConsolidationHandler;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * ConnectionFactory to create connection.
 *
 * @author chengyi (mark.lx@antfin.com) 2018-06-20 14:29
 */
public abstract class AbstractConnectionFactory implements ConnectionFactory {

    private static final Logger         logger      = BoltLoggerFactory
                                                        .getLogger(AbstractConnectionFactory.class);

    private static final EventLoopGroup workerGroup = NettyEventLoopUtil.newEventLoopGroup(Runtime
                                                        .getRuntime().availableProcessors() + 1,
                                                        new NamedThreadFactory(
                                                            "bolt-netty-client-worker", true));

    private final Configuration         configuration;
    private final Codec                 codec;
    private final ChannelHandler        heartbeatHandler;
    private final ChannelHandler        handler;

    protected Bootstrap                 bootstrap;

    public AbstractConnectionFactory(Codec codec, ChannelHandler heartbeatHandler,
                                     ChannelHandler handler, Configuration configuration) {
        if (codec == null) {
            throw new IllegalArgumentException("null codec");
        }
        if (handler == null) {
            throw new IllegalArgumentException("null handler");
        }

        this.configuration = configuration;
        this.codec = codec;
        this.heartbeatHandler = heartbeatHandler;
        this.handler = handler;
    }

    @Override
    public void init(final ConnectionEventHandler connectionEventHandler) {
        bootstrap = new Bootstrap();
        bootstrap.group(workerGroup).channel(NettyEventLoopUtil.getClientSocketChannelClass())
            .option(ChannelOption.TCP_NODELAY, ConfigManager.tcp_nodelay())
            .option(ChannelOption.SO_REUSEADDR, ConfigManager.tcp_so_reuseaddr())
            .option(ChannelOption.SO_KEEPALIVE, ConfigManager.tcp_so_keepalive())
            .option(ChannelOption.SO_SNDBUF, ConfigManager.tcp_so_sndbuf())
            .option(ChannelOption.SO_RCVBUF, ConfigManager.tcp_so_rcvbuf());

        // init netty write buffer water mark
        initWriteBufferWaterMark();

        // init byte buf allocator
        if (ConfigManager.netty_buffer_pooled()) {
            this.bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        } else {
            this.bootstrap.option(ChannelOption.ALLOCATOR, UnpooledByteBufAllocator.DEFAULT);
        }

        final boolean flushConsolidationSwitch = this.configuration
            .option(BoltClientOption.NETTY_FLUSH_CONSOLIDATION);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {

            @Override
            protected void initChannel(SocketChannel channel) {
                ChannelPipeline pipeline = channel.pipeline();
                ExtendedNettyChannelHandler extendedHandlers = configuration
                    .option(BoltClientOption.EXTENDED_NETTY_CHANNEL_HANDLER);
                if (extendedHandlers != null) {
                    List<ChannelHandler> frontHandlers = extendedHandlers.frontChannelHandlers();
                    if (frontHandlers != null) {
                        for (ChannelHandler channelHandler : frontHandlers) {
                            pipeline.addLast(channelHandler.getClass().getName(), channelHandler);
                        }
                    }
                }
                Boolean sslEnable = configuration.option(BoltClientOption.CLI_SSL_ENABLE);
                if (!sslEnable) {
                    // fixme: remove in next version
                    sslEnable = RpcConfigManager.client_ssl_enable();
                }
                if (sslEnable) {
                    SSLEngine engine = initSSLContext().newEngine(channel.alloc());
                    engine.setUseClientMode(true);
                    pipeline.addLast(Constants.SSL_HANDLER, new SslHandler(engine));
                }
                if (flushConsolidationSwitch) {
                    pipeline.addLast("flushConsolidationHandler", new FlushConsolidationHandler(
                        1024, true));
                }
                pipeline.addLast("decoder", codec.newDecoder());
                pipeline.addLast("encoder", codec.newEncoder());

                boolean idleSwitch = ConfigManager.tcp_idle_switch();
                if (idleSwitch) {
                    pipeline.addLast("idleStateHandler",
                        new IdleStateHandler(ConfigManager.tcp_idle(), ConfigManager.tcp_idle(), 0,
                            TimeUnit.MILLISECONDS));
                    pipeline.addLast("heartbeatHandler", heartbeatHandler);
                }

                pipeline.addLast("connectionEventHandler", connectionEventHandler);
                pipeline.addLast("handler", handler);
                if (extendedHandlers != null) {
                    List<ChannelHandler> backHandlers = extendedHandlers.backChannelHandlers();
                    if (backHandlers != null) {
                        for (ChannelHandler channelHandler : backHandlers) {
                            pipeline.addLast(channelHandler.getClass().getName(), channelHandler);
                        }
                    }
                }
            }
        });
    }

    @Override
    public Connection createConnection(Url url) throws Exception {
        Channel channel = doCreateConnection(url.getIp(), url.getPort(), url.getConnectTimeout());
        Connection conn = new Connection(channel, ProtocolCode.fromBytes(url.getProtocol()),
            url.getVersion(), url);
        if (channel.isActive()) {
            channel.pipeline().fireUserEventTriggered(ConnectionEventType.CONNECT);
        } else {
            channel.pipeline().fireUserEventTriggered(ConnectionEventType.CONNECT_FAILED);
            throw new RemotingException("create connection, but channel is inactive, url is "
                                        + url.getOriginUrl());
        }
        return conn;
    }

    @Override
    public Connection createConnection(String targetIP, int targetPort, int connectTimeout)
                                                                                           throws Exception {
        Channel channel = doCreateConnection(targetIP, targetPort, connectTimeout);
        Connection conn = new Connection(channel,
            ProtocolCode.fromBytes(RpcProtocol.PROTOCOL_CODE), RpcProtocolV2.PROTOCOL_VERSION_1,
            new Url(targetIP, targetPort));
        if (channel.isActive()) {
            channel.pipeline().fireUserEventTriggered(ConnectionEventType.CONNECT);
        } else {
            channel.pipeline().fireUserEventTriggered(ConnectionEventType.CONNECT_FAILED);
            throw new RemotingException(
                "create connection, but channel is inactive, target address is " + targetIP + ":"
                        + targetPort);
        }
        return conn;
    }

    @Override
    public Connection createConnection(String targetIP, int targetPort, byte version,
                                       int connectTimeout) throws Exception {
        Channel channel = doCreateConnection(targetIP, targetPort, connectTimeout);
        Connection conn = new Connection(channel,
            ProtocolCode.fromBytes(RpcProtocolV2.PROTOCOL_CODE), version, new Url(targetIP,
                targetPort));
        if (channel.isActive()) {
            channel.pipeline().fireUserEventTriggered(ConnectionEventType.CONNECT);
        } else {
            channel.pipeline().fireUserEventTriggered(ConnectionEventType.CONNECT_FAILED);
            throw new RemotingException(
                "create connection, but channel is inactive, target address is " + targetIP + ":"
                        + targetPort);
        }
        return conn;
    }

    /**
     * init netty write buffer water mark
     */
    private void initWriteBufferWaterMark() {
        // init with system properties
        Integer lowWaterMarkConfig = ConfigManager.netty_buffer_low_watermark();
        if (lowWaterMarkConfig != null) {
            configuration.option(BoltServerOption.NETTY_BUFFER_LOW_WATER_MARK, lowWaterMarkConfig);
        }
        Integer highWaterMarkConfig = ConfigManager.netty_buffer_high_watermark();
        if (highWaterMarkConfig != null) {
            configuration
                .option(BoltServerOption.NETTY_BUFFER_HIGH_WATER_MARK, highWaterMarkConfig);
        }

        int lowWaterMark = configuration.option(BoltGenericOption.NETTY_BUFFER_LOW_WATER_MARK);
        int highWaterMark = configuration.option(BoltGenericOption.NETTY_BUFFER_HIGH_WATER_MARK);
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

    private SslContext initSSLContext() {
        InputStream in = null;
        try {
            String sslKeyStoreType = configuration.option(BoltClientOption.CLI_SSL_KEYSTORE_TYPE);
            if (sslKeyStoreType == null) {
                // fixme: remove in next version
                sslKeyStoreType = RpcConfigManager.client_ssl_keystore_type();
            }
            KeyStore ks = KeyStore.getInstance(sslKeyStoreType);
            String sslKeyStore = configuration.option(BoltClientOption.CLI_SSL_KEYSTORE);
            if (sslKeyStore == null) {
                sslKeyStore = RpcConfigManager.client_ssl_keystore();
            }
            in = new FileInputStream(sslKeyStore);
            String keyStorePass = configuration.option(BoltClientOption.CLI_SSL_KEYSTORE_PASS);
            if (keyStorePass == null) {
                keyStorePass = RpcConfigManager.client_ssl_keystore_pass();
            }
            char[] passChs = keyStorePass.toCharArray();
            ks.load(in, passChs);
            String serverSslAlgorithm = configuration.option(BoltServerOption.SRV_SSL_KMF_ALGO);
            if (serverSslAlgorithm == null) {
                serverSslAlgorithm = RpcConfigManager.server_ssl_kmf_algorithm();
            }
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(serverSslAlgorithm);
            kmf.init(ks, passChs);
            String sslAlgorithm = configuration.option(BoltClientOption.CLI_SSL_TMF_ALGO);
            if (sslAlgorithm == null) {
                sslAlgorithm = RpcConfigManager.client_ssl_tmf_algorithm();
            }
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(sslAlgorithm);
            tmf.init(ks);

            return SslContextBuilder.forClient().keyManager(kmf).trustManager(tmf).build();
        } catch (Exception e) {
            logger.error("Fail to init SSL context for connection factory.", e);
            throw new IllegalStateException("Fail to init SSL context", e);
        } finally {
            IoUtils.closeQuietly(in);
        }

    }

    protected Channel doCreateConnection(String targetIP, int targetPort, int connectTimeout)
                                                                                             throws Exception {
        String address = targetIP + ":" + targetPort;
        if (logger.isDebugEnabled()) {
            logger.debug("connectTimeout of address [{}] is [{}].", address, connectTimeout);
        }
        if (connectTimeout <= 0) {
            throw new IllegalArgumentException(String.format(
                "illegal timeout for creating connection, address: %s, timeout: %d", address,
                connectTimeout));
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
