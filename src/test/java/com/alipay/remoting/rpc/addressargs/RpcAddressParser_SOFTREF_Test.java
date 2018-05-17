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
package com.alipay.remoting.rpc.addressargs;

import org.junit.Assert;

import com.alipay.remoting.Url;
import com.alipay.remoting.exception.RemotingException;
import com.alipay.remoting.rpc.RpcAddressParser;

/**
 * rpc address parser
 * test soft reference
 * 
 * @author xiaomin.cxm
 * @version $Id: RpcAddressParser_SOFTREF_Test.java, v 0.1 Apr 6, 2016 10:45:13 AM xiaomin.cxm Exp $
 */
public class RpcAddressParser_SOFTREF_Test {

    //    @Test
    public void testParserNonProtocol() throws RemotingException {
        String url = "127.0.0.1:1111?_TIMEOUT=3000&_SERIALIZETYPE=hessian2";
        RpcAddressParser parser = new RpcAddressParser();
        int MAX = 1000000;

        printMemory();
        long start1 = System.currentTimeMillis();
        for (int i = 0; i < MAX; ++i) {
            Url btUrl = parser.parse(url);
            Assert.assertEquals(btUrl.getUniqueKey(), "127.0.0.1:1111");
        }
        long end1 = System.currentTimeMillis();
        long time1 = end1 - start1;
        System.out.println("time1:" + time1);
        printMemory();
    }

    private void printMemory() {
        int mb = 1024 * 1024;
        Runtime rt = Runtime.getRuntime();
        long total = rt.totalMemory();
        long max = rt.maxMemory();
        long free = rt.freeMemory();
        System.out.print("total[" + total / mb + "mb] ");
        System.out.print("max[" + max / mb + "mb] ");
        System.out.println("free[" + free / mb + "mb]");
    }
}
