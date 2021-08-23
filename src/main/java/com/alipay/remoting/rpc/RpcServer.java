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

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManagerFactory;

import com.alipay.remoting.*;
import com.alipay.remoting.config.BoltGenericOption;
import com.alipay.remoting.config.BoltServerOption;
import org.slf4j.Logger;

import com.alipay.remoting.codec.Codec;
import com.alipay.remoting.config.ConfigManager;
import com.alipay.remoting.constant.Constants;
import com.alipay.remoting.exception.RemotingException;
import com.alipay.remoting.log.BoltLoggerFactory;
import com.alipay.remoting.rpc.protocol.UserProcessor;
import com.alipay.remoting.rpc.protocol.UserProcessorRegisterHelper;
import com.alipay.remoting.util.IoUtils;
import com.alipay.remoting.util.NettyEventLoopUtil;
import com.alipay.remoting.util.RemotingUtil;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.flush.FlushConsolidationHandler;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * Server for Rpc.
 *
 * Usage:
 * You can initialize RpcServer with one of the three constructors:
 *   {@link #RpcServer(int)}, {@link #RpcServer(int, boolean)}, {@link #RpcServer(int, boolean, boolean)}
 * Then call start() to start a rpc server, and call stop() to stop a rpc server.
 *
 * Notice:
 *   Once rpc server has been stopped, it can never be start again. You should init another instance of RpcServer to use.
 *
 * @author jiangping
 * @version $Id: RpcServer.java, v 0.1 2015-8-31 PM5:22:22 tao Exp $
 */
public class RpcServer extends AbstractRemotingServer {

    /** logger */
    private static final Logger                         logger                  = BoltLoggerFactory
                                                                                    .getLogger("RpcRemoting");
    /** server bootstrap */
    private ServerBootstrap                             bootstrap;

    /** channelFuture */
    private ChannelFuture                               channelFuture;

    /** connection event handler */
    private ConnectionEventHandler                      connectionEventHandler;

    /** connection event listener */
    private ConnectionEventListener                     connectionEventListener = new ConnectionEventListener();

    /** user processors of rpc server */
    private ConcurrentHashMap<String, UserProcessor<?>> userProcessors          = new ConcurrentHashMap<String, UserProcessor<?>>(
                                                                                    4);

    /** boss event loop group, boss group should not be daemon, need shutdown manually*/
    private final EventLoopGroup                        bossGroup               = NettyEventLoopUtil
                                                                                    .newEventLoopGroup(
                                                                                        1,
                                                                                        new NamedThreadFactory(
                                                                                            "Rpc-netty-server-boss",
                                                                                            false));
    /** worker event loop group. Reuse I/O worker threads between rpc servers. */
    private static final EventLoopGroup                 workerGroup             = NettyEventLoopUtil
                                                                                    .newEventLoopGroup(
                                                                                        Runtime
                                                                                            .getRuntime()
                                                                                            .availableProcessors() * 2,
                                                                                        new NamedThreadFactory(
                                                                                            "Rpc-netty-server-worker",
                                                                                            true));

    /** address parser to get custom args */
    private RemotingAddressParser                       addressParser;

    /** connection manager */
    private DefaultServerConnectionManager              connectionManager;

    /** rpc remoting */
    protected RpcRemoting                               rpcRemoting;

    /** rpc codec */
    private Codec                                       codec                   = new RpcCodec();

    static {
        if (workerGroup instanceof NioEventLoopGroup) {
            ((NioEventLoopGroup) workerGroup).setIoRatio(ConfigManager.netty_io_ratio());
        } else if (workerGroup instanceof EpollEventLoopGroup) {
            ((EpollEventLoopGroup) workerGroup).setIoRatio(ConfigManager.netty_io_ratio());
        }
    }

    /**
     * Construct a rpc server with random port <br>
     * the random port will determined after server startup
     * Note:<br>
     * You can only use invoke methods with params {@link Connection}, for example {@link #invokeSync(Connection, Object, int)} <br>
     * Otherwise {@link UnsupportedOperationException} will be thrown.
     */
    public RpcServer() {
        this(false);
    }

    /**
     * Construct a rpc server with random port <br>
     * the random port will determined after server startup
     * Note:<br>
     * You can only use invoke methods with params {@link Connection}, for example {@link #invokeSync(Connection, Object, int)} <br>
     * Otherwise {@link UnsupportedOperationException} will be thrown.
     *
     * @param manageConnection true to enable connection management feature
     */
    public RpcServer(boolean manageConnection) {
        this(0, manageConnection);
    }

    /**
     * Construct a rpc server. <br>
     *
     * Note:<br>
     * You can only use invoke methods with params {@link Connection}, for example {@link #invokeSync(Connection, Object, int)} <br>
     * Otherwise {@link UnsupportedOperationException} will be thrown.
     */
    public RpcServer(int port) {
        this(port, false);
    }

    /**
     * Construct a rpc server. <br>
     *
     * Note:<br>
     * You can only use invoke methods with params {@link Connection}, for example {@link #invokeSync(Connection, Object, int)} <br>
     * Otherwise {@link UnsupportedOperationException} will be thrown.
     */
    public RpcServer(String ip, int port) {
        this(ip, port, false);
    }

    /**
     * Construct a rpc server. <br>
     *
     * <ul>
     * <li>You can enable connection management feature by specify @param manageConnection true.</li>
     * <ul>
     * <li>When connection management feature enabled, you can use all invoke methods with params {@link String}, {@link Url}, {@link Connection} methods.</li>
     * <li>When connection management feature disabled, you can only use invoke methods with params {@link Connection}, otherwise {@link UnsupportedOperationException} will be thrown.</li>
     * </ul>
     * </ul>
     *
     * @param port listened port
     * @param manageConnection true to enable connection management feature
     */
    public RpcServer(int port, boolean manageConnection) {
        super(port);
        /* server connection management feature enabled or not, default value false, means disabled. */
        if (manageConnection) {
            option(BoltServerOption.SERVER_MANAGE_CONNECTION_SWITCH, true);
        }
    }

    /**
     * Construct a rpc server. <br>
     *
     * <ul>
     * <li>You can enable connection management feature by specify @param manageConnection true.</li>
     * <ul>
     * <li>When connection management feature enabled, you can use all invoke methods with params {@link String}, {@link Url}, {@link Connection} methods.</li>
     * <li>When connection management feature disabled, you can only use invoke methods with params {@link Connection}, otherwise {@link UnsupportedOperationException} will be thrown.</li>
     * </ul>
     * </ul>
     *
     * @param port listened port
     * @param manageConnection true to enable connection management feature
     */
    public RpcServer(String ip, int port, boolean manageConnection) {
        super(ip, port);
        /* server connection management feature enabled or not, default value false, means disabled. */
        if (manageConnection) {
            option(BoltServerOption.SERVER_MANAGE_CONNECTION_SWITCH, true);
        }
    }

    /**
     * Construct a rpc server. <br>
     *
     * You can construct a rpc server with synchronous or asynchronous stop strategy by {@param syncStop}.
     *
     * @param port listened port
     * @param manageConnection manage connection
     * @param syncStop true to enable stop in synchronous way
     */
    public RpcServer(int port, boolean manageConnection, boolean syncStop) {
        this(port, manageConnection);
        if (syncStop) {
            option(BoltServerOption.SERVER_SYNC_STOP, true);
        }
    }

    @Override
    protected void doInit() {
        if (this.addressParser == null) {
            this.addressParser = new RpcAddressParser();
        }
        if (option(BoltServerOption.SERVER_MANAGE_CONNECTION_SWITCH)) {
            // in server side, do not care the connection service state, so use null instead of global switch
            ConnectionSelectStrategy connectionSelectStrategy = new RandomSelectStrategy(this);
            this.connectionManager = new DefaultServerConnectionManager(connectionSelectStrategy);
            this.connectionManager.startup();

            this.connectionEventHandler = new RpcConnectionEventHandler(this);
            this.connectionEventHandler.setConnectionManager(this.connectionManager);
            this.connectionEventHandler.setConnectionEventListener(this.connectionEventListener);
        } else {
            this.connectionEventHandler = new ConnectionEventHandler(this);
            this.connectionEventHandler.setConnectionEventListener(this.connectionEventListener);
        }
        initRpcRemoting();

        Integer tcpSoSndBuf = option(BoltGenericOption.TCP_SO_SNDBUF);
        Integer tcpSoRcvBuf = option(BoltGenericOption.TCP_SO_RCVBUF);

        this.bootstrap = new ServerBootstrap();
        this.bootstrap
            .group(bossGroup, workerGroup)
            .channel(NettyEventLoopUtil.getServerSocketChannelClass())
            .option(ChannelOption.SO_BACKLOG, ConfigManager.tcp_so_backlog())
            .option(ChannelOption.SO_REUSEADDR, ConfigManager.tcp_so_reuseaddr())
            .childOption(ChannelOption.TCP_NODELAY, ConfigManager.tcp_nodelay())
            .childOption(ChannelOption.SO_KEEPALIVE, ConfigManager.tcp_so_keepalive())
            .childOption(ChannelOption.SO_SNDBUF,
                tcpSoSndBuf != null ? tcpSoSndBuf : ConfigManager.tcp_so_sndbuf())
            .childOption(ChannelOption.SO_RCVBUF,
                tcpSoRcvBuf != null ? tcpSoRcvBuf : ConfigManager.tcp_so_rcvbuf());

        // set write buffer water mark
        initWriteBufferWaterMark();

        // init byte buf allocator
        if (ConfigManager.netty_buffer_pooled()) {
            this.bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        } else {
            this.bootstrap.option(ChannelOption.ALLOCATOR, UnpooledByteBufAllocator.DEFAULT)
                .childOption(ChannelOption.ALLOCATOR, UnpooledByteBufAllocator.DEFAULT);
        }

        // enable trigger mode for epoll if need
        NettyEventLoopUtil.enableTriggeredMode(bootstrap);

        final boolean idleSwitch = ConfigManager.tcp_idle_switch();
        final boolean flushConsolidationSwitch = option(BoltServerOption.NETTY_FLUSH_CONSOLIDATION);
        final int idleTime = ConfigManager.tcp_server_idle();
        final ChannelHandler serverIdleHandler = new ServerIdleHandler();
        final RpcHandler rpcHandler = new RpcHandler(true, this.userProcessors);
        this.bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {

            @Override
            protected void initChannel(SocketChannel channel) {
                ChannelPipeline pipeline = channel.pipeline();
                ExtendedNettyChannelHandler extendedHandlers = option(BoltServerOption.EXTENDED_NETTY_CHANNEL_HANDLER);
                if (extendedHandlers != null) {
                    List<ChannelHandler> frontHandlers = extendedHandlers.frontChannelHandlers();
                    if (frontHandlers != null) {
                        for (ChannelHandler channelHandler : frontHandlers) {
                            pipeline.addLast(channelHandler.getClass().getName(), channelHandler);
                        }
                    }
                }

                Boolean sslEnable = option(BoltServerOption.SRV_SSL_ENABLE);
                if (!sslEnable) {
                    // fixme: remove in next version
                    sslEnable = RpcConfigManager.server_ssl_enable();
                }
                if (sslEnable) {
                    SSLEngine engine = initSSLContext().newEngine(channel.alloc());
                    engine.setUseClientMode(false);
                    // fixme: update in next version
                    engine.setNeedClientAuth(option(BoltServerOption.SRV_SSL_NEED_CLIENT_AUTH)
                                             || RpcConfigManager.server_ssl_need_client_auth());
                    pipeline.addLast(Constants.SSL_HANDLER, new SslHandler(engine));
                }

                if (flushConsolidationSwitch) {
                    pipeline.addLast("flushConsolidationHandler", new FlushConsolidationHandler(
                        1024, true));
                }
                pipeline.addLast("decoder", codec.newDecoder());
                pipeline.addLast("encoder", codec.newEncoder());
                if (idleSwitch) {
                    pipeline.addLast("idleStateHandler", new IdleStateHandler(0, 0, idleTime,
                        TimeUnit.MILLISECONDS));
                    pipeline.addLast("serverIdleHandler", serverIdleHandler);
                }
                pipeline.addLast("connectionEventHandler", connectionEventHandler);
                pipeline.addLast("handler", rpcHandler);
                if (extendedHandlers != null) {
                    List<ChannelHandler> backHandlers = extendedHandlers.backChannelHandlers();
                    if (backHandlers != null) {
                        for (ChannelHandler channelHandler : backHandlers) {
                            pipeline.addLast(channelHandler.getClass().getName(), channelHandler);
                        }
                    }
                }
                createConnection(channel);
            }

            /**
             * create connection operation<br>
             * <ul>
             * <li>If flag manageConnection be true, use {@link DefaultConnectionManager} to add a new connection, meanwhile bind it with the channel.</li>
             * <li>If flag manageConnection be false, just create a new connection and bind it with the channel.</li>
             * </ul>
             */
            private void createConnection(SocketChannel channel) {
                Url url = addressParser.parse(RemotingUtil.parseRemoteAddress(channel));
                if (option(BoltServerOption.SERVER_MANAGE_CONNECTION_SWITCH)) {
                    connectionManager.add(new Connection(channel, url), url.getUniqueKey());
                } else {
                    new Connection(channel, url);
                }
                channel.pipeline().fireUserEventTriggered(ConnectionEventType.CONNECT);
            }
        });
    }

    private SslContext initSSLContext() {
        InputStream in = null;
        try {
            String keyStoreType = option(BoltServerOption.SRV_SSL_KEYSTORE_TYPE);
            if (keyStoreType == null) {
                keyStoreType = RpcConfigManager.server_ssl_keystore_type();
            }
            KeyStore ks = KeyStore.getInstance(keyStoreType);
            String filePath = option(BoltServerOption.SRV_SSL_KEYSTORE);
            if (filePath == null) {
                filePath = RpcConfigManager.server_ssl_keystore();
            }
            in = new FileInputStream(filePath);

            String keyStorePass = option(BoltServerOption.SRV_SSL_KEYSTORE_PASS);
            if (keyStorePass == null) {
                keyStorePass = RpcConfigManager.server_ssl_keystore_pass();
            }
            char[] passChs = keyStorePass.toCharArray();
            ks.load(in, passChs);
            String sslAlgorithm = RpcConfigManager.server_ssl_kmf_algorithm();
            if (sslAlgorithm == null) {
                sslAlgorithm = option(BoltServerOption.SRV_SSL_KMF_ALGO);
            }
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(sslAlgorithm);
            kmf.init(ks, passChs);

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(RpcConfigManager
                .client_ssl_tmf_algorithm());
            tmf.init(ks);
            return SslContextBuilder.forServer(kmf).trustManager(tmf).build();
        } catch (Exception e) {
            logger.error("Fail to init SSL context for server.", e);
            throw new IllegalStateException("Fail to init SSL context", e);
        } finally {
            IoUtils.closeQuietly(in);
        }

    }

    @Override
    protected boolean doStart() throws InterruptedException {
        for (UserProcessor<?> userProcessor : userProcessors.values()) {
            if (!userProcessor.isStarted()) {
                userProcessor.startup();
            }
        }

        this.channelFuture = this.bootstrap.bind(new InetSocketAddress(ip(), port())).sync();
        if (port() == 0 && channelFuture.isSuccess()) {
            InetSocketAddress localAddress = (InetSocketAddress) channelFuture.channel()
                .localAddress();
            setLocalBindingPort(localAddress.getPort());
            logger.info("rpc server start with random port: {}!", port());
        }
        return this.channelFuture.isSuccess();
    }

    /**
     * Notice: only RpcServer#option(BoltServerOption.SERVER_MANAGE_CONNECTION_SWITCH, true) switch on, will close all connections.
     *
     * @see AbstractRemotingServer#doStop()
     */
    @Override
    protected boolean doStop() {
        if (null != this.channelFuture) {
            this.channelFuture.channel().close();
        }
        if (option(BoltServerOption.SERVER_SYNC_STOP)) {
            this.bossGroup.shutdownGracefully().awaitUninterruptibly();
        } else {
            this.bossGroup.shutdownGracefully();
        }
        if (option(BoltServerOption.SERVER_MANAGE_CONNECTION_SWITCH)
            && null != this.connectionManager) {
            this.connectionManager.shutdown();
            logger.warn("Close all connections from server side!");
        }
        for (UserProcessor<?> userProcessor : userProcessors.values()) {
            if (userProcessor.isStarted()) {
                userProcessor.shutdown();
            }
        }
        logger.warn("Rpc Server stopped!");
        return true;
    }

    /**
     * init rpc remoting
     */
    protected void initRpcRemoting() {
        this.rpcRemoting = new RpcServerRemoting(new RpcCommandFactory(), this.addressParser,
            this.connectionManager);
    }

    /**
     * @see RemotingServer#registerProcessor(byte, com.alipay.remoting.CommandCode, com.alipay.remoting.RemotingProcessor)
     */
    @Override
    public void registerProcessor(byte protocolCode, CommandCode cmd, RemotingProcessor<?> processor) {
        ProtocolManager.getProtocol(ProtocolCode.fromBytes(protocolCode)).getCommandHandler()
            .registerProcessor(cmd, processor);
    }

    /**
     * @see RemotingServer#registerDefaultExecutor(byte, ExecutorService)
     */
    @Override
    public void registerDefaultExecutor(byte protocolCode, ExecutorService executor) {
        ProtocolManager.getProtocol(ProtocolCode.fromBytes(protocolCode)).getCommandHandler()
            .registerDefaultExecutor(executor);
    }

    /**
     * Add processor to process connection event.
     *
     * @param type connection event type
     * @param processor connection event processor
     */
    public void addConnectionEventProcessor(ConnectionEventType type,
                                            ConnectionEventProcessor processor) {
        this.connectionEventListener.addConnectionEventProcessor(type, processor);
    }

    /**
     * Use UserProcessorRegisterHelper{@link UserProcessorRegisterHelper} to help register user processor for server side.
     *
     * @see AbstractRemotingServer#registerUserProcessor(com.alipay.remoting.rpc.protocol.UserProcessor)
     */
    @Override
    public void registerUserProcessor(UserProcessor<?> processor) {
        UserProcessorRegisterHelper.registerUserProcessor(processor, this.userProcessors);
        // startup the processor if it registered after component startup
        if (isStarted() && !processor.isStarted()) {
            processor.startup();
        }
    }

    /**
     * One way invocation using a string address, address format example - 127.0.0.1:12200?key1=value1&key2=value2 <br>
     * <p>
     * Notice:<br>
     *   <ol>
     *   <li><b>DO NOT modify the request object concurrently when this method is called.</b></li>
     *   <li>When do invocation, use the string address to find a available client connection, if none then throw exception</li>
     *   <li>Unlike rpc client, address arguments takes no effect here, for rpc server will not create connection.</li>
     *   </ol>
     *
     * @param addr address addr
     * @param request request request
     * @throws RemotingException remoting exception remoting exception
     * @throws InterruptedException interrupted exception
     */
    public void oneway(final String addr, final Object request) throws RemotingException,
                                                               InterruptedException {
        ensureStarted();
        check();
        this.rpcRemoting.oneway(addr, request, null);
    }

    /**
     * One way invocation with a {@link InvokeContext}, common api notice please see {@link #oneway(String, Object)}
     *
     * @param addr address
     * @param request request
     * @param invokeContext invokeContext
     * @throws RemotingException remoting exception
     * @throws InterruptedException interrupted exception
     */
    public void oneway(final String addr, final Object request, final InvokeContext invokeContext)
                                                                                                  throws RemotingException,
                                                                                                  InterruptedException {
        ensureStarted();
        check();
        this.rpcRemoting.oneway(addr, request, invokeContext);
    }

    /**
     * One way invocation using a parsed {@link Url} <br>
     * <p>
     * Notice:<br>
     *   <ol>
     *   <li><b>DO NOT modify the request object concurrently when this method is called.</b></li>
     *   <li>When do invocation, use the parsed {@link Url} to find a available client connection, if none then throw exception</li>
     *   </ol>
     *
     * @param url url
     * @param request request request
     * @throws RemotingException remoting exception remoting exception
     * @throws InterruptedException interrupted exception
     */
    public void oneway(final Url url, final Object request) throws RemotingException,
                                                           InterruptedException {
        ensureStarted();
        check();
        this.rpcRemoting.oneway(url, request, null);
    }

    /**
     * One way invocation with a {@link InvokeContext}, common api notice please see {@link #oneway(Url, Object)}
     *
     * @param url url
     * @param request request request
     * @param invokeContext invokeContext invokeContext
     * @throws RemotingException remoting exception remoting exception
     * @throws InterruptedException interrupted exception
     */
    public void oneway(final Url url, final Object request, final InvokeContext invokeContext)
                                                                                              throws RemotingException,
                                                                                              InterruptedException {
        ensureStarted();
        check();
        this.rpcRemoting.oneway(url, request, invokeContext);
    }

    /**
     * One way invocation using a {@link Connection} <br>
     * <p>
     * Notice:<br>
     *   <b>DO NOT modify the request object concurrently when this method is called.</b>
     *
     * @param conn connection connection
     * @param request request request
     * @throws RemotingException remoting exception remoting exception
     */
    public void oneway(final Connection conn, final Object request) throws RemotingException {
        ensureStarted();
        this.rpcRemoting.oneway(conn, request, null);
    }

    /**
     * One way invocation with a {@link InvokeContext}, common api notice please see {@link #oneway(Connection, Object)}
     *
     * @param conn connection connection
     * @param request request request
     * @param invokeContext invokeContext invokeContext
     * @throws RemotingException remoting exception remoting exception
     */
    public void oneway(final Connection conn, final Object request,
                       final InvokeContext invokeContext) throws RemotingException {
        ensureStarted();
        this.rpcRemoting.oneway(conn, request, invokeContext);
    }

    /**
     * Synchronous invocation using a string address, address format example - 127.0.0.1:12200?key1=value1&key2=value2 <br>
     * <p>
     * Notice:<br>
     *   <ol>
     *   <li><b>DO NOT modify the request object concurrently when this method is called.</b></li>
     *   <li>When do invocation, use the string address to find a available client connection, if none then throw exception</li>
     *   <li>Unlike rpc client, address arguments takes no effect here, for rpc server will not create connection.</li>
     *   </ol>
     *
     * @param addr address addr
     * @param request request request
     * @param timeoutMillis timeoutMillis timeoutMillis
     * @return Object
     * @throws RemotingException remoting exception remoting exception
     * @throws InterruptedException interrupted exception
     */
    public Object invokeSync(final String addr, final Object request, final int timeoutMillis)
                                                                                              throws RemotingException,
                                                                                              InterruptedException {
        ensureStarted();
        check();
        return this.rpcRemoting.invokeSync(addr, request, null, timeoutMillis);
    }

    /**
     * Synchronous invocation with a {@link InvokeContext}, common api notice please see {@link #invokeSync(String, Object, int)}
     *
     * @param addr address addr
     * @param request request request
     * @param invokeContext invokeContext invokeContext
     * @param timeoutMillis timeoutMillis timeoutMillis
     * @return object
     * @throws RemotingException remoting exception remoting exception
     * @throws InterruptedException interrupted exception
     */
    public Object invokeSync(final String addr, final Object request,
                             final InvokeContext invokeContext, final int timeoutMillis)
                                                                                        throws RemotingException,
                                                                                        InterruptedException {
        ensureStarted();
        check();
        return this.rpcRemoting.invokeSync(addr, request, invokeContext, timeoutMillis);
    }

    /**
     * Synchronous invocation using a parsed {@link Url} <br>
     * <p>
     * Notice:<br>
     *   <ol>
     *   <li><b>DO NOT modify the request object concurrently when this method is called.</b></li>
     *   <li>When do invocation, use the parsed {@link Url} to find a available client connection, if none then throw exception</li>
     *   </ol>
     *
     * @param url url
     * @param request request request
     * @param timeoutMillis timeoutMillis timeoutMillis
     * @return Object
     * @throws RemotingException remoting exception remoting exception
     * @throws InterruptedException interrupted exception
     */
    public Object invokeSync(Url url, Object request, int timeoutMillis) throws RemotingException,
                                                                        InterruptedException {
        ensureStarted();
        check();
        return this.rpcRemoting.invokeSync(url, request, null, timeoutMillis);
    }

    /**
     * Synchronous invocation with a {@link InvokeContext}, common api notice please see {@link #invokeSync(Url, Object, int)}
     *
     * @param url url
     * @param request request
     * @param invokeContext invokeContext
     * @param timeoutMillis timeoutMillis
     * @return object
     * @throws RemotingException remoting exception remoting exception
     * @throws InterruptedException interrupted exception
     */
    public Object invokeSync(final Url url, final Object request,
                             final InvokeContext invokeContext, final int timeoutMillis)
                                                                                        throws RemotingException,
                                                                                        InterruptedException {
        ensureStarted();
        check();
        return this.rpcRemoting.invokeSync(url, request, invokeContext, timeoutMillis);
    }

    /**
     * Synchronous invocation using a {@link Connection} <br>
     * <p>
     * Notice:<br>
     *   <b>DO NOT modify the request object concurrently when this method is called.</b>
     *
     * @param conn connection
     * @param request request
     * @param timeoutMillis timeoutMillis
     * @return Object
     * @throws RemotingException remoting exception remoting exception
     * @throws InterruptedException interrupted exception
     */
    public Object invokeSync(final Connection conn, final Object request, final int timeoutMillis)
                                                                                                  throws RemotingException,
                                                                                                  InterruptedException {
        ensureStarted();
        return this.rpcRemoting.invokeSync(conn, request, null, timeoutMillis);
    }

    /**
     * Synchronous invocation with a {@link InvokeContext}, common api notice please see {@link #invokeSync(Connection, Object, int)}
     *
     * @param conn connection connection
     * @param request request request
     * @param invokeContext invokeContext invokeContext
     * @param timeoutMillis timeoutMillis timeoutMillis
     * @return object
     * @throws RemotingException remoting exception remoting exception
     * @throws InterruptedException interrupted exception
     */
    public Object invokeSync(final Connection conn, final Object request,
                             final InvokeContext invokeContext, final int timeoutMillis)
                                                                                        throws RemotingException,
                                                                                        InterruptedException {
        ensureStarted();
        return this.rpcRemoting.invokeSync(conn, request, invokeContext, timeoutMillis);
    }

    /**
     * Future invocation using a string address, address format example - 127.0.0.1:12200?key1=value1&key2=value2 <br>
     * You can get result use the returned {@link RpcResponseFuture}.
     * <p>
     * Notice:<br>
     *   <ol>
     *   <li><b>DO NOT modify the request object concurrently when this method is called.</b></li>
     *   <li>When do invocation, use the string address to find a available client connection, if none then throw exception</li>
     *   <li>Unlike rpc client, address arguments takes no effect here, for rpc server will not create connection.</li>
     *   </ol>
     *
     * @param addr address address
     * @param request request request
     * @param timeoutMillis timeoutMillis timeoutMillis
     * @return RpcResponseFuture
     * @throws RemotingException remoting exception remoting exception
     * @throws InterruptedException interrupted exception
     */
    public RpcResponseFuture invokeWithFuture(final String addr, final Object request,
                                              final int timeoutMillis) throws RemotingException,
                                                                      InterruptedException {
        ensureStarted();
        check();
        return this.rpcRemoting.invokeWithFuture(addr, request, null, timeoutMillis);
    }

    /**
     * Future invocation with a {@link InvokeContext}, common api notice please see {@link #invokeWithFuture(String, Object, int)}
     *
     * @param addr address address
     * @param request request request
     * @param invokeContext invokeContext invokeContext
     * @param timeoutMillis timeoutMillis timeoutMillis
     * @return RpcResponseFuture
     * @throws RemotingException remoting exception remoting exception
     * @throws InterruptedException interrupted exception
     */
    public RpcResponseFuture invokeWithFuture(final String addr, final Object request,
                                              final InvokeContext invokeContext,
                                              final int timeoutMillis) throws RemotingException,
                                                                      InterruptedException {
        ensureStarted();
        check();
        return this.rpcRemoting.invokeWithFuture(addr, request, invokeContext, timeoutMillis);
    }

    /**
     * Future invocation using a parsed {@link Url} <br>
     * You can get result use the returned {@link RpcResponseFuture}.
     * <p>
     * Notice:<br>
     *   <ol>
     *   <li><b>DO NOT modify the request object concurrently when this method is called.</b></li>
     *   <li>When do invocation, use the parsed {@link Url} to find a available client connection, if none then throw exception</li>
     *   </ol>
     *
     * @param url url
     * @param request request request
     * @param timeoutMillis timeoutMillis timeoutMillis
     * @return RpcResponseFuture
     * @throws RemotingException remoting exception remoting exception
     * @throws InterruptedException interrupted exception
     */
    public RpcResponseFuture invokeWithFuture(final Url url, final Object request,
                                              final int timeoutMillis) throws RemotingException,
                                                                      InterruptedException {
        ensureStarted();
        check();
        return this.rpcRemoting.invokeWithFuture(url, request, null, timeoutMillis);
    }

    /**
     * Future invocation with a {@link InvokeContext}, common api notice please see {@link #invokeWithFuture(Url, Object, int)}
     *
     * @param url url
     * @param request request request
     * @param invokeContext invokeContext
     * @param timeoutMillis timeoutMillis
     * @return RpcResponseFuture
     * @throws RemotingException remoting exception remoting exception
     * @throws InterruptedException interrupted exception
     */
    public RpcResponseFuture invokeWithFuture(final Url url, final Object request,
                                              final InvokeContext invokeContext,
                                              final int timeoutMillis) throws RemotingException,
                                                                      InterruptedException {
        ensureStarted();
        check();
        return this.rpcRemoting.invokeWithFuture(url, request, invokeContext, timeoutMillis);
    }

    /**
     * Future invocation using a {@link Connection} <br>
     * You can get result use the returned {@link RpcResponseFuture}.
     * <p>
     * Notice:<br>
     *   <b>DO NOT modify the request object concurrently when this method is called.</b>
     *
     * @param conn connection
     * @param request request
     * @param timeoutMillis timeoutMillis
     * @return RpcResponseFuture
     * @throws RemotingException remoting exception
     */
    public RpcResponseFuture invokeWithFuture(final Connection conn, final Object request,
                                              final int timeoutMillis) throws RemotingException {

        ensureStarted();
        return this.rpcRemoting.invokeWithFuture(conn, request, null, timeoutMillis);
    }

    /**
     * Future invocation with a {@link InvokeContext}, common api notice please see {@link #invokeWithFuture(Connection, Object, int)}
     *
     * @param conn connection connection
     * @param request request request
     * @param invokeContext invokeContext invokeContext
     * @param timeoutMillis timeoutMillis timeoutMillis
     * @return RpcResponseFuture
     * @throws RemotingException remoting exception
     */
    public RpcResponseFuture invokeWithFuture(final Connection conn, final Object request,
                                              final InvokeContext invokeContext,
                                              final int timeoutMillis) throws RemotingException {

        ensureStarted();
        return this.rpcRemoting.invokeWithFuture(conn, request, invokeContext, timeoutMillis);
    }

    /**
     * Callback invocation using a string address, address format example - 127.0.0.1:12200?key1=value1&key2=value2 <br>
     * You can specify an implementation of {@link InvokeCallback} to get the result.
     * <p>
     * Notice:<br>
     *   <ol>
     *   <li><b>DO NOT modify the request object concurrently when this method is called.</b></li>
     *   <li>When do invocation, use the string address to find a available client connection, if none then throw exception</li>
     *   <li>Unlike rpc client, address arguments takes no effect here, for rpc server will not create connection.</li>
     *   </ol>
     *
     * @param addr address
     * @param request request
     * @param invokeCallback invokeCallback
     * @param timeoutMillis timeoutMillis
     * @throws RemotingException remoting exception remoting exception
     * @throws InterruptedException interrupted exception
     */
    public void invokeWithCallback(final String addr, final Object request,
                                   final InvokeCallback invokeCallback, final int timeoutMillis)
                                                                                                throws RemotingException,
                                                                                                InterruptedException {
        ensureStarted();
        check();
        this.rpcRemoting.invokeWithCallback(addr, request, null, invokeCallback, timeoutMillis);
    }

    /**
     * Callback invocation with a {@link InvokeContext}, common api notice please see {@link #invokeWithCallback(String, Object, InvokeCallback, int)}
     *
     * @param addr address address
     * @param request request request
     * @param invokeContext invokeContext
     * @param invokeCallback invokeCallback
     * @param timeoutMillis timeoutMillis
     * @throws RemotingException remoting exception remoting exception
     * @throws InterruptedException interrupted exception
     */
    public void invokeWithCallback(final String addr, final Object request,
                                   final InvokeContext invokeContext,
                                   final InvokeCallback invokeCallback, final int timeoutMillis)
                                                                                                throws RemotingException,
                                                                                                InterruptedException {
        ensureStarted();
        check();
        this.rpcRemoting.invokeWithCallback(addr, request, invokeContext, invokeCallback,
            timeoutMillis);
    }

    /**
     * Callback invocation using a parsed {@link Url} <br>
     * You can specify an implementation of {@link InvokeCallback} to get the result.
     * <p>
     * Notice:<br>
     *   <ol>
     *   <li><b>DO NOT modify the request object concurrently when this method is called.</b></li>
     *   <li>When do invocation, use the parsed {@link Url} to find a available client connection, if none then throw exception</li>
     *   </ol>
     *
     * @param url url
     * @param request request request
     * @param invokeCallback invokeCallback invokeCallback
     * @param timeoutMillis timeoutMillis timeoutMillis
     * @throws RemotingException remoting exception remoting exception
     * @throws InterruptedException interrupted exception
     */
    public void invokeWithCallback(final Url url, final Object request,
                                   final InvokeCallback invokeCallback, final int timeoutMillis)
                                                                                                throws RemotingException,
                                                                                                InterruptedException {
        ensureStarted();
        check();
        this.rpcRemoting.invokeWithCallback(url, request, null, invokeCallback, timeoutMillis);
    }

    /**
     * Callback invocation with a {@link InvokeContext}, common api notice please see {@link #invokeWithCallback(Url, Object, InvokeCallback, int)}
     *
     * @param url url
     * @param request request request
     * @param invokeContext invokeContext invokeContext
     * @param invokeCallback invokeCallback invokeCallback
     * @param timeoutMillis timeoutMillis timeoutMillis
     * @throws RemotingException remoting exception remoting exception
     * @throws InterruptedException interrupted exception
     */
    public void invokeWithCallback(final Url url, final Object request,
                                   final InvokeContext invokeContext,
                                   final InvokeCallback invokeCallback, final int timeoutMillis)
                                                                                                throws RemotingException,
                                                                                                InterruptedException {
        ensureStarted();
        check();
        this.rpcRemoting.invokeWithCallback(url, request, invokeContext, invokeCallback,
            timeoutMillis);
    }

    /**
     * Callback invocation using a {@link Connection} <br>
     * You can specify an implementation of {@link InvokeCallback} to get the result.
     * <p>
     * Notice:<br>
     *   <b>DO NOT modify the request object concurrently when this method is called.</b>
     *
     * @param conn connection connection
     * @param request request request
     * @param invokeCallback invokeCallback invokeCallback
     * @param timeoutMillis timeoutMillis  timeoutMillis
     * @throws RemotingException remoting exception remoting exception
     */
    public void invokeWithCallback(final Connection conn, final Object request,
                                   final InvokeCallback invokeCallback, final int timeoutMillis)
                                                                                                throws RemotingException {
        ensureStarted();
        this.rpcRemoting.invokeWithCallback(conn, request, null, invokeCallback, timeoutMillis);
    }

    /**
     * Callback invocation with a {@link InvokeContext}, common api notice please see {@link #invokeWithCallback(Connection, Object, InvokeCallback, int)}
     *
     * @param conn connection connection
     * @param request request request
     * @param invokeCallback invokeCallback invokeCallback
     * @param timeoutMillis timeoutMillis  timeoutMillis
     * @throws RemotingException remoting exception remoting exception
     */
    public void invokeWithCallback(final Connection conn, final Object request,
                                   final InvokeContext invokeContext,
                                   final InvokeCallback invokeCallback, final int timeoutMillis)
                                                                                                throws RemotingException {
        ensureStarted();
        this.rpcRemoting.invokeWithCallback(conn, request, invokeContext, invokeCallback,
            timeoutMillis);
    }

    /**
     * check whether a client address connected
     *
     * @param remoteAddr remote address
     * @return {@code true} means url is connected and {@code false} means disconnected.
     */
    public boolean isConnected(String remoteAddr) {
        ensureStarted();
        Url url = this.rpcRemoting.addressParser.parse(remoteAddr);
        return this.isConnected(url);
    }

    /**
     * check whether a {@link Url} connected
     *
     * @param url url
     * @return {@code true} means url is connected and {@code false} means disconnected.
     */
    public boolean isConnected(Url url) {
        ensureStarted();
        Connection conn = this.rpcRemoting.connectionManager.get(url.getUniqueKey());
        if (null != conn) {
            return conn.isFine();
        }
        return false;
    }

    /**
     * check whether connection manage feature enabled
     */
    private void check() {
        if (!option(BoltServerOption.SERVER_MANAGE_CONNECTION_SWITCH)) {
            throw new UnsupportedOperationException(
                "Please enable connection manage feature of Rpc Server before call this method! See comments in constructor RpcServer(int port, boolean manageConnection) to find how to enable!");
        }
    }

    /**
     * init netty write buffer water mark
     */
    private void initWriteBufferWaterMark() {
        // init with system properties
        Integer lowWaterMarkConfig = ConfigManager.netty_buffer_low_watermark();
        if (lowWaterMarkConfig != null) {
            option(BoltServerOption.NETTY_BUFFER_LOW_WATER_MARK, lowWaterMarkConfig);
        }
        Integer highWaterMarkConfig = ConfigManager.netty_buffer_high_watermark();
        if (highWaterMarkConfig != null) {
            option(BoltServerOption.NETTY_BUFFER_HIGH_WATER_MARK, highWaterMarkConfig);
        }

        int lowWaterMark = this.netty_buffer_low_watermark();
        int highWaterMark = this.netty_buffer_high_watermark();
        if (lowWaterMark > highWaterMark) {
            throw new IllegalArgumentException(
                String
                    .format(
                        "[server side] bolt netty high water mark {%s} should not be smaller than low water mark {%s} bytes)",
                        highWaterMark, lowWaterMark));
        } else {
            logger.warn(
                "[server side] bolt netty low water mark is {} bytes, high water mark is {} bytes",
                lowWaterMark, highWaterMark);
        }
        this.bootstrap.childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(
            lowWaterMark, highWaterMark));
    }

    // ~~~ getter and setter

    /**
     * Getter method for property <tt>addressParser</tt>.
     *
     * @return property value of addressParser
     */
    public RemotingAddressParser getAddressParser() {
        return this.addressParser;
    }

    /**
     * Setter method for property <tt>addressParser</tt>.
     *
     * @param addressParser address Parser value to be assigned to property addressParser
     */
    public void setAddressParser(RemotingAddressParser addressParser) {
        this.addressParser = addressParser;
    }

    /**
     * Getter method for property <tt>connectionManager</tt>.
     *
     * @return property value of connectionManager
     */
    public DefaultConnectionManager getConnectionManager() {
        return connectionManager;
    }
}
