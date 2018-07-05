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
package com.alipay.remoting.rpc.userprocessor.multiinterestprocessor;

import java.io.Serializable;

/**
 * @antuor muyun.cyt (muyun.cyt@antfin.com)  2018/7/5   11:20 AM
 */
public interface MultiInterestBaseRequestBody extends Serializable {
    /**
     * Getter method for property <tt>id</tt>.
     *
     * @return property value of id
     */
    int getId();

    /**
     * Setter method for property <tt>id</tt>.
     *
     * @param id value to be assigned to property id
     */
    void setId(int id);

    /**
     * Getter method for property <tt>msg</tt>.
     *
     * @return property value of msg
     */
    String getMsg();

    /**
     * Setter method for property <tt>msg</tt>.
     *
     * @param msg value to be assigned to property msg
     */
    void setMsg(String msg);
}
