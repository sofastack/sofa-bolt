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

import com.alipay.remoting.config.configs.ConfigContainer;
import com.alipay.remoting.config.switches.GlobalSwitch;

/**
 * define an interface which can be used to implement configurable apis.
 *
 * @author tsui
 * @version $Id: ConfigurableInstance.java, v 0.1 2018-07-30 21:09 tsui Exp $$
 */
public interface ConfigurableInstance {

    /**
     * get the config container for current instance
     *
     * @return the config container
     */
    @Deprecated
    ConfigContainer conf();

    /**
     * get the global switch for current instance
     * @return the global switch
     */
    @Deprecated
    GlobalSwitch switches();

    /**
     * Initialize netty write buffer water mark for remoting instance.
     * <p>
     * Notice: This api should be called before init remoting instance.
     *
     * deprecated, use option() instead:
     * instance#option(BoltOption.NETTY_BUFFER_LOW_WATER_MARK, low);
     * instance#option(BoltOption.NETTY_BUFFER_HIGH_WATER_MARK, high);
     *
     * @param low [0, high]
     * @param high [high, Integer.MAX_VALUE)
     */
    @Deprecated
    void initWriteBufferWaterMark(int low, int high);

    /**
     * get the low water mark for netty write buffer
     * deprecated, use instance#option(BoltClientOption.NETTY_BUFFER_LOW_WATER_MARK) instead
     * @return low watermark
     */
    @Deprecated
    int netty_buffer_low_watermark();

    /**
     * get the high water mark for netty write buffer
     * deprecated, use instance#option(BoltClientOption.NETTY_BUFFER_HIGH_WATER_MARK) instead
     * @return high watermark
     */
    @Deprecated
    int netty_buffer_high_watermark();
}