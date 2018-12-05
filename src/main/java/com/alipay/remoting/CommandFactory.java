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

import java.net.InetSocketAddress;

/**
 * Command factory
 * 
 * @author xiaomin.cxm
 * @version $Id: CommandFactory.java, v 0.1 Mar 10, 2016 11:24:24 AM yunliang.shi Exp $
 */
public interface CommandFactory {
    // ~~~ create request command

    /**
     * create a request command with request object
     *
     * @param requestObject the request object included in request command
     * @param <T>
     * @return
     */
    <T extends RemotingCommand> T createRequestCommand(final Object requestObject);

    // ~~~ create response command

    /**
     * create a normal response with response object
     * @param responseObject
     * @param requestCmd
     * @param <T>
     * @return
     */
    <T extends RemotingCommand> T createResponse(final Object responseObject,
                                                 RemotingCommand requestCmd);

    <T extends RemotingCommand> T createExceptionResponse(int id, String errMsg);

    <T extends RemotingCommand> T createExceptionResponse(int id, final Throwable t, String errMsg);

    <T extends RemotingCommand> T createExceptionResponse(int id, ResponseStatus status);

    <T extends RemotingCommand> T createExceptionResponse(int id, ResponseStatus status,
                                                          final Throwable t);

    <T extends RemotingCommand> T createTimeoutResponse(final InetSocketAddress address);

    <T extends RemotingCommand> T createSendFailedResponse(final InetSocketAddress address,
                                                           Throwable throwable);

    <T extends RemotingCommand> T createConnectionClosedResponse(final InetSocketAddress address,
                                                                 String message);
}
