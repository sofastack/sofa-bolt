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

import com.alipay.remoting.CommandCode;
import com.alipay.remoting.Connection;
import com.alipay.remoting.ConnectionEventHandler;
import com.alipay.remoting.ConnectionEventListener;
import com.alipay.remoting.ConnectionEventProcessor;
import com.alipay.remoting.ConnectionEventType;
import com.alipay.remoting.DefaultConnectionManager;
import com.alipay.remoting.InvokeCallback;
import com.alipay.remoting.InvokeContext;
import com.alipay.remoting.NamedThreadFactory;
import com.alipay.remoting.ProtocolCode;
import com.alipay.remoting.ProtocolManager;
import com.alipay.remoting.RandomSelectStrategy;
import com.alipay.remoting.RemotingAddressParser;
import com.alipay.remoting.RemotingProcessor;
import com.alipay.remoting.RemotingServer;
import com.alipay.remoting.ServerIdleHandler;
import com.alipay.remoting.SystemProperties;
import com.alipay.remoting.Url;
import com.alipay.remoting.codec.ProtocolCodeBasedEncoder;
import com.alipay.remoting.exception.RemotingException;
import com.alipay.remoting.log.BoltLoggerFactory;
import com.alipay.remoting.rpc.protocol.RpcProtocolDecoder;
import com.alipay.remoting.rpc.protocol.RpcProtocolManager;
import com.alipay.remoting.rpc.protocol.RpcProtocolV2;
import com.alipay.remoting.rpc.protocol.UserProcessor;
import com.alipay.remoting.util.GlobalSwitch;
import com.alipay.remoting.util.RemotingUtil;
import com.alipay.remoting.util.StringUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Server for Rpc.
 *
 * @author jiangping
 * @version $Id: RpcServer.java, v 0.1 2015-8-31 PM5:22:22 tao Exp $
 */
// TODO: 2018/4/23 by zmyer
public class RpcServer extends RemotingServer {

    /** logger */
    private static final Logger                         logger                  = BoltLoggerFactory
                                                                                    .getLogger("RpcRemoting");

    /** server bootstrap */
    private ServerBootstrap                             bootstrap;

    /** channelFuture */
    private ChannelFuture                               channelFuture;

    /** global switch */
    private GlobalSwitch                                globalSwitch            = new GlobalSwitch();

    /** connection event handler */
    private ConnectionEventHandler                      connectionEventHandler;

    /** connection event listener */
    private ConnectionEventListener                     connectionEventListener = new ConnectionEventListener();

    /** user processors of rpc server */
    private ConcurrentHashMap<String, UserProcessor<?>> userProcessors          = new ConcurrentHashMap<String, UserProcessor<?>>(
                                                                                    4);

    /** boss event loop group*/
    private final EventLoopGroup                        bossGroup               = new NioEventLoopGroup(
                                                                                    1,
                                                                                    new NamedThreadFactory(
                                                                                        "Rpc-netty-server-boss"));
    /** worker event loop group. Reuse I/O worker threads between rpc servers. */
    private final static NioEventLoopGroup              workerGroup             = new NioEventLoopGroup(
                                                                                    Runtime
                                                                                        .getRuntime()
                                                                                        .availableProcessors() * 2,
                                                                                    new NamedThreadFactory(
                                                                                        "Rpc-netty-server-worker"));

    /** address parser to get custom args */
    private RemotingAddressParser                       addressParser;

    /** connection manager */
    private DefaultConnectionManager                    connectionManager;

    /** rpc remoting */
    protected RpcRemoting                               rpcRemoting;

    static {
        workerGroup.setIoRatio(SystemProperties.netty_io_ratio());
    }

