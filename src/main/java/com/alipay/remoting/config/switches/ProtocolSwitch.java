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

import java.util.BitSet;

/**
 * Switches used in protocol, this is runtime switch.
 *
 * @author tsui
 * @version $Id: RpcProtocolSwitch.java, v 0.1 2017-09-31 15:50 tsui Exp $
 */
public class ProtocolSwitch implements Switch {

    // switche index
    public static final int     CRC_SWITCH_INDEX         = 0x000;

    // default value
    public static final boolean CRC_SWITCH_DEFAULT_VALUE = true;

    /** protocol switches */
    private BitSet              bs                       = new BitSet();

    // ~~~ public methods

    @Override
    public void turnOn(int index) {
        this.bs.set(index);
    }

    @Override
    public void turnOff(int index) {
        this.bs.clear(index);
    }

    @Override
    public boolean isOn(int index) {
        return this.bs.get(index);
    }

    /**
     * generate byte value according to the bit set in ProtocolSwitchStatus
     */
    public byte toByte() {
        return toByte(this.bs);
    }

    //~~~ static methods

    /**
     * check switch status whether on according to specified value
     *
     * @param switchIndex
     * @param value
     * @return
     */
    public static boolean isOn(int switchIndex, int value) {
        return toBitSet(value).get(switchIndex);
    }

    /**
     * create an instance of {@link ProtocolSwitch} according to byte value
     * 
     * @param value
     * @return ProtocolSwitchStatus with initialized bit set.
     */
    public static ProtocolSwitch create(int value) {
        ProtocolSwitch status = new ProtocolSwitch();
        status.setBs(toBitSet(value));
        return status;
    }

    /**
     * create an instance of {@link ProtocolSwitch} according to switch index
     *
     * @param index the switch index which you want to set true
     * @return ProtocolSwitchStatus with initialized bit set.
     */
    public static ProtocolSwitch create(int[] index) {
        ProtocolSwitch status = new ProtocolSwitch();
        for (int i = 0; i < index.length; ++i) {
            status.turnOn(index[i]);
        }
        return status;
    }

    /**
     * from bit set to byte
     * @param bs
     * @return byte represent the bit set
     */
    public static byte toByte(BitSet bs) {
        int value = 0;
        for (int i = 0; i < bs.length(); ++i) {
            if (bs.get(i)) {
                value += 1 << i;
            }
        }
        if (bs.length() > 7) {
            throw new IllegalArgumentException("The byte value " + value
                                               + " generated according to bit set " + bs
                                               + " is out of range, should be limited between ["
                                               + Byte.MIN_VALUE + "] to [" + Byte.MAX_VALUE + "]");
        }
        return (byte) value;
    }

    /**
     * from byte to bit set
     * @param value
     * @return bit set represent the byte
     */
    public static BitSet toBitSet(int value) {
        if (value > Byte.MAX_VALUE || value < Byte.MIN_VALUE) {
            throw new IllegalArgumentException(
                "The value " + value + " is out of byte range, should be limited between ["
                        + Byte.MIN_VALUE + "] to [" + Byte.MAX_VALUE + "]");
        }
        BitSet bs = new BitSet();
        int index = 0;
        while (value != 0) {
            if (value % 2 != 0) {
                bs.set(index);
            }
            ++index;
            value = (byte) (value >> 1);
        }
        return bs;
    }

    // ~~~ getter and setters

    /**
     * Setter method for property <tt>bs<tt>.
     *
     * @param bs value to be assigned to property bs
     */
    public void setBs(BitSet bs) {
        this.bs = bs;
    }

}