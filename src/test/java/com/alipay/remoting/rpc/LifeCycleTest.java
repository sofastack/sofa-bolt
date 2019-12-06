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

    @Test
    public void testAvailabilityCheck() {
        RpcServer server = new RpcServer(9999);
        RpcClient client = new RpcClient();
        try {
            server.invokeSync("127.0.0.1:9999", null, 3000);
            Assert.fail();
        } catch (LifeCycleException e) {
            e.printStackTrace();
        } catch (Exception e) {
            Assert.fail();
        }

        try {
            client.invokeSync("127.0.0.1:9999", null, 3000);
            Assert.fail();
        } catch (LifeCycleException e) {
            e.printStackTrace();
        } catch (Exception e) {
            Assert.fail();
        }
    }
}
