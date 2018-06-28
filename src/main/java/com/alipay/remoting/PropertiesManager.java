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

import com.alipay.remoting.util.GlobalSwitch;

import static com.alipay.remoting.util.GlobalSwitch.PROPERTIES_SWITCH;

/**
 * PropertiesManager provide a feature that enables user to customize properties
 * when PROPERTIES_SWITCH {@link com.alipay.remoting.util.GlobalSwitch#PROPERTIES_SWITCH} is on, properties manager will return user properties first unless it can't find the property,
 * and then it will return the system property.
 * @author muyun.cyt
 * @version 2018/6/28 上午11:17
 */
public class PropertiesManager {

    /** global switch  */
    private GlobalSwitch globalSwitch;

    public PropertiesManager(GlobalSwitch globalSwitch) {
        this.globalSwitch = globalSwitch;
    }

    // ~~~ properties for bootstrap
    public boolean tcp_nodelay() {
        //        if (globalSwitch.isOn(PROPERTIES_SWITCH)){
        //            return UserProperties.containsProperty(Configs.TCP_NODELAY)?SystemProperties.tcp_nodelay():UserProperties.tcp_nodelay();
        //        }
        return SystemProperties.tcp_nodelay();
    }

    public boolean tcp_so_reuseaddr() {
        //        if (globalSwitch.isOn(PROPERTIES_SWITCH)){
        //            return UserProperties.containsProperty(Configs.TCP_SO_REUSEADDR)?SystemProperties.tcp_so_reuseaddr():UserProperties.tcp_so_reuseaddr();
        //        }
        return SystemProperties.tcp_so_reuseaddr();
    }

    public int tcp_so_backlog() {
        //        if (globalSwitch.isOn(PROPERTIES_SWITCH)){
        //            return UserProperties.containsProperty(Configs.TCP_SO_BACKLOG)?SystemProperties.tcp_so_backlog():UserProperties.tcp_so_backlog();
        //        }
        return SystemProperties.tcp_so_backlog();
    }

    public boolean tcp_so_keepalive() {
        //        if (globalSwitch.isOn(PROPERTIES_SWITCH)){
        //            return UserProperties.containsProperty(Configs.TCP_SO_KEEPALIVE)?SystemProperties.tcp_so_keepalive():UserProperties.tcp_so_keepalive();
        //        }
        return SystemProperties.tcp_so_keepalive();
    }

    public int netty_io_ratio() {
        //        if (globalSwitch.isOn(PROPERTIES_SWITCH)){
        //            return UserProperties.containsProperty(Configs.NETTY_IO_RATIO)?SystemProperties.netty_io_ratio():UserProperties.netty_io_ratio();
        //        }
        return SystemProperties.netty_io_ratio();
    }

    public boolean netty_buffer_pooled() {
        if (globalSwitch.isOn(PROPERTIES_SWITCH)) {
            return UserProperties.containsProperty(Configs.NETTY_BUFFER_POOLED) ? SystemProperties
                .netty_buffer_pooled() : UserProperties.netty_buffer_pooled();
        }
        return SystemProperties.netty_buffer_pooled();
    }

    public int netty_buffer_low_watermark() {
        if (globalSwitch.isOn(PROPERTIES_SWITCH)) {
            return UserProperties.containsProperty(Configs.NETTY_BUFFER_LOW_WATERMARK) ? SystemProperties
                .netty_buffer_low_watermark() : UserProperties.netty_buffer_low_watermark();
        }
        return SystemProperties.netty_buffer_low_watermark();
    }

    public int netty_buffer_high_watermark() {
        if (globalSwitch.isOn(PROPERTIES_SWITCH)) {
            return UserProperties.containsProperty(Configs.NETTY_BUFFER_HIGH_WATERMARK) ? SystemProperties
                .netty_buffer_high_watermark() : UserProperties.netty_buffer_high_watermark();
        }
        return SystemProperties.netty_buffer_high_watermark();
    }

