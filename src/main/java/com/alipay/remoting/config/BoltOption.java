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

/**
 * The base implementation class of the configuration item.
 *
 * @author chengyi (mark.lx@antfin.com) 2018-11-06 17:25
 */
public class BoltOption<T> {

    private final String name;
    private T            defaultValue;

    protected BoltOption(String name, T defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
    }

    public String name() {
        return name;
    }

    public T defaultValue() {
        return defaultValue;
    }

    public static <T> BoltOption<T> valueOf(String name) {
        return new BoltOption<T>(name, null);
    }

    public static <T> BoltOption<T> valueOf(String name, T defaultValue) {
        return new BoltOption<T>(name, defaultValue);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BoltOption<?> that = (BoltOption<?>) o;

        return name != null ? name.equals(that.name) : that.name == null;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
