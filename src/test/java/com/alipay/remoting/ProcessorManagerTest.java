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

import org.junit.Assert;
import org.junit.Test;

import com.alipay.remoting.rpc.protocol.RpcCommandCode;
import com.alipay.remoting.rpc.protocol.RpcRequestProcessor;

/**
 * test processor manager
 *
 * @author tsui
 * @version $Id: ProcessorManagerTest.java, v 0.1 2018-07-06 12:19 tsui Exp $$ 
 */
public class ProcessorManagerTest {

    /**
     * test it should be override if register twice for the same command code
     */
    @Test
    public void testRegisterProcessor() {
        ProcessorManager processorManager = new ProcessorManager();
        CommandCode cmd1 = RpcCommandCode.RPC_REQUEST;
        CommandCode cmd2 = RpcCommandCode.RPC_REQUEST;
        RpcRequestProcessor rpcRequestProcessor1 = new RpcRequestProcessor();
        RpcRequestProcessor rpcRequestProcessor2 = new RpcRequestProcessor();
        processorManager.registerProcessor(cmd1, rpcRequestProcessor1);
        processorManager.registerProcessor(cmd2, rpcRequestProcessor2);
        Assert.assertEquals(processorManager.getProcessor(cmd1), rpcRequestProcessor2);
        Assert.assertEquals(processorManager.getProcessor(cmd2), rpcRequestProcessor2);
    }
}