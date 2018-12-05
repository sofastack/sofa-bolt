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

/**
 * Implements common function and provide default value.
 * more details in {@link com.alipay.remoting.rpc.protocol.AbstractUserProcessor}
 * @author muyun.cyt (muyun.cyt@antfin.com)  2018/7/5   11:17 AM
 */
public abstract class AbstractMultiInterestUserProcessor<T> extends AbstractUserProcessor<T>
                                                                                            implements
                                                                                            MultiInterestUserProcessor<T> {

    /**
     * do not need to implement this method because of the multiple interests
     * @see com.alipay.remoting.rpc.protocol.UserProcessor#interest()
     * */
    @Override
    public String interest() {
        return null;
    }

}
