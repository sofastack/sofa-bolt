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
package com.alipay.remoting.inner.connection;

import java.lang.ref.SoftReference;
import java.lang.reflect.Constructor;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alipay.remoting.Url;
import com.alipay.remoting.rpc.RpcAddressParser;

/**
 * Test basic usage of url
 * 
 * @author xiaomin.cxm
 * @version $Id: UrlTest.java, v 0.1 Apr 18, 2016 7:22:01 PM xiaomin.cxm Exp $
 */
public class UrlTest {
    private final static Logger logger = LoggerFactory.getLogger(UrlTest.class);

    @Test
    public void testUrlArgs() {
        Url url = new Url("127.0.0.1", 1111);
        try {
            url.setConnNum(-1);
        } catch (Exception e) {
            logger.error(e.getMessage());
            Assert.assertEquals(e.getClass().getName(), IllegalArgumentException.class.getName());
        }

        try {
            url.setConnectTimeout(-1);
        } catch (Exception e) {
            logger.error(e.getMessage());
            Assert.assertEquals(e.getClass().getName(), IllegalArgumentException.class.getName());
        }
    }

    @Test
    public void testUrlArgsEquals() {
        RpcAddressParser parser = new RpcAddressParser();

        String urlA = "localhost:3333?key1=value1";
        Url urlObjA = parser.parse(urlA);
        String urlB = "localhost:3333?key1=value1";
        Url urlObjB = parser.parse(urlB);
        String urlC = "localhost:3333?key1=value2";
        Url urlObjC = parser.parse(urlC);

        Assert.assertEquals(urlObjA, urlObjB);
        Assert.assertEquals(urlObjA.hashCode(), urlObjB.hashCode());
        Assert.assertFalse(urlObjA.equals(urlObjC));
        Assert.assertFalse(urlObjA.hashCode() == urlObjC.hashCode());
    }

    @Test
    public void testGC() throws Exception {
        String url = "localhost:3333?k1=v1&k2=v2";

        Constructor con = Url.class.getDeclaredConstructor(new Class[] { String.class });
        con.setAccessible(true);

        long start = System.currentTimeMillis();
        long MAX_TIME_ELAPSED = 10 * 1000;
        Url urlObject = null;
        while (!Url.isCollected) {
            urlObject = (Url) con.newInstance((String) url);
            Url.parsedUrls.put(url, new SoftReference<Url>(urlObject));

            if ((System.currentTimeMillis() - start) > MAX_TIME_ELAPSED) {
                Assert.fail("GC should have already been called!");
                break;
            }
        }
    }

    /**
     * -server -Xms20m -Xmx20m -Xmn3m -Xss256k -XX:PermSize=20m -XX:MaxPermSize=20m
     * -XX:+UseConcMarkSweepGC -XX:+UseParNewGC -XX:+CMSClassUnloadingEnabled
     * -XX:+DisableExplicitGC -XX:+UseCMSInitiatingOccupancyOnly
     * -XX:CMSInitiatingOccupancyFraction=68 -verbose:gc -XX:+PrintGCDetails
     * -XX:+PrintGCDateStamps -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/Users/tsui/logs/
     *
     * @param args
     */
    public static void main(String[] args) {
        //        ConcurrentHashMap<String, SoftReference<String>> parsedUrls = new ConcurrentHashMap<String, SoftReference<String>>();
        //        int MAX = Integer.valueOf(args[0]);
        //        for (int i = 0; i < MAX; i++) {
        //            parsedUrls.put("test" + i, new SoftReference<String>(i + "hehe"));
        //        }
        //        Collection<SoftReference<String>> urls = parsedUrls.values();
        //        for (SoftReference<String> url : urls) {
        //            System.out.println(url.get());
        //        }
    }
}
