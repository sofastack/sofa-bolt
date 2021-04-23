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

import com.alipay.remoting.LifeCycleException;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author muyun
 * @version $Id: LifeCycleTest.java, v 0.1 2019年12月02日 10:25 AM muyun Exp $
 */
public class LifeCycleTest {

    private RpcServer server = new RpcServer(9999, true);
    private RpcClient client = new RpcClient();

    @Test
    public void testAvailabilityCheck() {
        Assert.assertTrue(testFunctionAvailable(false));
        server.startup();
        client.startup();
        Assert.assertTrue(testFunctionAvailable(true));
        server.shutdown();
        client.shutdown();
        Assert.assertTrue(testFunctionAvailable(false));
    }

    private boolean testFunctionAvailable(boolean expectedResult) {
        try {
            server.isConnected("127.0.0.1:9999");
            if (!expectedResult) {
                return false;
            }
        } catch (LifeCycleException e) {
            if (expectedResult) {
                return false;
            }
        }

        try {
            client.getConnection("127.0.0.1:9999", 1000);
            if (!expectedResult) {
                return false;
            }
        } catch (LifeCycleException e) {
            if (expectedResult) {
                return false;
            }
        } catch (Exception e) {
            if (expectedResult) {
                return false;
            }
        }

        return true;
    }
}
