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

import java.util.concurrent.ThreadLocalRandom;

/**
 * @antuor muyun.cyt (muyun.cyt@antfin.com)  2018/7/5   11:20 AM
 */
public class RequestBodyC2 implements MultiInterestBaseRequestBody {

    private static final long  serialVersionUID          = -7512540836127308096L;

    public static final String DEFAULT_CLIENT_STR        = "HELLO WORLD! I'm from client--C2";
    public static final String DEFAULT_SERVER_STR        = "HELLO WORLD! I'm from server--C2";
    public static final String DEFAULT_SERVER_RETURN_STR = "HELLO WORLD! I'm server return--C2";
    public static final String DEFAULT_CLIENT_RETURN_STR = "HELLO WORLD! I'm client return--C2";

    public static final String DEFAULT_ONEWAY_STR        = "HELLO WORLD! I'm oneway req--C2";
    public static final String DEFAULT_SYNC_STR          = "HELLO WORLD! I'm sync req--C2";
    public static final String DEFAULT_FUTURE_STR        = "HELLO WORLD! I'm future req--C2";
    public static final String DEFAULT_CALLBACK_STR      = "HELLO WORLD! I'm call back req--C2";

    /** id */
    private int                id;

    /** msg */
    private String             msg;

    /** body */
    private byte[]             body;

    public RequestBodyC2() {
        //json serializer need default constructor
        //super();
    }

    public RequestBodyC2(int id, String msg) {
        //super(id, msg);
        this.id = id;
        this.msg = msg;
    }

    public RequestBodyC2(int id, int size) {
        // super(id, size);
        this.id = id;
        this.msg = "";
        this.body = new byte[size];
        ThreadLocalRandom.current().nextBytes(this.body);
    }

    /**
     * Getter method for property <tt>id</tt>.
     *
     * @return property value of id
     */
    public int getId() {
        return this.id;
    }

    /**
     * Setter method for property <tt>id</tt>.
     *
     * @param id value to be assigned to property id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Getter method for property <tt>msg</tt>.
     *
     * @return property value of msg
     */
    public String getMsg() {
        return this.msg;
    }

    /**
     * Setter method for property <tt>msg</tt>.
     *
     * @param msg value to be assigned to property msg
     */
    public void setMsg(String msg) {
        this.msg = msg;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Body[this.id = " + this.id + ", this.msg = " + this.msg + "]";
    }
}
