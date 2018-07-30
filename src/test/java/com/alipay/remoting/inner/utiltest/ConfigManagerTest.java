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
package com.alipay.remoting.inner.utiltest;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.alipay.remoting.config.ConfigManager;
import com.alipay.remoting.config.Configs;

/**
 * test ConfigManager get config
 * 
 * @author tsui
 * @version $Id: ConfigManagerTest.java, v 0.1 2017-08-03 21:51 tsui Exp $
 */
public class ConfigManagerTest {
    @BeforeClass
    public static void initClass() {
    }

    @Before
    public void init() {
    }

    @After
    public void stop() {
    }

    @AfterClass
    public static void afterClass() {
    }

    @Test
    public void testSystemSettings() {
        Assert.assertNotNull(ConfigManager.tcp_nodelay());
        Assert.assertNotNull(ConfigManager.tcp_so_reuseaddr());
        Assert.assertNotNull(ConfigManager.tcp_so_backlog());
        Assert.assertNotNull(ConfigManager.tcp_so_keepalive());
        Assert.assertNotNull(ConfigManager.netty_io_ratio());
        Assert.assertNotNull(ConfigManager.netty_buffer_pooled());
        Assert.assertNotNull(ConfigManager.tcp_idle_switch());
        Assert.assertNotNull(ConfigManager.tcp_idle());
        Assert.assertNotNull(ConfigManager.tcp_idle_maxtimes());
        Assert.assertNotNull(ConfigManager.tcp_server_idle());
        Assert.assertNotNull(ConfigManager.conn_create_tp_min_size());
        Assert.assertNotNull(ConfigManager.conn_create_tp_max_size());
        Assert.assertNotNull(ConfigManager.conn_create_tp_queue_size());
        Assert.assertNotNull(ConfigManager.conn_create_tp_keepalive());
        Assert.assertNotNull(ConfigManager.conn_reconnect_switch());
        Assert.assertNotNull(ConfigManager.conn_monitor_switch());
        Assert.assertNotNull(ConfigManager.conn_monitor_initial_delay());
        Assert.assertNotNull(ConfigManager.conn_monitor_period());
        Assert.assertNotNull(ConfigManager.conn_threshold());
        Assert.assertNotNull(ConfigManager.retry_detect_period());
        Assert.assertNotNull(ConfigManager.serializer());
        int low_default = Integer.parseInt(Configs.NETTY_BUFFER_LOW_WATERMARK_DEFAULT);
        int high_default = Integer.parseInt(Configs.NETTY_BUFFER_HIGH_WATERMARK_DEFAULT);
        Assert.assertEquals(low_default, ConfigManager.netty_buffer_low_watermark());
        Assert.assertEquals(high_default, ConfigManager.netty_buffer_high_watermark());
    }
}