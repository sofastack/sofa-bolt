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
package com.alipay.remoting.config.configs;

/**
 * the interface of a config container
 *
 * Mainly used to manage config by user api, this is instance related, not globally.
 * That is to say, different remoting instance client or server hold different ConfigContainer.
 *
 * @author tsui
 * @version $Id: ConfigContainer.java, v 0.1 2018-07-28 18:31 tsui Exp $$
 */
public interface ConfigContainer {
    /**
     * check whether a config item of a certain config type exist.
     * @param configType config types in the config container, different config type can hold the same config item key
     * @param configItem config items in the config container
     * @return exist then return true, not exist return alse
     */
    boolean contains(ConfigType configType, ConfigItem configItem);

    /**
     * try to get config value using config type and config item.
     * @param configType config types in the config container, different config type can hold the same config item key
     * @param configItem config items in the config container
     * @param <T> the generics of return value
     * @return the right value and cast to type T, if no mappings, then return null
     */
    <T> T get(ConfigType configType, ConfigItem configItem);

    /**
     * init a config item with certain config type, and the value can be any type.
     * Notice: the value can be overwrite if you set more than once.
     *
     * @param configType config types in the config container, different config type can hold the same config item key
     * @param configItem config items in the config container
     */
    void set(ConfigType configType, ConfigItem configItem, Object value);
}