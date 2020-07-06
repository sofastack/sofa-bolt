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
 * Remoting address parser
 * 
 * Implement this to generate a {@link Url}
 * 
 * @author xiaomin.cxm
 * @version $Id: RemotingAddressParser.java, v 0.1 Mar 11, 2016 5:56:55 PM xiaomin.cxm Exp $
 */
public interface RemotingAddressParser {
    /**
     * Parse a simple string url to get {@link Url}
     * 
     * @param url
     * @return parsed {@link Url}
     */
    Url parse(String url);

    /**
     * Parse a simple string url to get a unique key of a certain address
     * 
     * @param url
     * @return
     */
    String parseUniqueKey(String url);

    /**
     * Parse to get property value according to specified property key
     * 
     * @param url
     * @param propKey
     * @return propValue
     */
    String parseProperty(String url, String propKey);

    /**
     * Initialize {@link Url} arguments
     * 
     * @param url
     */
    void initUrlArgs(Url url);

    /** symbol : */
    char COLON = ':';

    /** symbol = */
    char EQUAL = '=';

    /** symbol & */
    char AND   = '&';

    /** symbol ? */
    char QUES  = '?';
}
