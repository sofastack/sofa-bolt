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

import com.alipay.remoting.config.BoltOption;
import com.alipay.remoting.config.BoltOptions;
import com.alipay.remoting.config.ConfigManager;
import com.alipay.remoting.config.Configurable;
import com.alipay.remoting.config.ConfigurableInstance;
import com.alipay.remoting.config.configs.ConfigContainer;
import com.alipay.remoting.config.configs.ConfigItem;
import com.alipay.remoting.config.configs.ConfigType;
import com.alipay.remoting.config.configs.DefaultConfigContainer;
import com.alipay.remoting.config.switches.GlobalSwitch;

/**
 * @author chengyi (mark.lx@antfin.com) 2018-11-07 15:22
 */
public abstract class AbstractBoltClient extends AbstractLifeCycle implements BoltClient,
                                                                  ConfigurableInstance {

    private final BoltOptions     options;
    private final ConfigType      configType;
    private final GlobalSwitch    globalSwitch;
    private final ConfigContainer configContainer;

    public AbstractBoltClient() {
        this.options = new BoltOptions();
        this.configType = ConfigType.CLIENT_SIDE;
        this.globalSwitch = new GlobalSwitch();
        this.configContainer = new DefaultConfigContainer();
    }

    @Override
    public <T> T option(BoltOption<T> option) {
        return options.option(option);
    }

    @Override
    public <T> Configurable option(BoltOption<T> option, T value) {
        options.option(option, value);
        return this;
    }

    @Override
    public ConfigContainer conf() {
        return this.configContainer;
    }

    @Override
    public GlobalSwitch switches() {
        return this.globalSwitch;
    }

    @Override
    public void initWriteBufferWaterMark(int low, int high) {
        this.configContainer.set(configType, ConfigItem.NETTY_BUFFER_LOW_WATER_MARK, low);
        this.configContainer.set(configType, ConfigItem.NETTY_BUFFER_HIGH_WATER_MARK, high);
    }

    @Override
    public int netty_buffer_low_watermark() {
        Object config = configContainer.get(configType, ConfigItem.NETTY_BUFFER_LOW_WATER_MARK);
        if (config != null) {
            return (Integer) config;
        } else {
            return ConfigManager.netty_buffer_low_watermark();
        }
    }

    @Override
    public int netty_buffer_high_watermark() {
        Object config = configContainer.get(configType, ConfigItem.NETTY_BUFFER_HIGH_WATER_MARK);
        if (config != null) {
            return (Integer) config;
        } else {
            return ConfigManager.netty_buffer_high_watermark();
        }
    }
}
