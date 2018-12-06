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

import com.alipay.remoting.Configs;
import com.alipay.remoting.rpc.RpcClient;
import com.alipay.remoting.util.GlobalSwitch;

/**
 *
 * @author tsui
 * @version $Id: SwitchStatusTest.java, v 0.1 2017-08-03 17:25 tsui Exp $
 */
public class GlobalSwitchTest {
    private RpcClient client1;
    private RpcClient client2;

    @BeforeClass
    public static void initClass() {
    }

    @Before
    public void init() {
        client1 = new RpcClient();
        client2 = new RpcClient();
    }

    @After
    public void stop() {
    }

    @AfterClass
    public static void afterClass() {
    }

    @Test
    public void testSystemSettingsTrue_UserSettingsTrue() {
        System.setProperty(Configs.CONN_RECONNECT_SWITCH, "true");
        System.setProperty(Configs.CONN_MONITOR_SWITCH, "true");
        GlobalSwitch.reinit();

        client1.enableConnectionMonitorSwitch();
        client1.enableReconnectSwitch();

        Assert.assertTrue(client1.isConnectionMonitorSwitchOn());
        Assert.assertTrue(client1.isReconnectSwitchOn());
        Assert.assertTrue(client2.isConnectionMonitorSwitchOn());
        Assert.assertTrue(client2.isReconnectSwitchOn());
    }

    @Test
    public void testSystemSettingsTrue_UserSettingsFalse() {
        System.setProperty(Configs.CONN_RECONNECT_SWITCH, "true");
        System.setProperty(Configs.CONN_MONITOR_SWITCH, "true");
        GlobalSwitch.reinit();

        Assert.assertTrue(client1.isConnectionMonitorSwitchOn());
        Assert.assertTrue(client1.isReconnectSwitchOn());
        Assert.assertTrue(client2.isConnectionMonitorSwitchOn());
        Assert.assertTrue(client2.isReconnectSwitchOn());
    }

    @Test
    public void testSystemSettingsFlase_UserSettingsTrue() {
        System.setProperty(Configs.CONN_RECONNECT_SWITCH, "false");
        System.setProperty(Configs.CONN_MONITOR_SWITCH, "false");
        GlobalSwitch.reinit();

        client2.enableConnectionMonitorSwitch();
        client2.enableReconnectSwitch();
        Assert.assertFalse(client1.isConnectionMonitorSwitchOn());
        Assert.assertFalse(client1.isReconnectSwitchOn());
        Assert.assertTrue(client2.isConnectionMonitorSwitchOn());
        Assert.assertTrue(client2.isReconnectSwitchOn());
    }

    @Test
    public void testSystemSettingsFalse_UserSettingsFalse() {
        System.setProperty(Configs.CONN_RECONNECT_SWITCH, "false");
        System.setProperty(Configs.CONN_MONITOR_SWITCH, "false");
        GlobalSwitch.reinit();

        Assert.assertFalse(client1.isConnectionMonitorSwitchOn());
        Assert.assertFalse(client1.isReconnectSwitchOn());
        Assert.assertFalse(client2.isConnectionMonitorSwitchOn());
        Assert.assertFalse(client2.isReconnectSwitchOn());
    }
}