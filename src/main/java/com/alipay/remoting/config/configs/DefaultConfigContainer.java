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

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;

import com.alipay.remoting.log.BoltLoggerFactory;

/**
 * default implementation for config container
 *
 * @author tsui
 * @version $Id: DefaultConfigContainer.java, v 0.1 2018-07-28 18:11 tsui Exp $$
 */
public class DefaultConfigContainer implements ConfigContainer {
    /** logger */
    private static final Logger                      logger      = BoltLoggerFactory
                                                                     .getLogger("CommonDefault");

    /**
     * use a hash map to store the user configs with different config types and config items.
     */
    private Map<ConfigType, Map<ConfigItem, Object>> userConfigs = new HashMap<ConfigType, Map<ConfigItem, Object>>();

    @Override
    public boolean contains(ConfigType configType, ConfigItem configItem) {
        validate(configType, configItem);
        return null != userConfigs.get(configType)
               && userConfigs.get(configType).containsKey(configItem);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(ConfigType configType, ConfigItem configItem) {
        validate(configType, configItem);
        if (userConfigs.containsKey(configType)) {
            return (T) userConfigs.get(configType).get(configItem);
        }
        return null;
    }

    @Override
    public void set(ConfigType configType, ConfigItem configItem, Object value) {
        validate(configType, configItem, value);
        Map<ConfigItem, Object> items = userConfigs.get(configType);
        if (null == items) {
            items = new HashMap<ConfigItem, Object>();
            userConfigs.put(configType, items);
        }
        Object prev = items.put(configItem, value);
        if (null != prev) {
            logger.warn("the value of ConfigType {}, ConfigItem {} changed from {} to {}",
                configType, configItem, prev.toString(), value.toString());
        }
    }

    private void validate(ConfigType configType, ConfigItem configItem) {
        if (null == configType || null == configItem) {
            throw new IllegalArgumentException(String.format(
                "ConfigType {%s}, ConfigItem {%s} should not be null!", configType, configItem));
        }
    }

    private void validate(ConfigType configType, ConfigItem configItem, Object value) {
        if (null == configType || null == configItem || null == value) {
            throw new IllegalArgumentException(String.format(
                "ConfigType {%s}, ConfigItem {%s}, value {%s} should not be null!", configType,
                configItem, value == null ? null : value.toString()));
        }
    }
}