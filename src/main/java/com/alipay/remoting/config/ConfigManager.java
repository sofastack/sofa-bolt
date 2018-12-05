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

/**
 * get configs through system properties prior to default value
 *
 * @author tsui
 * @version $Id: ConfigManager.java, v 0.1 2017-08-03 19:21 tsui Exp $
 */
public class ConfigManager {
    // ~~~ properties for bootstrap
    public static boolean tcp_nodelay() {
        return getBool(Configs.TCP_NODELAY, Configs.TCP_NODELAY_DEFAULT);
    }

    public static boolean tcp_so_reuseaddr() {
        return getBool(Configs.TCP_SO_REUSEADDR, Configs.TCP_SO_REUSEADDR_DEFAULT);
    }

    public static int tcp_so_backlog() {
        return getInt(Configs.TCP_SO_BACKLOG, Configs.TCP_SO_BACKLOG_DEFAULT);
    }

    public static boolean tcp_so_keepalive() {
        return getBool(Configs.TCP_SO_KEEPALIVE, Configs.TCP_SO_KEEPALIVE_DEFAULT);
    }

    public static int netty_io_ratio() {
        return getInt(Configs.NETTY_IO_RATIO, Configs.NETTY_IO_RATIO_DEFAULT);
    }

    public static boolean netty_buffer_pooled() {
        return getBool(Configs.NETTY_BUFFER_POOLED, Configs.NETTY_BUFFER_POOLED_DEFAULT);
    }

    public static int netty_buffer_low_watermark() {
        return getInt(Configs.NETTY_BUFFER_LOW_WATERMARK,
            Configs.NETTY_BUFFER_LOW_WATERMARK_DEFAULT);
    }

    public static int netty_buffer_high_watermark() {
        return getInt(Configs.NETTY_BUFFER_HIGH_WATERMARK,
            Configs.NETTY_BUFFER_HIGH_WATERMARK_DEFAULT);
    }

    public static boolean netty_epoll() {
        return getBool(Configs.NETTY_EPOLL_SWITCH, Configs.NETTY_EPOLL_SWITCH_DEFAULT);
    }

    public static boolean netty_epoll_lt_enabled() {
        return getBool(Configs.NETTY_EPOLL_LT, Configs.NETTY_EPOLL_LT_DEFAULT);
    }

    // ~~~ properties for idle
    public static boolean tcp_idle_switch() {
        return getBool(Configs.TCP_IDLE_SWITCH, Configs.TCP_IDLE_SWITCH_DEFAULT);
    }

    public static int tcp_idle() {
        return getInt(Configs.TCP_IDLE, Configs.TCP_IDLE_DEFAULT);
    }

    public static int tcp_idle_maxtimes() {
        return getInt(Configs.TCP_IDLE_MAXTIMES, Configs.TCP_IDLE_MAXTIMES_DEFAULT);
    }

    public static int tcp_server_idle() {
        return getInt(Configs.TCP_SERVER_IDLE, Configs.TCP_SERVER_IDLE_DEFAULT);
    }

    // ~~~ properties for connection manager
    public static int conn_create_tp_min_size() {
        return getInt(Configs.CONN_CREATE_TP_MIN_SIZE, Configs.CONN_CREATE_TP_MIN_SIZE_DEFAULT);
    }

    public static int conn_create_tp_max_size() {
        return getInt(Configs.CONN_CREATE_TP_MAX_SIZE, Configs.CONN_CREATE_TP_MAX_SIZE_DEFAULT);
    }

    public static int conn_create_tp_queue_size() {
        return getInt(Configs.CONN_CREATE_TP_QUEUE_SIZE, Configs.CONN_CREATE_TP_QUEUE_SIZE_DEFAULT);
    }

    public static int conn_create_tp_keepalive() {
        return getInt(Configs.CONN_CREATE_TP_KEEPALIVE_TIME,
            Configs.CONN_CREATE_TP_KEEPALIVE_TIME_DEFAULT);
    }

    // ~~~ properties for processor manager
    public static int default_tp_min_size() {
        return getInt(Configs.TP_MIN_SIZE, Configs.TP_MIN_SIZE_DEFAULT);
    }

    public static int default_tp_max_size() {
        return getInt(Configs.TP_MAX_SIZE, Configs.TP_MAX_SIZE_DEFAULT);
    }

    public static int default_tp_queue_size() {
        return getInt(Configs.TP_QUEUE_SIZE, Configs.TP_QUEUE_SIZE_DEFAULT);
    }

    public static int default_tp_keepalive_time() {
        return getInt(Configs.TP_KEEPALIVE_TIME, Configs.TP_KEEPALIVE_TIME_DEFAULT);
    }

    // ~~~ properties for reconnect manager
    public static boolean conn_reconnect_switch() {
        return getBool(Configs.CONN_RECONNECT_SWITCH, Configs.CONN_RECONNECT_SWITCH_DEFAULT);
    }

    // ~~~ properties for connection monitor
    public static boolean conn_monitor_switch() {
        return getBool(Configs.CONN_MONITOR_SWITCH, Configs.CONN_MONITOR_SWITCH_DEFAULT);
    }

    public static long conn_monitor_initial_delay() {
        return getLong(Configs.CONN_MONITOR_INITIAL_DELAY,
            Configs.CONN_MONITOR_INITIAL_DELAY_DEFAULT);
    }

    public static long conn_monitor_period() {
        return getLong(Configs.CONN_MONITOR_PERIOD, Configs.CONN_MONITOR_PERIOD_DEFAULT);
    }

    public static int conn_threshold() {
        return getInt(Configs.CONN_THRESHOLD, Configs.CONN_THRESHOLD_DEFAULT);
    }

    public static int retry_detect_period() {
        return getInt(Configs.RETRY_DETECT_PERIOD, Configs.RETRY_DETECT_PERIOD_DEFAULT);
    }

    // ~~~ properties for serializer
    public static final byte serializer = serializer();

    public static byte serializer() {
        return getByte(Configs.SERIALIZER, Configs.SERIALIZER_DEFAULT);
    }

    // ~~~ public helper methods to retrieve system property
    public static boolean getBool(String key, String defaultValue) {
        return Boolean.parseBoolean(System.getProperty(key, defaultValue));
    }

    public static int getInt(String key, String defaultValue) {
        return Integer.parseInt(System.getProperty(key, defaultValue));
    }

    public static byte getByte(String key, String defaultValue) {
        return Byte.parseByte(System.getProperty(key, defaultValue));
    }

    public static long getLong(String key, String defaultValue) {
        return Long.parseLong(System.getProperty(key, defaultValue));
    }
}