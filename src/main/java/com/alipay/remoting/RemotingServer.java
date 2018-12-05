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

import java.util.concurrent.ExecutorService;

import com.alipay.remoting.rpc.protocol.UserProcessor;

/**
 * @author chengyi (mark.lx@antfin.com) 2018-06-16 06:55
 */
public interface RemotingServer {

    /**
     * init the server
     */
    @Deprecated
    void init();

    /**
     * Start the server.
     */
    boolean start();

    /**
     * Stop the server.
     *
     * Remoting server can not be used any more after stop.
     * If you need, you should destroy it, and instantiate another one.
     */
    boolean stop();

    /**
     * Get the ip of the server.
     *
     * @return ip
     */
    String ip();

    /**
     * Get the port of the server.
     *
     * @return listened port
     */
    int port();

    /**
     * Register processor for command with the command code.
     *
     * @param protocolCode protocol code
     * @param commandCode command code
     * @param processor processor
     */
    void registerProcessor(byte protocolCode, CommandCode commandCode,
                           RemotingProcessor<?> processor);

    /**
     * Register default executor service for server.
     *
     * @param protocolCode protocol code
     * @param executor the executor service for the protocol code
     */
    void registerDefaultExecutor(byte protocolCode, ExecutorService executor);

    /**
     * Register user processor.
     *
     * @param processor user processor which can be a single-interest processor or a multi-interest processor
     */
    void registerUserProcessor(UserProcessor<?> processor);

}
