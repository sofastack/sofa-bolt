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

/**
 * basic info for biz
 * 
 * @author xiaomin.cxm
 * @version $Id: BizContext.java, v 0.1 Jan 6, 2016 10:35:04 PM xiaomin.cxm Exp $
 */
public interface BizContext {
    /**
     * get remote address
     * 
     * @return remote address
     */
    String getRemoteAddress();

    /**
     * get remote host ip
     * 
     * @return remote host
     */
    String getRemoteHost();

    /**
     * get remote port
     * 
     * @return remote port
     */
    int getRemotePort();

    /**
     * get the connection of this request
     *
     * @return connection
     */
    Connection getConnection();

    /**
     * check whether request already timeout
     *
     * @return true if already timeout, you can log some useful info and then discard this request.
     */
    boolean isRequestTimeout();

    /**
     * get the timeout value from rpc client.
     *
     * @return client timeout
     */
    int getClientTimeout();

    /**
     * get the arrive time stamp
     *
     * @return the arrive time stamp
     */
    long getArriveTimestamp();

    /**
     * put a key and value
     */
    void put(String key, String value);

    /**
     * get value
     * 
     * @param key target key
     * @return value
     */
    String get(String key);

    /**
     * get invoke context.
     *
     * @return InvokeContext
     */
    InvokeContext getInvokeContext();
}