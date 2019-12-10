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
package com.alipay.remoting.rpc.userprocessor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.alipay.remoting.BizContext;
import com.alipay.remoting.rpc.common.SimpleServerUserProcessor;
import com.alipay.remoting.rpc.protocol.MultiInterestUserProcessor;
import com.alipay.remoting.rpc.protocol.SyncMutiInterestUserProcessor;
import com.alipay.remoting.rpc.protocol.SyncUserProcessor;
import com.alipay.remoting.rpc.protocol.UserProcessor;
import com.alipay.remoting.rpc.protocol.UserProcessorRegisterHelper;
import com.alipay.remoting.rpc.userprocessor.multiinterestprocessor.SimpleServerMultiInterestUserProcessor;

/**
 * @antuor muyun.cyt (muyun.cyt@antfin.com)  2018/7/5   4:51 PM
 */
public class UserProcessorRegisterHelperTest {

    ConcurrentHashMap<String, UserProcessor<?>> userProcessors;

    @Before
    public void init() {
        userProcessors = new ConcurrentHashMap<String, UserProcessor<?>>();
    }

    @Test
    public void testRegisterUserProcessor() {
        UserProcessor userProcessor = new SimpleServerUserProcessor();
        UserProcessorRegisterHelper.registerUserProcessor(userProcessor, userProcessors);
        Assert.assertEquals(1, userProcessors.size());
    }

    @Test
    public void testRegisterMultiInterestUserProcessor() {
        UserProcessor multiInterestUserProcessor = new SimpleServerMultiInterestUserProcessor();
        UserProcessorRegisterHelper.registerUserProcessor(multiInterestUserProcessor,
            userProcessors);
        Assert.assertEquals(((SimpleServerMultiInterestUserProcessor) multiInterestUserProcessor)
            .multiInterest().size(), userProcessors.size());
    }

    @Test
    public void testInterestNullException() {
        UserProcessor userProcessor = new SyncUserProcessor() {
            @Override
            public Object handleRequest(BizContext bizCtx, Object request) throws Exception {
                return request;
            }

            @Override
            public String interest() {
                return null;
            }
        };

        try {
            UserProcessorRegisterHelper.registerUserProcessor(userProcessor, userProcessors);
        } catch (RuntimeException e) {
        }

        Assert.assertEquals(0, userProcessors.size());
    }

    @Test
    public void testInterestEmptyException() {
        MultiInterestUserProcessor userProcessor = new SyncMutiInterestUserProcessor() {
            @Override
            public Object handleRequest(BizContext bizCtx, Object request) throws Exception {
                return request;
            }

            @Override
            public List<String> multiInterest() {
                return new ArrayList<String>();
            }

        };

        try {
            UserProcessorRegisterHelper.registerUserProcessor(userProcessor, userProcessors);
        } catch (RuntimeException e) {
        }

        Assert.assertEquals(0, userProcessors.size());
    }

    @Test
    public void testInterestRepeatException() {
        UserProcessor userProcessor = new SimpleServerUserProcessor();
        UserProcessor repeatedUserProcessor = new SimpleServerUserProcessor();
        try {
            UserProcessorRegisterHelper.registerUserProcessor(userProcessor, userProcessors);
            UserProcessorRegisterHelper
                .registerUserProcessor(repeatedUserProcessor, userProcessors);
        } catch (RuntimeException e) {
        }

        Assert.assertEquals(1, userProcessors.size());
    }

}
