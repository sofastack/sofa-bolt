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

import javax.net.ssl.KeyManagerFactory;

/**
 * Supported options in server side.
 *
 * @author chengyi (mark.lx@antfin.com) 2018-11-06 18:00
 */
public class BoltServerOption<T> extends BoltGenericOption<T> {

    public static final BoltOption<Integer> TCP_SO_BACKLOG                  = valueOf(
                                                                                "bolt.tcp.so.backlog",
                                                                                1024);

    public static final BoltOption<Boolean> NETTY_EPOLL_LT                  = valueOf(
                                                                                "bolt.netty.epoll.lt",
                                                                                true);

    public static final BoltOption<Integer> TCP_SERVER_IDLE                 = valueOf(
                                                                                "bolt.tcp.server.idle.interval",
                                                                                90 * 1000);

    public static final BoltOption<Boolean> SERVER_MANAGE_CONNECTION_SWITCH = valueOf(
                                                                                "bolt.server.manage.connection",
                                                                                false);

    public static final BoltOption<Boolean> SERVER_SYNC_STOP                = valueOf(
                                                                                "bolt.server.sync.stop",
                                                                                false);

    public static final BoltOption<Boolean> SRV_SSL_ENABLE                  = valueOf(
                                                                                "bolt.server.ssl.enable",
                                                                                false);
    public static final BoltOption<String>  SRV_SSL_KEYSTORE_TYPE           = valueOf(
                                                                                "bolt.server.ssl.keystore.type",
                                                                                null);
    public static final BoltOption<Boolean> SRV_SSL_NEED_CLIENT_AUTH        = valueOf(
                                                                                "bolt.server.ssl.clientAuth",
                                                                                false);
    public static final BoltOption<String>  SRV_SSL_KEYSTORE                = valueOf(
                                                                                "bolt.server.ssl.keystore",
                                                                                null);
    public static final BoltOption<String>  SRV_SSL_KEYSTORE_PASS           = valueOf(
                                                                                "bolt.server.ssl.keystore.password",
                                                                                null);
    public static final BoltOption<String>  SRV_SSL_KMF_ALGO                = valueOf(
                                                                                "bolt.server.ssl.kmf.algorithm",
                                                                                KeyManagerFactory
                                                                                    .getDefaultAlgorithm());

    private BoltServerOption(String name, T defaultValue) {
        super(name, defaultValue);
    }
}
