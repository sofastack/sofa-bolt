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
package com.alipay.remoting.inner.utiltest;

import java.util.BitSet;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.alipay.remoting.config.switches.ProtocolSwitch;

/**
 *
 * @author tsui
 * @version $Id: ProtocolSwitchStatusTest.java, v 0.1 2017-10-09 20:23 tsui Exp $
 */
public class ProtocolSwitchTest {

    @BeforeClass
    public static void initClass() {
    }

    @Before
    public void init() {
    }

    @After
    public void stop() {
    }

    @AfterClass
    public static void afterClass() {
    }

    @Test
    public void test_toByte() {
        ProtocolSwitch protocolSwitch = new ProtocolSwitch();
        protocolSwitch.turnOn(0);
        Assert.assertEquals(1, protocolSwitch.toByte());

        protocolSwitch = new ProtocolSwitch();
        protocolSwitch.turnOn(1);
        Assert.assertEquals(2, protocolSwitch.toByte());

        protocolSwitch = new ProtocolSwitch();
        protocolSwitch.turnOn(2);
        Assert.assertEquals(4, protocolSwitch.toByte());

        protocolSwitch = new ProtocolSwitch();
        protocolSwitch.turnOn(3);
        Assert.assertEquals(8, protocolSwitch.toByte());

        protocolSwitch = new ProtocolSwitch();
        protocolSwitch.turnOn(0);
        protocolSwitch.turnOn(1);
        Assert.assertEquals(3, protocolSwitch.toByte());

        protocolSwitch = new ProtocolSwitch();
        protocolSwitch.turnOn(6);
        Assert.assertEquals(64, protocolSwitch.toByte());

        protocolSwitch = new ProtocolSwitch();
        for (int i = 0; i < 7; ++i) {
            protocolSwitch.turnOn(i);
        }
        Assert.assertEquals(127, protocolSwitch.toByte());

        protocolSwitch = new ProtocolSwitch();
        for (int i = 0; i < 8; ++i) {
            protocolSwitch.turnOn(i);
        }
        try {
            protocolSwitch.toByte();
            Assert.fail("Should not reach here!");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void test_createUsingIndex() {
        for (int i = 0; i < 7; ++i) {
            Assert.assertTrue(ProtocolSwitch.create(new int[] { i }).isOn(i));
        }

        int size = 7;
        int[] a = new int[size];
        for (int i = 0; i < size; ++i) {
            a[i] = i;
        }
        ProtocolSwitch status = ProtocolSwitch.create(a);
        for (int i = 0; i < size; ++i) {
            Assert.assertTrue(status.isOn(i));
        }
    }

    @Test
    public void test_createUsingByte() {
        Assert.assertFalse(ProtocolSwitch.create(0).isOn(0));
        Assert.assertTrue(ProtocolSwitch.create(1).isOn(0));
        Assert.assertTrue(ProtocolSwitch.create(2).isOn(1));
        Assert.assertTrue(ProtocolSwitch.create(4).isOn(2));
        Assert.assertTrue(ProtocolSwitch.create(8).isOn(3));
        Assert.assertTrue(ProtocolSwitch.create(3).isOn(0));
        Assert.assertTrue(ProtocolSwitch.create(3).isOn(1));
        Assert.assertFalse(ProtocolSwitch.create(3).isOn(2));
        Assert.assertTrue(ProtocolSwitch.create(64).isOn(6));
        Assert.assertFalse(ProtocolSwitch.create(64).isOn(0));
        Assert.assertTrue(ProtocolSwitch.create(127).isOn(0));
        Assert.assertTrue(ProtocolSwitch.create(127).isOn(1));
        Assert.assertTrue(ProtocolSwitch.create(127).isOn(6));
        Assert.assertFalse(ProtocolSwitch.create(127).isOn(7));
        try {
            ProtocolSwitch.create(255);
            Assert.fail("Should not reach here!");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void test_isSwitchOn() {
        for (int i = 0; i < 7; ++i) {
            Assert.assertTrue(ProtocolSwitch.isOn(i, (1 << i)));
        }
        Assert.assertTrue(ProtocolSwitch.isOn(0, 1));
        Assert.assertTrue(ProtocolSwitch.isOn(1, 2));
        Assert.assertTrue(ProtocolSwitch.isOn(2, 4));
        Assert.assertTrue(ProtocolSwitch.isOn(3, 8));
        Assert.assertTrue(ProtocolSwitch.isOn(0, 3));
        Assert.assertTrue(ProtocolSwitch.isOn(1, 3));
        Assert.assertFalse(ProtocolSwitch.isOn(2, 3));
        Assert.assertTrue(ProtocolSwitch.isOn(6, 64));
        Assert.assertFalse(ProtocolSwitch.isOn(0, 64));
        Assert.assertTrue(ProtocolSwitch.isOn(0, 127));
        Assert.assertTrue(ProtocolSwitch.isOn(1, 127));
        Assert.assertFalse(ProtocolSwitch.isOn(7, 127));
        try {
            ProtocolSwitch.isOn(7, 255);
            Assert.fail("Should not reach here!");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void test_byteToBitSet() {
        BitSet bs = ProtocolSwitch.toBitSet(1);
        Assert.assertTrue(bs.get(0));

        bs = ProtocolSwitch.toBitSet(2);
        Assert.assertTrue(bs.get(1));

        bs = ProtocolSwitch.toBitSet(4);
        Assert.assertTrue(bs.get(2));

        bs = ProtocolSwitch.toBitSet(8);
        Assert.assertTrue(bs.get(3));

        bs = ProtocolSwitch.toBitSet(3);
        Assert.assertTrue(bs.get(0));
        Assert.assertTrue(bs.get(1));

        bs = ProtocolSwitch.toBitSet(12);
        Assert.assertTrue(bs.get(2));
        Assert.assertTrue(bs.get(3));

        bs = ProtocolSwitch.toBitSet(64);
        Assert.assertTrue(bs.get(6));

        bs = ProtocolSwitch.toBitSet(127);
        for (int i = 0; i <= 6; ++i) {
            Assert.assertTrue(bs.get(i));
        }
    }

    @Test
    public void test_bitSetToByte() {
        BitSet bs = new BitSet();

        bs.set(0);
        Assert.assertEquals(1, ProtocolSwitch.toByte(bs));

        bs.clear();
        bs.set(1);
        Assert.assertEquals(2, ProtocolSwitch.toByte(bs));

        bs.clear();
        bs.set(2);
        Assert.assertEquals(4, ProtocolSwitch.toByte(bs));

        bs.clear();
        bs.set(3);
        Assert.assertEquals(8, ProtocolSwitch.toByte(bs));

        bs.clear();
        bs.set(0);
        bs.set(1);
        Assert.assertEquals(3, ProtocolSwitch.toByte(bs));

        bs.clear();
        bs.set(2);
        bs.set(3);
        Assert.assertEquals(12, ProtocolSwitch.toByte(bs));

        bs.clear();
        bs.set(6);
        Assert.assertEquals(64, ProtocolSwitch.toByte(bs));

        bs.clear();
        for (int i = 0; i <= 6; ++i) {
            bs.set(i);
        }
        Assert.assertEquals(127, ProtocolSwitch.toByte(bs));

        bs.clear();
        for (int i = 0; i <= 7; ++i) {
            bs.set(i);
        }
        try {
            ProtocolSwitch.toByte(bs);
            Assert.fail("Should not reach here!");
        } catch (IllegalArgumentException e) {
        }
    }
}