    public boolean netty_epoll() {
        if (globalSwitch.isOn(PROPERTIES_SWITCH)) {
            return UserProperties.containsProperty(Configs.NETTY_EPOLL_SWITCH) ? SystemProperties
                .netty_epoll() : UserProperties.netty_epoll();
        }
        return SystemProperties.netty_epoll();
    }

    public boolean netty_epoll_lt_enabled() {
        if (globalSwitch.isOn(PROPERTIES_SWITCH)) {
            return UserProperties.containsProperty(Configs.NETTY_EPOLL_LT) ? SystemProperties
                .netty_epoll_lt_enabled() : UserProperties.netty_epoll_lt_enabled();
        }
        return SystemProperties.netty_epoll_lt_enabled();
    }

    // ~~~ properties for idle
    public boolean tcp_idle_switch() {
        //        if (globalSwitch.isOn(PROPERTIES_SWITCH)){
        //            return UserProperties.containsProperty(Configs.TCP_IDLE_SWITCH)?SystemProperties.tcp_idle_switch():UserProperties.tcp_idle_switch();
        //        }
        return SystemProperties.tcp_idle_switch();
    }

    public int tcp_idle() {
        //        if (globalSwitch.isOn(PROPERTIES_SWITCH)){
        //            return UserProperties.containsProperty(Configs.TCP_IDLE)?SystemProperties.tcp_idle():UserProperties.tcp_idle();
        //        }
        return SystemProperties.tcp_idle();
    }

    public int tcp_idle_maxtimes() {
        //        if (globalSwitch.isOn(PROPERTIES_SWITCH)){
        //            return UserProperties.containsProperty(Configs.TCP_IDLE_MAXTIMES)?SystemProperties.tcp_idle_maxtimes():UserProperties.tcp_idle_maxtimes();
        //        }
        return SystemProperties.tcp_idle_maxtimes();
    }

    public int tcp_server_idle() {
        //        if (globalSwitch.isOn(PROPERTIES_SWITCH)){
        //            return UserProperties.containsProperty(Configs.TCP_SERVER_IDLE)?SystemProperties.tcp_server_idle():UserProperties.tcp_server_idle();
        //        }
        return SystemProperties.tcp_server_idle();
    }

    // ~~~ properties for connection manager
    public int conn_create_tp_min_size() {
        //        if (globalSwitch.isOn(PROPERTIES_SWITCH)){
        //            return UserProperties.containsProperty(Configs.CONN_CREATE_TP_MIN_SIZE)?SystemProperties.conn_create_tp_min_size():UserProperties.conn_create_tp_min_size();
        //        }
        return SystemProperties.conn_create_tp_min_size();
    }

    public int conn_create_tp_max_size() {
        //        if (globalSwitch.isOn(PROPERTIES_SWITCH)){
        //            return UserProperties.containsProperty(Configs.CONN_CREATE_TP_MAX_SIZE)?SystemProperties.conn_create_tp_max_size():UserProperties.conn_create_tp_max_size();
        //        }
        return SystemProperties.conn_create_tp_max_size();
    }

    public int conn_create_tp_queue_size() {
        //        if (globalSwitch.isOn(PROPERTIES_SWITCH)){
        //            return UserProperties.containsProperty(Configs.CONN_CREATE_TP_QUEUE_SIZE)?SystemProperties.conn_create_tp_queue_size():UserProperties.conn_create_tp_queue_size();
        //        }
        return SystemProperties.conn_create_tp_queue_size();
    }

    public int conn_create_tp_keepalive() {
        //        if (globalSwitch.isOn(PROPERTIES_SWITCH)){
        //            return UserProperties.containsProperty(Configs.CONN_CREATE_TP_KEEPALIVE_TIME)?SystemProperties.conn_create_tp_keepalive():UserProperties.conn_create_tp_keepalive();
        //        }
        return SystemProperties.conn_create_tp_keepalive();
    }