    /**
     * Construct a rpc server. <br>
     *
     * Note:<br>
     * You can only use invoke methods with params {@link Connection}, for example {@link #invokeSync(Connection, Object, int)} <br>
     * Otherwise {@link UnsupportedOperationException} will be thrown.
     *
     * @param port
     */
    // TODO: 2018/4/23 by zmyer
    public RpcServer(int port) {
        super(port);
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
     * @param port
     * @param manageConnection true to enable connection management feature
     */
    // TODO: 2018/4/23 by zmyer
    public RpcServer(int port, boolean manageConnection) {
        this(port);
        /** server connection management feature enabled or not, default value false, means disabled. */
        if (manageConnection) {
            this.globalSwitch.turnOn(GlobalSwitch.SERVER_MANAGE_CONNECTION_SWITCH);
        }
    }

    /**
     * Construct a rpc server. <br>
     *
     * You can construct a rpc server with synchronous or asynchronous stop strategy by {@param syncStop}.
     *
     * @param port
     * @param manageConnection
     * @param syncStop true to enable stop in synchronous way
     */
    // TODO: 2018/4/23 by zmyer
    public RpcServer(int port, boolean manageConnection, boolean syncStop) {
        this(port, manageConnection);
        if (syncStop) {
            this.globalSwitch.turnOn(GlobalSwitch.SERVER_SYNC_STOP);
        }
    }

    // TODO: 2018/4/23 by zmyer
    @Override
    protected void doInit() {
        if (this.addressParser == null) {
            //创建rpc地址解析器
            this.addressParser = new RpcAddressParser();
        }
        //初始化remoting
        initRpcRemoting(null);
        //创建服务器引导对象
        this.bootstrap = new ServerBootstrap();
        //初始化服务器引导对象
        this.bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
            .option(ChannelOption.SO_BACKLOG, SystemProperties.tcp_so_backlog())
            .option(ChannelOption.SO_REUSEADDR, SystemProperties.tcp_so_reuseaddr())
            .childOption(ChannelOption.TCP_NODELAY, SystemProperties.tcp_nodelay())
            .childOption(ChannelOption.SO_KEEPALIVE, SystemProperties.tcp_so_keepalive());

        // set write buffer water mark
        //初始化写入缓冲区水位
        initWriteBufferWaterMark();

        //是否缓存缓冲区
        boolean pooledBuffer = SystemProperties.netty_buffer_pooled();
        if (pooledBuffer) {
            this.bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        }

        final boolean idleSwitch = SystemProperties.tcp_idle_switch();
        final int idleTime = SystemProperties.tcp_server_idle();
        //服务器空闲处理器
        final ChannelHandler serverIdleHandler = new ServerIdleHandler();
        //rpc处理器
        final RpcHandler rpcHandler = new RpcHandler(true, this.userProcessors);
        //设置channel初始化处理器
        this.bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {

            protected void initChannel(SocketChannel channel) throws Exception {
                //读取新建的channel对象对应的流水线
                ChannelPipeline pipeline = channel.pipeline();
                //设置解码器
                pipeline.addLast("decoder", new RpcProtocolDecoder(
                    RpcProtocolManager.DEFAULT_PROTOCOL_CODE_LENGTH));
                //设置编码器
                pipeline.addLast(
                    "encoder",
                    new ProtocolCodeBasedEncoder(ProtocolCode
                        .fromBytes(RpcProtocolV2.PROTOCOL_CODE)));
                if (idleSwitch) {
                    //设置空闲状态处理器
                    pipeline.addLast("idleStateHandler", new IdleStateHandler(0, 0, idleTime,
                        TimeUnit.MILLISECONDS));
                    //设置空闲处理器
                    pipeline.addLast("serverIdleHandler", serverIdleHandler);
                }
                //添加连接事件处理器
                pipeline.addLast("connectionEventHandler", connectionEventHandler);
                //添加rpc处理器
                pipeline.addLast("handler", rpcHandler);
                //创建连接对象
                createConnection(channel);
            }

            /**
             * create connection operation<br>
             * <ul>
             * <li>If flag manageConnection be true, use {@link DefaultConnectionManager} to add a new connection, meanwhile bind it with the channel.</li>
             * <li>If flag manageConnection be false, just create a new connection and bind it with the channel.</li>
             * </ul>
             *
             * @param channel
             */
            // TODO: 2018/4/23 by zmyer
            private void createConnection(SocketChannel channel) {
                //解析新建立的连接对象地址
                Url url = addressParser.parse(RemotingUtil.parseRemoteAddress(channel));
                if (globalSwitch.isOn(GlobalSwitch.SERVER_MANAGE_CONNECTION_SWITCH)) {
                    //将新建立的连接插入到管理器中
                    connectionManager.add(new Connection(channel, url), url.getUniqueKey());
                } else {
                    //直接创建对应的新连接对象
                    new Connection(channel, url);
                }
                //触发连接事件
                channel.pipeline().fireUserEventTriggered(ConnectionEventType.CONNECT);
            }
        });
    }

