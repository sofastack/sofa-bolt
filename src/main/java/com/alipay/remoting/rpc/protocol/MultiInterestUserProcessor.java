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

import java.util.List;

/**
 * Support multi-interests feature based on UserProcessor
 *
 * The implementations of this interface don't need to implement the {@link com.alipay.remoting.rpc.protocol.UserProcessor#interest() interest()} method;
 * @author muyun.cyt (muyun.cyt@antfin.com)  2018/7/5   11:19 AM
 */
public interface MultiInterestUserProcessor<T> extends UserProcessor<T> {

    /**
     * A list of the class names of user request.
     * Use String type to avoid classloader problem.
     */
    List<String> multiInterest();

}
