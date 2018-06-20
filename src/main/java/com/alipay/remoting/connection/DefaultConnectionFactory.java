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

import com.alipay.remoting.NamedThreadFactory;
import io.netty.channel.ChannelHandler;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * Default connection factory.
 *
 * @author chengyi (mark.lx@antfin.com) 2018-06-20 15:18
 */
public class DefaultConnectionFactory extends AbstractConnectionFactory {

    private final int                threads;
    private final NamedThreadFactory threadFactory;
    private final ChannelHandler     encoder;
    private final ChannelHandler     decoder;
    private final IdleStateHandler   idleStateHandler;
    private final ChannelHandler     heartbeatHandler;
    private final ChannelHandler     handler;

    public DefaultConnectionFactory(int threads, NamedThreadFactory threadFactory,
                                    ChannelHandler encoder, ChannelHandler decoder,
                                    IdleStateHandler idleStateHandler,
                                    ChannelHandler heartbeatHandler, ChannelHandler handler) {
        if (threads <= 0) {
            throw new IllegalArgumentException("threads must be positive, given: " + threads);
        }
        if (threadFactory == null) {
            throw new IllegalArgumentException("null threadFactory");
        }
        if (encoder == null) {
            throw new IllegalArgumentException("null encoder");
        }
        if (decoder == null) {
            throw new IllegalArgumentException("null decoder");
        }
        if (handler == null) {
            throw new IllegalArgumentException("null handler");
        }

        this.threads = threads;
        this.threadFactory = threadFactory;
        this.encoder = encoder;
        this.decoder = decoder;
        this.idleStateHandler = idleStateHandler;
        this.heartbeatHandler = heartbeatHandler;
        this.handler = handler;
    }

    @Override
    protected int threads() {
        return threads;
    }

    @Override
    protected NamedThreadFactory threadFactory() {
        return threadFactory;
    }

    @Override
    protected ChannelHandler encoder() {
        return encoder;
    }

    @Override
    protected ChannelHandler decoder() {
        return decoder;
    }

    @Override
    protected IdleStateHandler idleStateHandler() {
        return idleStateHandler;
    }

    @Override
    protected ChannelHandler heartbeatHandler() {
        return heartbeatHandler;
    }

    @Override
    protected ChannelHandler handler() {
        return handler;
    }
}