    /**
     * @throws InterruptedException
     * @see com.alipay.remoting.RemotingServer#doStart()
     */
    // TODO: 2018/4/23 by zmyer
    @Override
    protected boolean doStart() throws InterruptedException {
        //开始绑定服务器地址
        this.channelFuture = this.bootstrap.bind(new InetSocketAddress(this.port)).sync();
        return this.channelFuture.isSuccess();
    }

    /**
     * @see com.alipay.remoting.RemotingServer#doStart(String)
     */
    // TODO: 2018/4/23 by zmyer
    @Override
    protected boolean doStart(String ip) throws InterruptedException {
        //绑定服务器地址
        this.channelFuture = this.bootstrap.bind(new InetSocketAddress(ip, this.port)).sync();
        return channelFuture.isSuccess();
    }

    /**
     * Notice: only {@link GlobalSwitch#SERVER_MANAGE_CONNECTION_SWITCH} switch on, will close all connections.
     *
     * @see com.alipay.remoting.RemotingServer#doStop()
     */
    // TODO: 2018/4/23 by zmyer
    @Override
    protected void doStop() {
        //关闭channel
        this.channelFuture.channel().close();
        if (this.globalSwitch.isOn(GlobalSwitch.SERVER_SYNC_STOP)) {
            //关闭事件循环处理器(同步)
            this.bossGroup.shutdownGracefully().awaitUninterruptibly();
        } else {
            //关闭时间循环处理器(异步)
            this.bossGroup.shutdownGracefully();
        }
        if (this.globalSwitch.isOn(GlobalSwitch.SERVER_MANAGE_CONNECTION_SWITCH)) {
            //删除所有的连接对象
            this.connectionManager.removeAll();
            logger.warn("Close all connections from server side!");
        }
        logger.warn("Rpc Server stopped!");
    }

    /**
     * init rpc remoting
     * @param rpcRemoting
     */
    // TODO: 2018/4/23 by zmyer
    protected void initRpcRemoting(RpcRemoting rpcRemoting) {
        //如果打开了管理连接开关
        if (this.globalSwitch.isOn(GlobalSwitch.SERVER_MANAGE_CONNECTION_SWITCH)) {
            //创建连接事件处理器
            this.connectionEventHandler = new RpcConnectionEventHandler(globalSwitch);
            //创建连接管理器
            this.connectionManager = new DefaultConnectionManager(new RandomSelectStrategy());
            //设置连接管理器
            this.connectionEventHandler.setConnectionManager(this.connectionManager);
            //设置连接事件监听器
            this.connectionEventHandler.setConnectionEventListener(this.connectionEventListener);
        } else {
            //如果开关关闭，则直接创建连接事件处理器
            this.connectionEventHandler = new ConnectionEventHandler(globalSwitch);
            //为连接事件处理器设置监听器
            this.connectionEventHandler.setConnectionEventListener(this.connectionEventListener);
        }
        if (null != rpcRemoting) {
            //设置remoting对象
            this.rpcRemoting = rpcRemoting;
        } else {
            //创建remoting对象
            this.rpcRemoting = new RpcServerRemoting(new RpcCommandFactory(), this.addressParser,
                this.connectionManager);
        }
    }

