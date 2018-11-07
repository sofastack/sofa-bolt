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

import java.util.concurrent.ConcurrentHashMap;

/**
 * Option carrier.
 *
 * @author chengyi (mark.lx@antfin.com) 2018-11-06 17:42
 */
public class BoltOptions {

    private ConcurrentHashMap<BoltOption<?>, Object> options = new ConcurrentHashMap<BoltOption<?>, Object>();

    /**
     * Get the optioned value.
     * Return default value if option does not exist.
     *
     * @param option target option
     * @return the optioned value of default value if option does not exist.
     */
    @SuppressWarnings("unchecked")
    public <T> T option(BoltOption<T> option) {
        Object value = options.get(option);
        if (value == null) {
            value = option.defaultValue();
        }

        return value == null ? null : (T) value;
    }

    /**
     * Set up an new option with specific value.
     * Use a value of {@code null} to remove a previous set {@link BoltOption}.
     *
     * @param option target option
     * @param value option value, null for remove a previous set {@link BoltOption}.
     * @return this BoltOptions instance
     */
    public <T> BoltOptions option(BoltOption<T> option, T value) {
        if (value == null) {
            options.remove(option);
            return this;
        }

        options.put(option, value);
        return this;
    }
}
