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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * test rpc server and stop logic
 *
 * @author tsui  
 * @version $Id: RpcServerTest.java, v 0.1 2018-05-29 15:27 tsui Exp $$ 
 */
public class RpcServerTest {
    static Logger logger = LoggerFactory.getLogger(RpcServerTest.class);

    @Before
    public void init() {
    }

    @After
    public void stop() {
    }

    @Test
    public void doTestStartAndStop() {
        doTestStartAndStop(true);
        doTestStartAndStop(false);
    }

    private void doTestStartAndStop(boolean syncStop) {
        // 1. start a rpc server successfully
        RpcServer rpcServer1 = new RpcServer(1111, false, syncStop);
        try {
            rpcServer1.start();
        } catch (Exception e) {
            logger.warn("start fail");
            Assert.fail("Should not reach here");
        }

        logger.warn("start success");
        // 2. start a rpc server with the same port number failed
        RpcServer rpcServer2 = new RpcServer(1111, false, syncStop);
        try {
            rpcServer2.start();
            Assert.fail("Should not reach here");
            logger.warn("start success");
        } catch (Exception e) {
            logger.warn("start fail");
        }

        // 3. stop the first rpc server successfully
        try {
            rpcServer1.stop();
        } catch (IllegalStateException e) {
            Assert.fail("Should not reach here");
        }

        // 4. stop the second rpc server failed, for if start failed, stop method will be called automatically
        try {
            rpcServer2.stop();
            Assert.fail("Should not reach here");
        } catch (IllegalStateException e) {
            // expect
        }
    }
}