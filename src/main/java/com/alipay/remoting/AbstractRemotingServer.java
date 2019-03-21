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

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;

import com.alipay.remoting.config.BoltOption;
import com.alipay.remoting.config.BoltOptions;
import com.alipay.remoting.config.ConfigManager;
import com.alipay.remoting.config.Configurable;
import com.alipay.remoting.config.ConfigurableInstance;
import com.alipay.remoting.config.configs.ConfigContainer;
import com.alipay.remoting.config.configs.ConfigItem;
import com.alipay.remoting.config.configs.DefaultConfigContainer;
import com.alipay.remoting.config.switches.GlobalSwitch;
import org.slf4j.Logger;

import com.alipay.remoting.config.configs.ConfigType;
import com.alipay.remoting.log.BoltLoggerFactory;

/**
 * Server template for remoting.
 * 
 * @author jiangping
 * @version $Id: AbstractRemotingServer.java, v 0.1 2015-9-5 PM7:37:48 tao Exp $
 */
public abstract class AbstractRemotingServer extends AbstractLifeCycle implements RemotingServer,
                                                                      ConfigurableInstance {

    private static final Logger   logger  = BoltLoggerFactory.getLogger("CommonDefault");

    private AtomicBoolean         started = new AtomicBoolean(false);
    private String                ip;
    private int                   port;

    private final BoltOptions     options;
    private final ConfigType      configType;
    private final GlobalSwitch    globalSwitch;
    private final ConfigContainer configContainer;

    public AbstractRemotingServer(int port) {
        this(new InetSocketAddress(port).getAddress().getHostAddress(), port);
    }

    public AbstractRemotingServer(String ip, int port) {
        this.ip = ip;
        this.port = port;

        this.options = new BoltOptions();
        this.configType = ConfigType.SERVER_SIDE;
        this.globalSwitch = new GlobalSwitch();
        this.configContainer = new DefaultConfigContainer();
    }

    @Override
    public void init() {
        // Do not call this method, it will be removed in the next version
    }

    @Override
    public boolean start() {
        if (started.compareAndSet(false, true)) {
            try {
                doInit();

                logger.warn("Prepare to start server on port {} ", port);
                if (doStart()) {
                    logger.warn("Server started on port {}", port);
                    return true;
                } else {
                    logger.warn("Failed starting server on port {}", port);
                    return false;
                }
            } catch (Throwable t) {
                this.stop();// do stop to ensure close resources created during doInit()
                throw new IllegalStateException("ERROR: Failed to start the Server!", t);
            }
        } else {
            String errMsg = "ERROR: The server has already started!";
            logger.error(errMsg);
            throw new IllegalStateException(errMsg);
        }
    }

    @Override
    public boolean stop() {
        if (started.compareAndSet(true, false)) {
            return this.doStop();
        } else {
            throw new IllegalStateException("ERROR: The server has already stopped!");
        }
    }

    @Override
    public String ip() {
        return ip;
    }

    @Override
    public int port() {
        return port;
    }

    protected abstract void doInit();

    protected abstract boolean doStart() throws InterruptedException;

    protected abstract boolean doStop();

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
        if (configContainer.contains(configType, ConfigItem.NETTY_BUFFER_LOW_WATER_MARK)) {
            return (Integer) configContainer
                .get(configType, ConfigItem.NETTY_BUFFER_LOW_WATER_MARK);
        } else {
            return ConfigManager.netty_buffer_low_watermark();
        }
    }

    @Override
    public int netty_buffer_high_watermark() {
        if (configContainer.contains(configType, ConfigItem.NETTY_BUFFER_HIGH_WATER_MARK)) {
            return (Integer) configContainer.get(configType,
                ConfigItem.NETTY_BUFFER_HIGH_WATER_MARK);
        } else {
            return ConfigManager.netty_buffer_high_watermark();
        }
    }
}
