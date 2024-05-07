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
package com.alipay.remoting.rpc.protocol;

import com.alipay.remoting.AsyncContext;
import com.alipay.remoting.BizContext;
import com.alipay.remoting.InvokeContext;
import com.alipay.remoting.LifeCycleException;
import com.alipay.remoting.RemotingContext;
import com.alipay.remoting.rpc.RpcCommandFactory;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * @author Even
 * @date 2024/4/29 11:20
 */
public class RpcCommandHandlerTest {

    private static RemotingContext remotingContext = null;

    private static final List<RemotingContext> remotingContextList = new ArrayList<>();

    private static final CountDownLatch countDownLatch = new CountDownLatch(2);

    @BeforeClass
    public static void beforeClass() {
        ConcurrentHashMap<String, UserProcessor<?>> userProcessors = new ConcurrentHashMap<>();
        userProcessors.put("testClass", new MockUserProcessors());
        remotingContext = new RemotingContext(null, new InvokeContext(),true, userProcessors);
    }

    @Test
    public void testHandleCommand() throws Exception {
        List<RpcRequestCommand> msg = new ArrayList<>();
        RpcRequestCommand rpcRequestCommand = new RpcRequestCommand();
        rpcRequestCommand.setTimeout(1000);
        rpcRequestCommand.setRequestClass("testClass");
        RpcRequestCommand rpcRequestCommand2 = new RpcRequestCommand();
        rpcRequestCommand2.setTimeout(2000);
        rpcRequestCommand2.setRequestClass("testClass");
        msg.add(rpcRequestCommand);
        msg.add(rpcRequestCommand2);
        RpcCommandHandler rpcCommandHandler = new RpcCommandHandler(new RpcCommandFactory());
        rpcCommandHandler.handleCommand(remotingContext, msg);
        countDownLatch.await(10, TimeUnit.SECONDS);
        Assert.assertTrue(remotingContextList.size() == 2);
        Assert.assertTrue(remotingContextList.get(0).getTimeout() != remotingContextList.get(1).getTimeout());
    }

    static class MockUserProcessors implements UserProcessor {

        @Override
        public void startup() throws LifeCycleException {

        }

        @Override
        public void shutdown() throws LifeCycleException {

        }

        @Override
        public boolean isStarted() {
            return false;
        }

        @Override
        public BizContext preHandleRequest(RemotingContext remotingCtx, Object request) {
            Assert.assertTrue(remotingCtx != remotingContext);
            remotingContextList.add(remotingCtx);
            countDownLatch.countDown();
            return null;
        }

        @Override
        public void handleRequest(BizContext bizCtx, AsyncContext asyncCtx, Object request) {

        }

        @Override
        public Object handleRequest(BizContext bizCtx, Object request) throws Exception {

            return null;
        }

        @Override
        public String interest() {
            return null;
        }

        @Override
        public Executor getExecutor() {
            return null;
        }

        @Override
        public ClassLoader getBizClassLoader() {
            return null;
        }

        @Override
        public boolean processInIOThread() {
            return false;
        }

        @Override
        public boolean timeoutDiscard() {
            return false;
        }

        @Override
        public void setExecutorSelector(ExecutorSelector executorSelector) {

        }

        @Override
        public ExecutorSelector getExecutorSelector() {
            return null;
        }
    }
}