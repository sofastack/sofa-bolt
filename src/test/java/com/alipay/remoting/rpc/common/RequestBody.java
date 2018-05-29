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
package com.alipay.remoting.rpc.common;

import java.io.Serializable;
import java.util.Random;

/**
 * biz request as a demo
 * 
 * @author jiangping
 * @version $Id: RequestBody.java, v 0.1 2015-10-19 PM2:32:26 tao Exp $
 */
public class RequestBody implements Serializable {

    /** for serialization */
    private static final long  serialVersionUID          = -1288207208017808618L;

    public static final String DEFAULT_CLIENT_STR        = "HELLO WORLD! I'm from client";
    public static final String DEFAULT_SERVER_STR        = "HELLO WORLD! I'm from server";
    public static final String DEFAULT_SERVER_RETURN_STR = "HELLO WORLD! I'm server return";
    public static final String DEFAULT_CLIENT_RETURN_STR = "HELLO WORLD! I'm client return";

    public static final String DEFAULT_ONEWAY_STR        = "HELLO WORLD! I'm oneway req";
    public static final String DEFAULT_SYNC_STR          = "HELLO WORLD! I'm sync req";
    public static final String DEFAULT_FUTURE_STR        = "HELLO WORLD! I'm future req";
    public static final String DEFAULT_CALLBACK_STR      = "HELLO WORLD! I'm call back req";

    /** id */
    private int                id;

    /** msg */
    private String             msg;

    /** body */
    private byte[]             body;

    private Random             r                         = new Random();

    public RequestBody() {
        //json serializer need default constructor
    }

    public RequestBody(int id, String msg) {
        this.id = id;
        this.msg = msg;
    }

    public RequestBody(int id, int size) {
        this.id = id;
        this.msg = "";
        this.body = new byte[size];
        r.nextBytes(this.body);
    }

    /**
     * Getter method for property <tt>id</tt>.
     * 
     * @return property value of id
     */
    public int getId() {
        return id;
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
        return msg;
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
        return "Body[this.id = " + id + ", this.msg = " + msg + "]";
    }

    static public enum InvokeType {
        ONEWAY, SYNC, FUTURE, CALLBACK;
    }
}