    // ~~~ properties for processor manager
    public int default_tp_min_size() {
        //        if (globalSwitch.isOn(PROPERTIES_SWITCH)){
        //            return UserProperties.containsProperty(Configs.TP_MIN_SIZE)?SystemProperties.default_tp_min_size():UserProperties.default_tp_min_size();
        //        }
        return SystemProperties.default_tp_min_size();
    }

    public int default_tp_max_size() {
        //        if (globalSwitch.isOn(PROPERTIES_SWITCH)){
        //            return UserProperties.containsProperty(Configs.TP_MAX_SIZE)?SystemProperties.default_tp_max_size():UserProperties.default_tp_max_size();
        //        }
        return SystemProperties.default_tp_max_size();
    }

    public int default_tp_queue_size() {
        //        if (globalSwitch.isOn(PROPERTIES_SWITCH)){
        //            return UserProperties.containsProperty(Configs.TP_QUEUE_SIZE)?SystemProperties.default_tp_queue_size():UserProperties.default_tp_queue_size();
        //        }
        return SystemProperties.default_tp_queue_size();
    }

    public int default_tp_keepalive_time() {
        //        if (globalSwitch.isOn(PROPERTIES_SWITCH)){
        //            return UserProperties.containsProperty(Configs.TP_KEEPALIVE_TIME)?SystemProperties.default_tp_keepalive_time():UserProperties.default_tp_keepalive_time();
        //        }
        return SystemProperties.default_tp_keepalive_time();
    }

    // ~~~ properties for reconnect manager
    public boolean conn_reconnect_switch() {
        if (globalSwitch.isOn(PROPERTIES_SWITCH)) {
            return UserProperties.containsProperty(Configs.CONN_RECONNECT_SWITCH) ? SystemProperties
                .conn_reconnect_switch() : UserProperties.conn_reconnect_switch();
        }
        return SystemProperties.conn_reconnect_switch();
    }

    // ~~~ properties for connection monitor
    public boolean conn_monitor_switch() {
        if (globalSwitch.isOn(PROPERTIES_SWITCH)) {
            return UserProperties.containsProperty(Configs.CONN_MONITOR_SWITCH) ? SystemProperties
                .conn_monitor_switch() : UserProperties.conn_monitor_switch();
        }
        return SystemProperties.conn_monitor_switch();
    }

    public long conn_monitor_initial_delay() {
        //        if (globalSwitch.isOn(PROPERTIES_SWITCH)){
        //            return UserProperties.containsProperty(Configs.CONN_MONITOR_INITIAL_DELAY)?SystemProperties.conn_monitor_initial_delay():UserProperties.conn_monitor_initial_delay();
        //        }
        return SystemProperties.conn_monitor_initial_delay();
    }

    public long conn_monitor_period() {
        //        if (globalSwitch.isOn(PROPERTIES_SWITCH)){
        //            return UserProperties.containsProperty(Configs.CONN_MONITOR_PERIOD)?SystemProperties.conn_monitor_period():UserProperties.conn_monitor_period();
        //        }
        return SystemProperties.conn_monitor_period();
    }

    public int conn_threshold() {
        //        if (globalSwitch.isOn(PROPERTIES_SWITCH)){
        //            return UserProperties.containsProperty(Configs.CONN_THRESHOLD)?SystemProperties.conn_threshold():UserProperties.conn_threshold();
        //        }
        return SystemProperties.conn_threshold();
    }

    public int retry_detect_period() {
        //        if (globalSwitch.isOn(PROPERTIES_SWITCH)){
        //            return UserProperties.containsProperty(Configs.RETRY_DETECT_PERIOD)?SystemProperties.retry_detect_period():UserProperties.retry_detect_period();
        //        }
        return SystemProperties.retry_detect_period();
    }

    // ~~~ properties for serializer
    public final byte serializer = serializer();

    public byte serializer() {
        //        if (globalSwitch.isOn(PROPERTIES_SWITCH)){
        //            return UserProperties.containsProperty(Configs.SERIALIZER)?SystemProperties.serializer():UserProperties.serializer();
        //        }
        return SystemProperties.serializer();
    }

}
