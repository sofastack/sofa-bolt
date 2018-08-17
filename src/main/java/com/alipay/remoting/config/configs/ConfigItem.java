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
 * Items of config.
 *
 * Mainly used to define some config items managed by {@link ConfigContainer}.
 * You can define new config items based on this if need.
 *
 * @author tsui
 * @version $Id: ConfigItem.java, v 0.1 2018-07-28 17:43 tsui Exp $$ 
 */
public enum ConfigItem {
    // ~~~ netty related
    NETTY_BUFFER_LOW_WATER_MARK, // netty writer buffer low water mark
    NETTY_BUFFER_HIGH_WATER_MARK // netty writer buffer high water mark
}