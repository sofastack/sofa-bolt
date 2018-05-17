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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.alipay.remoting.util.StringUtils;

/**
 *
 * @author tsui
 * @version $Id: StringUtilsTest.java, v 0.1 2017-07-19 14:57 tsui Exp $
 */
public class StringUtilsTest {

    @Before
    public void init() {

    }

    @After
    public void stop() {
    }

    @Test
    public void testIsEmpty() {
        Assert.assertTrue(StringUtils.isEmpty(null));
        Assert.assertTrue(StringUtils.isEmpty(""));
        Assert.assertFalse(StringUtils.isEmpty(" "));
        Assert.assertFalse(StringUtils.isEmpty("bob"));
        Assert.assertFalse(StringUtils.isEmpty("  bob  "));

        Assert.assertFalse(StringUtils.isNotEmpty(null));
        Assert.assertFalse(StringUtils.isNotEmpty(""));
        Assert.assertTrue(StringUtils.isNotEmpty(" "));
        Assert.assertTrue(StringUtils.isNotEmpty("bob"));
        Assert.assertTrue(StringUtils.isNotEmpty("  bob  "));
    }

    @Test
    public void testIsBlank() {
        Assert.assertTrue(StringUtils.isBlank(null));
        Assert.assertTrue(StringUtils.isBlank(""));
        Assert.assertTrue(StringUtils.isBlank(" "));
        Assert.assertFalse(StringUtils.isBlank("bob"));
        Assert.assertFalse(StringUtils.isBlank("  bob  "));

        Assert.assertFalse(StringUtils.isNotBlank(null));
        Assert.assertFalse(StringUtils.isNotBlank(""));
        Assert.assertFalse(StringUtils.isNotBlank(" "));
        Assert.assertTrue(StringUtils.isNotBlank("bob"));
        Assert.assertTrue(StringUtils.isNotBlank("  bob  "));
    }

    @Test
    public void testIsNumeric() {
        Assert.assertTrue(StringUtils.isNumeric("11"));
        Assert.assertFalse(StringUtils.isNumeric("1a1"));
        Assert.assertFalse(StringUtils.isNumeric("aa"));
        Assert.assertTrue(StringUtils.isNumeric(""));
        Assert.assertFalse(StringUtils.isNumeric("  "));
        Assert.assertFalse(StringUtils.isNumeric(" a "));
        Assert.assertFalse(StringUtils.isNumeric("  123  "));
    }

    @Test
    public void testSplit() {
        String test = "127.0.0.1:12200?key1=value1&";
        String[] splitted = StringUtils.split(test, '?');
        Assert.assertEquals(splitted[0], "127.0.0.1:12200");
        Assert.assertEquals(splitted[1], "key1=value1&");
    }

    @Test
    public void testEquals() {
        String t = "hehe";
        String t1 = "hehe";
        String b = "hehehe";
        Assert.assertTrue(StringUtils.equals(t, t1));
        Assert.assertFalse(StringUtils.equals(t, b));
    }
}