    /**
     * @see com.alipay.remoting.RemotingServer#registerProcessor(byte, com.alipay.remoting.CommandCode, com.alipay.remoting.RemotingProcessor)
     */
    // TODO: 2018/4/23 by zmyer
    @Override
    public void registerProcessor(byte protocolCode, CommandCode cmd, RemotingProcessor<?> processor) {
        //为具体的协议对象，注册指定的处理器
        ProtocolManager.getProtocol(ProtocolCode.fromBytes(protocolCode)).getCommandHandler()
            .registerProcessor(cmd, processor);
    }

    /**
     * @see com.alipay.remoting.RemotingServer#registerDefaultExecutor(byte, ExecutorService)
     */
    // TODO: 2018/4/23 by zmyer
    @Override
    public void registerDefaultExecutor(byte protocolCode, ExecutorService executor) {
        //为具体的协议对象注册默认执行对象
        ProtocolManager.getProtocol(ProtocolCode.fromBytes(protocolCode)).getCommandHandler()
            .registerDefaultExecutor(executor);
    }

    /**
     * Add processor to process connection event.
     *
     * @param type
     * @param processor
     */
    // TODO: 2018/4/23 by zmyer
    public void addConnectionEventProcessor(ConnectionEventType type,
                                            ConnectionEventProcessor processor) {
        //为连接事件监听器注册具体的事件处理器
        this.connectionEventListener.addConnectionEventProcessor(type, processor);
    }

    /**
     * @see com.alipay.remoting.RemotingServer#registerUserProcessor(com.alipay.remoting.rpc.protocol.UserProcessor)
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
                            + "] has already been registered to rpc server, can not register again!";
            throw new RuntimeException(errMsg);
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
     * @param addr
     * @param request
     * @throws RemotingException
     * @throws InterruptedException
     */
    // TODO: 2018/4/23 by zmyer
    public void oneway(final String addr, final Object request) throws RemotingException,
                                                               InterruptedException {
        check();
        //oneway模式
        this.rpcRemoting.oneway(addr, request, null);
    }

