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

import java.util.Arrays;

/**
 * Protocol code definition, you can define your own protocol code in byte array {@link ProtocolCode#version}
 * We suggest to use just one byte for simplicity.
 *
 * @author tsui
 * @version $Id: ProtocolCode.java, v 0.1 2018-03-27 17:23 tsui Exp $
 */
public class ProtocolCode {
    /** bytes to represent protocol code */
    byte[] version;

    private ProtocolCode(byte... version) {
        this.version = version;
    }

    public static ProtocolCode fromBytes(byte... version) {
        return new ProtocolCode(version);
    }

    /**
     * get the first single byte if your protocol code is single code.
     * @return
     */
    public byte getFirstByte() {
        return this.version[0];
    }

    public int length() {
        return this.version.length;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ProtocolCode that = (ProtocolCode) o;
        return Arrays.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(version);
    }

    @Override
    public String toString() {
        return "ProtocolVersion{" + "version=" + Arrays.toString(version) + '}';
    }
}