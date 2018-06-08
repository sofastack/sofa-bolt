/**
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

import java.lang.reflect.Constructor;
import java.util.concurrent.ThreadFactory;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * @author eonezhang 08/06/2018
 */
public enum TransportType {
    NIO(NioServerSocketChannel.class, NioSocketChannel.class, NioEventLoopGroup.class),

    EPOLL(EpollServerSocketChannel.class, EpollSocketChannel.class, EpollEventLoopGroup.class);

    private final Class<? extends ServerChannel>  serverChannelClass;
    private final Class<? extends SocketChannel>  socketChannelClass;
    private final Class<? extends EventLoopGroup> eventLoopGroupClass;

    TransportType(Class<? extends ServerChannel> serverChannelClass,
                  Class<? extends SocketChannel> socketChannelClass,
                  Class<? extends EventLoopGroup> eventLoopGroupClass) {
        this.serverChannelClass = serverChannelClass;
        this.socketChannelClass = socketChannelClass;
        this.eventLoopGroupClass = eventLoopGroupClass;
    }

    /**
     * Returns the {@link ServerChannel} class that is available for this transport type.
     */
    public Class<? extends ServerChannel> serverChannelClass() {
        return serverChannelClass;
    }

    /**
     * Creates the available {@link EventLoopGroup}.
     */
    public EventLoopGroup newEventLoopGroup(int nThreads, ThreadFactory threadFactory) {
        if (eventLoopGroupClass == EpollEventLoopGroup.class) {
            try {
                Constructor<EpollEventLoopGroup> constructor = EpollEventLoopGroup.class
                    .getConstructor(int.class, ThreadFactory.class);
                return constructor.newInstance(nThreads, threadFactory);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        try {
            Constructor<NioEventLoopGroup> constructor = NioEventLoopGroup.class.getConstructor(
                int.class, ThreadFactory.class);
            return constructor.newInstance(nThreads, threadFactory);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the available {@link TransportType}.
     */
    public static TransportType detectTransportType() {
        boolean useEpoll = Boolean.parseBoolean(System.getProperty("transport.use.epoll", "false"));
        return useEpoll ? EPOLL : NIO;
    }

    /**
     * Returns the available {@link SocketChannel} class for {@code eventLoopGroup}.
     */
    public static Class<? extends SocketChannel> socketChannelType(EventLoopGroup eventLoopGroup) {
        for (TransportType type : values()) {
            if (type.eventLoopGroupClass.isAssignableFrom(eventLoopGroup.getClass())) {
                return type.socketChannelClass;
            }
        }
        throw unsupportedEventLoopType(eventLoopGroup);
    }

    private static IllegalStateException unsupportedEventLoopType(EventLoopGroup eventLoopGroup) {
        return new IllegalStateException("unsupported event loop type: "
                                         + eventLoopGroup.getClass().getName());
    }
}