    /**
     * One way invocation with a {@link InvokeContext}, common api notice please see {@link #oneway(String, Object)}
     *
     * @param addr
     * @param request
     * @param invokeContext
     * @throws RemotingException
     * @throws InterruptedException
     */
    // TODO: 2018/4/23 by zmyer
    public void oneway(final String addr, final Object request, final InvokeContext invokeContext)
                                                                                                  throws RemotingException,
                                                                                                  InterruptedException {
        check();
        //oneway模式
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
     * @param url
     * @param request
     * @throws RemotingException
     * @throws InterruptedException
     */
    // TODO: 2018/4/23 by zmyer
    public void oneway(final Url url, final Object request) throws RemotingException,
                                                           InterruptedException {
        check();
        this.rpcRemoting.oneway(url, request, null);
    }

    /**
     * One way invocation with a {@link InvokeContext}, common api notice please see {@link #oneway(Url, Object)}
     *
     * @param url
     * @param request
     * @param invokeContext
     * @throws RemotingException
     * @throws InterruptedException
     */
    // TODO: 2018/4/23 by zmyer
    public void oneway(final Url url, final Object request, final InvokeContext invokeContext)
                                                                                              throws RemotingException,
                                                                                              InterruptedException {
        check();
        this.rpcRemoting.oneway(url, request, invokeContext);
    }

    /**
     * One way invocation using a {@link Connection} <br>
     * <p>
     * Notice:<br>
     *   <b>DO NOT modify the request object concurrently when this method is called.</b>
     *
     * @param conn
     * @param request
     * @throws RemotingException
     */
    // TODO: 2018/4/23 by zmyer
    public void oneway(final Connection conn, final Object request) throws RemotingException {
        this.rpcRemoting.oneway(conn, request, null);
    }

    /**
     * One way invocation with a {@link InvokeContext}, common api notice please see {@link #oneway(Connection, Object)}
     *
     * @param conn
     * @param request
     * @param invokeContext
     * @throws RemotingException
     */
    // TODO: 2018/4/23 by zmyer
    public void oneway(final Connection conn, final Object request,
                       final InvokeContext invokeContext) throws RemotingException {
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
     * @param addr
     * @param request
     * @param timeoutMillis
     * @return Object
     * @throws RemotingException
     * @throws InterruptedException
     */
    // TODO: 2018/4/23 by zmyer
    public Object invokeSync(final String addr, final Object request, final int timeoutMillis)
                                                                                              throws RemotingException,
                                                                                              InterruptedException {
        check();
        return this.rpcRemoting.invokeSync(addr, request, null, timeoutMillis);
    }

    /**
     * Synchronous invocation with a {@link InvokeContext}, common api notice please see {@link #invokeSync(String, Object, int)}
     *
     * @param addr
     * @param request
     * @param invokeContext
     * @param timeoutMillis
     * @return
     * @throws RemotingException
     * @throws InterruptedException
     */
    // TODO: 2018/4/23 by zmyer
    public Object invokeSync(final String addr, final Object request,
                             final InvokeContext invokeContext, final int timeoutMillis)
                                                                                        throws RemotingException,
                                                                                        InterruptedException {
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
     * @param url
     * @param request
     * @param timeoutMillis
     * @return Object
     * @throws RemotingException
     * @throws InterruptedException
     */
    public Object invokeSync(Url url, Object request, int timeoutMillis) throws RemotingException,
                                                                        InterruptedException {
        check();
        // TODO: 2018/4/23 by zmyer
        return this.rpcRemoting.invokeSync(url, request, null, timeoutMillis);
    }

    /**
     * Synchronous invocation with a {@link InvokeContext}, common api notice please see {@link #invokeSync(Url, Object, int)}
     *
     * @param url
     * @param request
     * @param invokeContext
     * @param timeoutMillis
     * @return
     * @throws RemotingException
     * @throws InterruptedException
     */
    // TODO: 2018/4/23 by zmyer
    public Object invokeSync(final Url url, final Object request,
                             final InvokeContext invokeContext, final int timeoutMillis)
                                                                                        throws RemotingException,
                                                                                        InterruptedException {
        check();
        return this.rpcRemoting.invokeSync(url, request, invokeContext, timeoutMillis);
    }

    /**
     * Synchronous invocation using a {@link Connection} <br>
     * <p>
     * Notice:<br>
     *   <b>DO NOT modify the request object concurrently when this method is called.</b>
     *
     * @param conn
     * @param request
     * @param timeoutMillis
     * @return Object
     * @throws RemotingException
     * @throws InterruptedException
     */
    // TODO: 2018/4/23 by zmyer
    public Object invokeSync(final Connection conn, final Object request, final int timeoutMillis)
                                                                                                  throws RemotingException,
                                                                                                  InterruptedException {
        return this.rpcRemoting.invokeSync(conn, request, null, timeoutMillis);
    }

    /**
     * Synchronous invocation with a {@link InvokeContext}, common api notice please see {@link #invokeSync(Connection, Object, int)}
     *
     * @param conn
     * @param request
     * @param invokeContext
     * @param timeoutMillis
     * @return
     * @throws RemotingException
     * @throws InterruptedException
     */
    // TODO: 2018/4/23 by zmyer
    public Object invokeSync(final Connection conn, final Object request,
                             final InvokeContext invokeContext, final int timeoutMillis)
                                                                                        throws RemotingException,
                                                                                        InterruptedException {
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
     * @param addr
     * @param request
     * @param timeoutMillis
     * @return RpcResponseFuture
     * @throws RemotingException
     * @throws InterruptedException
     */
    // TODO: 2018/4/23 by zmyer
    public RpcResponseFuture invokeWithFuture(final String addr, final Object request,
                                              final int timeoutMillis) throws RemotingException,
                                                                      InterruptedException {
        check();
        return this.rpcRemoting.invokeWithFuture(addr, request, null, timeoutMillis);
    }

    /**
     * Future invocation with a {@link InvokeContext}, common api notice please see {@link #invokeWithFuture(String, Object, int)}
     *
     * @param addr
     * @param request
     * @param invokeContext
     * @param timeoutMillis
     * @return
     * @throws RemotingException
     * @throws InterruptedException
     */
    // TODO: 2018/4/23 by zmyer
    public RpcResponseFuture invokeWithFuture(final String addr, final Object request,
                                              final InvokeContext invokeContext,
                                              final int timeoutMillis) throws RemotingException,
                                                                      InterruptedException {
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
     * @param url
     * @param request
     * @param timeoutMillis
     * @return RpcResponseFuture
     * @throws RemotingException
     * @throws InterruptedException
     */
    // TODO: 2018/4/23 by zmyer
    public RpcResponseFuture invokeWithFuture(final Url url, final Object request,
                                              final int timeoutMillis) throws RemotingException,
                                                                      InterruptedException {
        check();
        return this.rpcRemoting.invokeWithFuture(url, request, null, timeoutMillis);
    }

    /**
     * Future invocation with a {@link InvokeContext}, common api notice please see {@link #invokeWithFuture(Url, Object, int)}
     *
     * @param url
     * @param request
     * @param invokeContext
     * @param timeoutMillis
     * @return
     * @throws RemotingException
     * @throws InterruptedException
     */
    // TODO: 2018/4/23 by zmyer
    public RpcResponseFuture invokeWithFuture(final Url url, final Object request,
                                              final InvokeContext invokeContext,
                                              final int timeoutMillis) throws RemotingException,
                                                                      InterruptedException {
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
     * @param conn
     * @param request
     * @param timeoutMillis
     * @return
     * @throws RemotingException
     */
    // TODO: 2018/4/23 by zmyer
    public RpcResponseFuture invokeWithFuture(final Connection conn, final Object request,
                                              final int timeoutMillis) throws RemotingException {

        return this.rpcRemoting.invokeWithFuture(conn, request, null, timeoutMillis);
    }

    /**
     * Future invocation with a {@link InvokeContext}, common api notice please see {@link #invokeWithFuture(Connection, Object, int)}
     *
     * @param conn
     * @param request
     * @param invokeContext
     * @param timeoutMillis
     * @return
     * @throws RemotingException
     */
    // TODO: 2018/4/23 by zmyer
    public RpcResponseFuture invokeWithFuture(final Connection conn, final Object request,
                                              final InvokeContext invokeContext,
                                              final int timeoutMillis) throws RemotingException {

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
     * @param addr
     * @param request
     * @param invokeCallback
     * @param timeoutMillis
     * @throws RemotingException
     * @throws InterruptedException
     */
    // TODO: 2018/4/23 by zmyer
    public void invokeWithCallback(final String addr, final Object request,
                                   final InvokeCallback invokeCallback, final int timeoutMillis)
                                                                                                throws RemotingException,
                                                                                                InterruptedException {
        check();
        this.rpcRemoting.invokeWithCallback(addr, request, null, invokeCallback, timeoutMillis);
    }

    /**
     * Callback invocation with a {@link InvokeContext}, common api notice please see {@link #invokeWithCallback(String, Object, InvokeCallback, int)}
     *
     * @param addr
     * @param request
     * @param invokeContext
     * @param invokeCallback
     * @param timeoutMillis
     * @throws RemotingException
     * @throws InterruptedException
     */
    // TODO: 2018/4/23 by zmyer
    public void invokeWithCallback(final String addr, final Object request,
                                   final InvokeContext invokeContext,
                                   final InvokeCallback invokeCallback, final int timeoutMillis)
                                                                                                throws RemotingException,
                                                                                                InterruptedException {
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
     * @param url
     * @param request
     * @param invokeCallback
     * @param timeoutMillis
     * @throws RemotingException
     * @throws InterruptedException
     */
    // TODO: 2018/4/23 by zmyer
    public void invokeWithCallback(final Url url, final Object request,
                                   final InvokeCallback invokeCallback, final int timeoutMillis)
                                                                                                throws RemotingException,
                                                                                                InterruptedException {
        check();
        this.rpcRemoting.invokeWithCallback(url, request, null, invokeCallback, timeoutMillis);
    }

    /**
     * Callback invocation with a {@link InvokeContext}, common api notice please see {@link #invokeWithCallback(Url, Object, InvokeCallback, int)}
     *
     * @param url
     * @param request
     * @param invokeContext
     * @param invokeCallback
     * @param timeoutMillis
     * @throws RemotingException
     * @throws InterruptedException
     */
    // TODO: 2018/4/23 by zmyer
    public void invokeWithCallback(final Url url, final Object request,
                                   final InvokeContext invokeContext,
                                   final InvokeCallback invokeCallback, final int timeoutMillis)
                                                                                                throws RemotingException,
                                                                                                InterruptedException {
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
     * @param conn
     * @param request
     * @param invokeCallback
     * @param timeoutMillis
     * @throws RemotingException
     */
    // TODO: 2018/4/23 by zmyer
    public void invokeWithCallback(final Connection conn, final Object request,
                                   final InvokeCallback invokeCallback, final int timeoutMillis)
                                                                                                throws RemotingException {
        this.rpcRemoting.invokeWithCallback(conn, request, null, invokeCallback, timeoutMillis);
    }

    /**
     * Callback invocation with a {@link InvokeContext}, common api notice please see {@link #invokeWithCallback(Connection, Object, InvokeCallback, int)}
     *
     * @param conn
     * @param request
     * @param invokeCallback
     * @param timeoutMillis
     * @throws RemotingException
     */
    // TODO: 2018/4/23 by zmyer
    public void invokeWithCallback(final Connection conn, final Object request,
                                   final InvokeContext invokeContext,
                                   final InvokeCallback invokeCallback, final int timeoutMillis)
                                                                                                throws RemotingException {
        this.rpcRemoting.invokeWithCallback(conn, request, invokeContext, invokeCallback,
            timeoutMillis);
    }

    /**
     * check whether a client address connected
     *
     * @param remoteAddr
     * @return
     */
    // TODO: 2018/4/23 by zmyer
    public boolean isConnected(String remoteAddr) {
        Url url = this.rpcRemoting.addressParser.parse(remoteAddr);
        return this.isConnected(url);
    }

    /**
     * check whether a {@link Url} connected
     *
     * @param url
     * @return
     */
    // TODO: 2018/4/23 by zmyer
    public boolean isConnected(Url url) {
        Connection conn = this.rpcRemoting.connectionManager.get(url.getUniqueKey());
        if (null != conn) {
            return conn.isFine();
        }
        return false;
    }

    /**
     * check whether connection manage feature enabled
     */
    // TODO: 2018/4/23 by zmyer
    private void check() {
        if (!this.globalSwitch.isOn(GlobalSwitch.SERVER_MANAGE_CONNECTION_SWITCH)) {
            throw new UnsupportedOperationException(
                "Please enable connection manage feature of Rpc Server before call this method! See comments in constructor RpcServer(int port, boolean manageConnection) to find how to enable!");
        }
    }

    /**
     * init netty write buffer water mark
     */
    // TODO: 2018/4/23 by zmyer
    private void initWriteBufferWaterMark() {
        //读取低水位
        int lowWaterMark = SystemProperties.netty_buffer_low_watermark();
        //读取高水位
        int highWaterMark = SystemProperties.netty_buffer_high_watermark();
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
        //设置写入缓冲区水位
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
     * @param addressParser value to be assigned to property addressParser
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
