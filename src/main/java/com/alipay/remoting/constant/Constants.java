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
package com.alipay.remoting.constant;

/**
 * Bolt Constants.
 *
 * @author chengyi (mark.lx@antfin.com) 2019-03-06 15:19
 */
public class Constants {

    /**
     * default expire time to remove connection pool, time unit: milliseconds
     */
    public static final int    DEFAULT_EXPIRE_TIME = 10 * 60 * 1000;

    /**
     * default retry times when failed to get result of FutureTask
     */
    public static final int    DEFAULT_RETRY_TIMES = 2;

    public static final String SSL_HANDLER         = "sslHandler";

}
