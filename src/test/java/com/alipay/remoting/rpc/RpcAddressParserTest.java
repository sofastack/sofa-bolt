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
package com.alipay.remoting.rpc;

import com.alipay.remoting.Url;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class RpcAddressParserTest {
    private RpcAddressParser parser = new RpcAddressParser();

    @Test
    public void parseNormal() {
        //normal ipv4 url
        Url url = parser.parse("127.0.0.1:12200");
        Assert.assertEquals("127.0.0.1", url.getIp());
        Assert.assertEquals(12200, url.getPort());
        url = parser.parse("127.0.0.1:12200?KEY1=VALUE1&KEY2=VALUE2");
        Assert.assertEquals("127.0.0.1", url.getIp());
        Assert.assertEquals(12200, url.getPort());
        Assert.assertEquals(url.getProperties().size(), 2);

        //normal ipv6 url
        url = parser.parse("0:0:0:0:0:0:0:1:12200");
        Assert.assertEquals("0:0:0:0:0:0:0:1", url.getIp());
        Assert.assertEquals(12200, url.getPort());
        url = parser.parse("0:0:0:0:0:0:0:1:12200?KEY1=VALUE1&KEY2=VALUE2");
        Assert.assertEquals("0:0:0:0:0:0:0:1", url.getIp());
        Assert.assertEquals(12200, url.getPort());
        Assert.assertEquals(url.getProperties().size(), 2);
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void parseAbnormal_Blank() {
        String urlStr = "";
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Illegal format address string [" + urlStr
                             + "], should not be blank! ");
        parser.parse(urlStr);
    }

    @Test
    public void parseAbnormal_NO_COLON() {
        String urlStr = "127.0.0.1";
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Illegal format address string [" + urlStr
                             + "], must have one COLON[:]! ");
        parser.parse(urlStr);
    }

    @Test
    public void parseAbnormal_ENDWITH_COLON() {
        String urlStr = "127.0.0.1:";
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Illegal format address string [" + urlStr
                             + "], should not end with COLON[:]! ");
        parser.parse(urlStr);
    }

    @Test
    public void parseAbnormal_ENDWITH_QUES() {
        String urlStr = "127.0.0.1:12200?";
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Illegal format address string [" + urlStr
                             + "], should not end with QUES[?]! ");
        parser.parse(urlStr);
    }

    @Test
    public void parseAbnormal_ENDWITH_EQUAL() {
        String urlStr = "127.0.0.1:12200?key1=";
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Illegal format address string [" + urlStr
                             + "], should not end with EQUAL[=]! ");
        parser.parse(urlStr);
    }

    @Test
    public void parseAbnormal_NO_EQUAL() {
        String urlStr = "127.0.0.1:12200?key1";
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Illegal format address string [" + urlStr
                             + "], must have one EQUAL[=]! ");
        parser.parse(urlStr);
    }

    @Test
    public void parseAbnormal_ENDWITH_AND() {
        String urlStr = "127.0.0.1:12200?key1=value1&";
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Illegal format address string [" + urlStr
                             + "], should not end with AND[&]! ");
        parser.parse(urlStr);
    }
}