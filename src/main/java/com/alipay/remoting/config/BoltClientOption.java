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

import javax.net.ssl.TrustManagerFactory;

/**
 * Supported options in client side.
 *
 * @author chengyi (mark.lx@antfin.com) 2018-11-06 18:00
 */
public class BoltClientOption<T> extends BoltGenericOption<T> {

    public static final BoltOption<Integer> NETTY_IO_RATIO                = valueOf(
                                                                              "bolt.tcp.heartbeat.interval",
                                                                              15 * 1000);
    public static final BoltOption<Integer> TCP_IDLE_MAXTIMES             = valueOf(
                                                                              "bolt.tcp.heartbeat.maxtimes",
                                                                              3);

    public static final BoltOption<Integer> CONN_CREATE_TP_MIN_SIZE       = valueOf(
                                                                              "bolt.conn.create.tp.min",
                                                                              3);
    public static final BoltOption<Integer> CONN_CREATE_TP_MAX_SIZE       = valueOf(
                                                                              "bolt.conn.create.tp.max",
                                                                              8);
    public static final BoltOption<Integer> CONN_CREATE_TP_QUEUE_SIZE     = valueOf(
                                                                              "bolt.conn.create.tp.queue",
                                                                              50);
    public static final BoltOption<Integer> CONN_CREATE_TP_KEEPALIVE_TIME = valueOf(
                                                                              "bolt.conn.create.tp.keepalive",
                                                                              60);

    public static final BoltOption<Boolean> CONN_RECONNECT_SWITCH         = valueOf(
                                                                              "bolt.conn.reconnect",
                                                                              false);
    public static final BoltOption<Boolean> CONN_MONITOR_SWITCH           = valueOf(
                                                                              "bolt.conn.monitor",
                                                                              false);

    public static final BoltOption<Boolean> CLI_SSL_ENABLE                = valueOf(
                                                                              "bolt.client.ssl.enable",
                                                                              false);
    public static final BoltOption<String>  CLI_SSL_KEYSTORE_TYPE         = valueOf(
                                                                              "bolt.client.ssl.keystore.type",
                                                                              null);
    public static final BoltOption<String>  CLI_SSL_KEYSTORE              = valueOf(
                                                                              "bolt.client.ssl.keystore",
                                                                              null);

    public static final BoltOption<String>  CLI_SSL_KEYSTORE_PASS         = valueOf(
                                                                              "bolt.client.ssl.keystore.password",
                                                                              null);
    public static final BoltOption<String>  CLI_SSL_TMF_ALGO              = valueOf(
                                                                              "bolt.client.ssl.tmf.algorithm",
                                                                              TrustManagerFactory
                                                                                  .getDefaultAlgorithm());

    private BoltClientOption(String name, T defaultValue) {
        super(name, defaultValue);
    }
}
