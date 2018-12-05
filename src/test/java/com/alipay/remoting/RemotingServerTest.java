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

import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.alipay.remoting.rpc.RpcServer;

/**
 * test {@link AbstractRemotingServer} apis
 *
 * @author tsui
 * @version $Id: RemotingServerTest.java, v 0.1 May 16, 2018 10:00:48 AM tsui Exp $
 */
public class RemotingServerTest {
    @Test
    public void testStartRepeatedly() {
        RpcServer rpcServer = new RpcServer(1111);
        rpcServer.start();

        try {
            rpcServer.start();
            Assert.fail("Should not reach here!");
        } catch (Exception e) {
            // expect IllegalStateException
        }
        rpcServer.stop();
    }

    @Test
    public void testStartFailed() throws InterruptedException {
        AbstractRemotingServer remotingServer = Mockito.mock(AbstractRemotingServer.class);
        when(remotingServer.doStart()).thenThrow(new RuntimeException("start error"));

        Assert.assertFalse(remotingServer.start());
    }

    @Test
    public void testStopRepeatedly() {
        RpcServer rpcServer = new RpcServer(1111);
        try {
            rpcServer.start();
        } catch (Exception e) {
            Assert.fail("Should not reach here!");
            e.printStackTrace();
        }
        rpcServer.stop();
        try {
            rpcServer.stop();
            Assert.fail("Should not reach here!");
        } catch (Exception e) {
            // expect IllegalStateException
        }
    }
}
