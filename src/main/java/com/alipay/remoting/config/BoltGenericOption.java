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
package com.alipay.remoting.config;

import com.alipay.remoting.ConnectionSelectStrategy;
import com.alipay.remoting.ExtendedNettyChannelHandler;

/**
 * Supported options both in client and server side.
 *
 * @author chengyi (mark.lx@antfin.com) 2018-11-06 17:59
 */
public class BoltGenericOption<T> extends BoltOption<T> {

    /*------------ NETTY Config Start ------------*/
    public static final BoltOption<Boolean>                     TCP_NODELAY                    = valueOf(
                                                                                                   "bolt.tcp.nodelay",
                                                                                                   true);
    public static final BoltOption<Boolean>                     TCP_SO_REUSEADDR               = valueOf(
                                                                                                   "bolt.tcp.so.reuseaddr",
                                                                                                   true);
    public static final BoltOption<Boolean>                     TCP_SO_KEEPALIVE               = valueOf(
                                                                                                   "bolt.tcp.so.keepalive",
                                                                                                   true);

    public static final BoltOption<Integer>                     TCP_SO_SNDBUF                  = valueOf("bolt.tcp.so.sndbuf");

    public static final BoltOption<Integer>                     TCP_SO_RCVBUF                  = valueOf("bolt.tcp.so.rcvbuf");

    public static final BoltOption<Integer>                     NETTY_IO_RATIO                 = valueOf(
                                                                                                   "bolt.netty.io.ratio",
                                                                                                   70);
    public static final BoltOption<Boolean>                     NETTY_BUFFER_POOLED            = valueOf(
                                                                                                   "bolt.netty.buffer.pooled",
                                                                                                   true);

    public static final BoltOption<Integer>                     NETTY_BUFFER_HIGH_WATER_MARK   = valueOf(
                                                                                                   "bolt.netty.buffer.high.watermark",
                                                                                                   64 * 1024);
    public static final BoltOption<Integer>                     NETTY_BUFFER_LOW_WATER_MARK    = valueOf(
                                                                                                   "bolt.netty.buffer.low.watermark",
                                                                                                   32 * 1024);

    public static final BoltOption<Boolean>                     NETTY_EPOLL_SWITCH             = valueOf(
                                                                                                   "bolt.netty.epoll.switch",
                                                                                                   true);

    public static final BoltOption<Boolean>                     TCP_IDLE_SWITCH                = valueOf(
                                                                                                   "bolt.tcp.heartbeat.switch",
                                                                                                   true);
    /*------------ NETTY Config End ------------*/

    /*------------ Thread Pool Config Start ------------*/
    public static final BoltOption<Integer>                     TP_MIN_SIZE                    = valueOf(
                                                                                                   "bolt.tp.min",
                                                                                                   20);
    public static final BoltOption<Integer>                     TP_MAX_SIZE                    = valueOf(
                                                                                                   "bolt.tp.max",
                                                                                                   400);
    public static final BoltOption<Integer>                     TP_QUEUE_SIZE                  = valueOf(
                                                                                                   "bolt.tp.queue",
                                                                                                   600);
    public static final BoltOption<Integer>                     TP_KEEPALIVE_TIME              = valueOf(
                                                                                                   "bolt.tp.keepalive",
                                                                                                   60);

    /*------------ Thread Pool Config End ------------*/

    public static final BoltOption<ConnectionSelectStrategy>    CONNECTION_SELECT_STRATEGY     = valueOf("CONNECTION_SELECT_STRATEGY");

    public static final BoltOption<Boolean>                     NETTY_FLUSH_CONSOLIDATION      = valueOf(
                                                                                                   "bolt.netty.flush_consolidation",
                                                                                                   false);

    public static final BoltOption<ExtendedNettyChannelHandler> EXTENDED_NETTY_CHANNEL_HANDLER = valueOf(
                                                                                                   "bolt.extend.netty.channel.handler",
                                                                                                   null);

    protected BoltGenericOption(String name, T defaultValue) {
        super(name, defaultValue);
    }
}
