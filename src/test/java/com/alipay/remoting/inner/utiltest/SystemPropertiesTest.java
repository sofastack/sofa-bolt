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

import com.alipay.remoting.SystemProperties;

/**
 * test system properties always with default value
 * 
 * @author tsui
 * @version $Id: SystemPropertiesTest.java, v 0.1 2017-08-03 21:51 tsui Exp $
 */
public class SystemPropertiesTest {
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
    public void test() {
        Assert.assertNotNull(SystemProperties.tcp_nodelay());
        Assert.assertNotNull(SystemProperties.tcp_so_reuseaddr());
        Assert.assertNotNull(SystemProperties.tcp_so_backlog());
        Assert.assertNotNull(SystemProperties.tcp_so_keepalive());
        Assert.assertNotNull(SystemProperties.netty_io_ratio());
        Assert.assertNotNull(SystemProperties.netty_buffer_pooled());
        Assert.assertNotNull(SystemProperties.netty_buffer_low_watermark());
        Assert.assertNotNull(SystemProperties.netty_buffer_high_watermark());
        Assert.assertNotNull(SystemProperties.tcp_idle_switch());
        Assert.assertNotNull(SystemProperties.tcp_idle());
        Assert.assertNotNull(SystemProperties.tcp_idle_maxtimes());
        Assert.assertNotNull(SystemProperties.tcp_server_idle());
        Assert.assertNotNull(SystemProperties.conn_create_tp_min_size());
        Assert.assertNotNull(SystemProperties.conn_create_tp_max_size());
        Assert.assertNotNull(SystemProperties.conn_create_tp_queue_size());
        Assert.assertNotNull(SystemProperties.conn_create_tp_keepalive());
        Assert.assertNotNull(SystemProperties.conn_reconnect_switch());
        Assert.assertNotNull(SystemProperties.conn_monitor_switch());
        Assert.assertNotNull(SystemProperties.conn_monitor_initial_delay());
        Assert.assertNotNull(SystemProperties.conn_monitor_period());
        Assert.assertNotNull(SystemProperties.conn_threshold());
        Assert.assertNotNull(SystemProperties.retry_detect_period());
        Assert.assertNotNull(SystemProperties.serializer());
    }
}