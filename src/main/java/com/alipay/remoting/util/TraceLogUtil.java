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
package com.alipay.remoting.util;

import org.slf4j.Logger;

import com.alipay.remoting.InvokeContext;

/** 
 * Trace log util
 *
 * @author tsui 
 * @version $Id: TraceLogUtil.java, v 0.1 2016-08-02 17:31 tsui Exp $
 */
public class TraceLogUtil {
    /**
     * print trace log
     * @param traceId
     * @param invokeContext
     */
    public static void printConnectionTraceLog(Logger logger, String traceId,
                                               InvokeContext invokeContext) {
        String sourceIp = invokeContext.get(InvokeContext.CLIENT_LOCAL_IP);
        Integer sourcePort = invokeContext.get(InvokeContext.CLIENT_LOCAL_PORT);
        String targetIp = invokeContext.get(InvokeContext.CLIENT_REMOTE_IP);
        Integer targetPort = invokeContext.get(InvokeContext.CLIENT_REMOTE_PORT);
        StringBuilder logMsg = new StringBuilder();
        logMsg.append(traceId).append(",");
        logMsg.append(sourceIp).append(",");
        logMsg.append(sourcePort).append(",");
        logMsg.append(targetIp).append(",");
        logMsg.append(targetPort);
        if (logger.isInfoEnabled()) {
            logger.info(logMsg.toString());
        }
    }
}