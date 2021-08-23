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

import com.alipay.remoting.config.BoltClientOption;
import com.alipay.remoting.config.BoltOption;
import com.alipay.remoting.config.BoltOptions;
import com.alipay.remoting.config.ConfigManager;
import com.alipay.remoting.config.Configuration;
import com.alipay.remoting.config.ConfigurableInstance;
import com.alipay.remoting.config.configs.ConfigContainer;
import com.alipay.remoting.config.configs.DefaultConfigContainer;
import com.alipay.remoting.config.switches.GlobalSwitch;

/**
 * @author chengyi (mark.lx@antfin.com) 2018-11-07 15:22
 */
public abstract class AbstractBoltClient extends AbstractLifeCycle implements BoltClient,
                                                                  ConfigurableInstance {

    private final BoltOptions     options;
    private final GlobalSwitch    globalSwitch;
    private final ConfigContainer configContainer;

    public AbstractBoltClient() {
        this.options = new BoltOptions();
        this.globalSwitch = new GlobalSwitch();
        if (ConfigManager.conn_reconnect_switch()) {
            option(BoltClientOption.CONN_RECONNECT_SWITCH, true);
        } else {
            option(BoltClientOption.CONN_RECONNECT_SWITCH, false);
        }

        if (ConfigManager.conn_monitor_switch()) {
            option(BoltClientOption.CONN_MONITOR_SWITCH, true);
        } else {
            option(BoltClientOption.CONN_MONITOR_SWITCH, false);
        }
        this.configContainer = new DefaultConfigContainer();
    }

    @Override
    public <T> T option(BoltOption<T> option) {
        return options.option(option);
    }

    @Override
    public <T> Configuration option(BoltOption<T> option, T value) {
        options.option(option, value);
        return this;
    }

    @Override
    @Deprecated
    public ConfigContainer conf() {
        return this.configContainer;
    }

    @Override
    @Deprecated
    public GlobalSwitch switches() {
        return this.globalSwitch;
    }

    @Override
    public void initWriteBufferWaterMark(int low, int high) {
        option(BoltClientOption.NETTY_BUFFER_LOW_WATER_MARK, low);
        option(BoltClientOption.NETTY_BUFFER_HIGH_WATER_MARK, high);
    }

    @Override
    public int netty_buffer_low_watermark() {
        return option(BoltClientOption.NETTY_BUFFER_LOW_WATER_MARK);
    }

    @Override
    public int netty_buffer_high_watermark() {
        return option(BoltClientOption.NETTY_BUFFER_HIGH_WATER_MARK);
    }
}
