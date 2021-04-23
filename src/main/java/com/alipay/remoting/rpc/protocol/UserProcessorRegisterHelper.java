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

import java.util.concurrent.ConcurrentHashMap;

import com.alipay.remoting.util.StringUtils;

/**
 * @author muyun.cyt (muyun.cyt@antfin.com)  2018/7/5   11:22 AM
 */
public class UserProcessorRegisterHelper {

    /**
     * Help register single-interest user processor.
     *
     * @param processor  the processor need to be registered
     * @param userProcessors   the map of user processors
     */
    public static void registerUserProcessor(UserProcessor<?> processor,
                                             ConcurrentHashMap<String, UserProcessor<?>> userProcessors) {
        if (null == processor) {
            throw new RuntimeException("User processor should not be null!");
        }
        if (processor instanceof MultiInterestUserProcessor) {
            registerUserProcessor((MultiInterestUserProcessor) processor, userProcessors);
        } else {
            if (StringUtils.isBlank(processor.interest())) {
                throw new RuntimeException("Processor interest should not be blank!");
            }
            UserProcessor<?> preProcessor = userProcessors.putIfAbsent(processor.interest(),
                processor);
            if (preProcessor != null) {
                String errMsg = "Processor with interest key ["
                                + processor.interest()
                                + "] has already been registered to rpc server, can not register again!";
                throw new RuntimeException(errMsg);
            }
        }

    }

    /**
     * Help register multi-interest user processor.
     *
     * @param processor  the processor with multi-interest need to be registered
     * @param userProcessors    the map of user processors
     */
    private static void registerUserProcessor(MultiInterestUserProcessor<?> processor,
                                              ConcurrentHashMap<String, UserProcessor<?>> userProcessors) {
        if (null == processor.multiInterest() || processor.multiInterest().isEmpty()) {
            throw new RuntimeException("Processor interest should not be blank!");
        }
        for (String interest : processor.multiInterest()) {
            UserProcessor<?> preProcessor = userProcessors.putIfAbsent(interest, processor);
            if (preProcessor != null) {
                String errMsg = "Processor with interest key ["
                                + interest
                                + "] has already been registered to rpc server, can not register again!";
                throw new RuntimeException(errMsg);
            }
        }

    }
}
