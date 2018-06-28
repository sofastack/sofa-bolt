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
package com.alipay.remoting;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author muyun.cyt
 * @version 2018/6/28 上午11:21
 */
public class UserProperties {

    private static ConcurrentHashMap<String, String> userProperties = new ConcurrentHashMap<String, String>();

    /**
     * @param key   property key
     * @param value property value
     * if PROPERTIES_SWITCH{@link com.alipay.remoting.util.GlobalSwitch#PROPERTIES_SWITCH} is on,user can add or modify their properties.
     * */
    public static void setProterty(String key, String value) {
        userProperties.put(key, value);
    }

    /**
     * @param key  property key
     * @return     whether userProperties contains the property key
     * */
    public static boolean containsProperty(String key) {
        return userProperties.contains(key);
    }

    // ~~~ properties for bootstrap
    //    public static boolean tcp_nodelay() {
    //        return getBool(Configs.TCP_NODELAY);
    //    }
    //
    //    public static boolean tcp_so_reuseaddr() {
    //        return getBool(Configs.TCP_SO_REUSEADDR);
    //    }
    //
    //    public static int tcp_so_backlog() {
    //        return getInt(Configs.TCP_SO_BACKLOG);
    //    }
    //
    //    public static boolean tcp_so_keepalive() {
    //        return getBool(Configs.TCP_SO_KEEPALIVE);
    //    }

    //    public static int netty_io_ratio() {
    //        return getInt(Configs.NETTY_IO_RATIO);
    //    }

    public static boolean netty_buffer_pooled() {
        return getBool(Configs.NETTY_BUFFER_POOLED);
    }

    public static int netty_buffer_low_watermark() {
        return getInt(Configs.NETTY_BUFFER_LOW_WATERMARK);
    }

    public static int netty_buffer_high_watermark() {
        return getInt(Configs.NETTY_BUFFER_HIGH_WATERMARK);
    }

    public static boolean netty_epoll() {
        return getBool(Configs.NETTY_EPOLL_SWITCH);
    }

    public static boolean netty_epoll_lt_enabled() {
        return getBool(Configs.NETTY_EPOLL_LT);
    }

    // ~~~ properties for idle
    //    public static boolean tcp_idle_switch() {
    //        return getBool(Configs.TCP_IDLE_SWITCH);
    //    }
    //
    //    public static int tcp_idle() {
    //        return getInt(Configs.TCP_IDLE);
    //    }
    //
    //    public static int tcp_idle_maxtimes() {
    //        return getInt(Configs.TCP_IDLE_MAXTIMES);
    //    }
    //
    //    public static int tcp_server_idle() {
    //        return getInt(Configs.TCP_SERVER_IDLE);
    //    }

    // ~~~ properties for connection manager
    //    public static int conn_create_tp_min_size() {
    //        return getInt(Configs.CONN_CREATE_TP_MIN_SIZE);
    //    }
    //
    //    public static int conn_create_tp_max_size() {
    //        return getInt(Configs.CONN_CREATE_TP_MAX_SIZE);
    //    }
    //
    //    public static int conn_create_tp_queue_size() {
    //        return getInt(Configs.CONN_CREATE_TP_QUEUE_SIZE);
    //    }
    //
    //    public static int conn_create_tp_keepalive() {
    //        return getInt(Configs.CONN_CREATE_TP_KEEPALIVE_TIME);
    //    }

    // ~~~ properties for processor manager
    //    public static int default_tp_min_size() {
    //        return getInt(Configs.TP_MIN_SIZE);
    //    }
    //
    //    public static int default_tp_max_size() {
    //        return getInt(Configs.TP_MAX_SIZE);
    //    }
    //
    //    public static int default_tp_queue_size() {
    //        return getInt(Configs.TP_QUEUE_SIZE);
    //    }
    //
    //    public static int default_tp_keepalive_time() {
    //        return getInt(Configs.TP_KEEPALIVE_TIME);
    //    }

    // ~~~ properties for reconnect manager
    public static boolean conn_reconnect_switch() {
        return getBool(Configs.CONN_RECONNECT_SWITCH);
    }

    // ~~~ properties for connection monitor
    public static boolean conn_monitor_switch() {
        return getBool(Configs.CONN_MONITOR_SWITCH);
    }

    //    public static long conn_monitor_initial_delay() {
    //        return getLong(Configs.CONN_MONITOR_INITIAL_DELAY);
    //    }
    //
    //    public static long conn_monitor_period() {
    //        return getLong(Configs.CONN_MONITOR_PERIOD);
    //    }
    //
    //    public static int conn_threshold() {
    //        return getInt(Configs.CONN_THRESHOLD);
    //    }
    //
    //    public static int retry_detect_period() {
    //        return getInt(Configs.RETRY_DETECT_PERIOD);
    //    }

    // ~~~ properties for serializer
    public static final byte serializer = serializer();

    public static byte serializer() {
        return getByte(Configs.SERIALIZER);
    }

    // ~~~ private methods
    private static String getProperty(String key) {
        return userProperties.get(key);
    }

    protected static boolean getBool(String key) {
        return Boolean.parseBoolean(getProperty(key));
    }

    protected static int getInt(String key) {
        return Integer.parseInt(getProperty(key));
    }

    protected static byte getByte(String key) {
        return Byte.parseByte(getProperty(key));
    }

    protected static long getLong(String key) {
        return Long.parseLong(getProperty(key));
    }
}
