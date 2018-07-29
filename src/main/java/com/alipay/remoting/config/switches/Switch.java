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
package com.alipay.remoting.config.switches;

/**
 * switch interface
 *
 * @author tsui
 * @version $Id: Switch.java, v 0.1 2018-04-08 11:26 tsui Exp $
 */
public interface Switch {
    /**
     * api for user to turn on a feature
     *
     * @param index the switch index of feature
     */
    void turnOn(int index);

    /**
     * api for user to turn off a feature
     * @param index the switch index of feature
     */
    void turnOff(int index);

    /**
     * check switch whether on
     *
     * @param index the switch index of feature
     * @return true if either system setting is on or user setting is on
     */
    boolean isOn(int index);
}