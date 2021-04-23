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

/**
 * Command handler.
 * 
 * @author jiangping
 * @version $Id: CommandHandler.java, v 0.1 2015-12-14 PM4:03:55 tao Exp $
 */
public interface CommandHandler {
    /**
     * Handle the command.
     * 
     * @param ctx
     * @param msg
     * @throws Exception
     */
    void handleCommand(RemotingContext ctx, Object msg) throws Exception;

    /**
     * Register processor for command with specified code.
     * 
     * @param cmd
     * @param processor
     */
    void registerProcessor(CommandCode cmd, RemotingProcessor<?> processor);

    /**
     * Register default executor for the handler.
     * 
     * @param executor
     */
    void registerDefaultExecutor(ExecutorService executor);

    /**
     * Get default executor for the handler.
     */
    ExecutorService getDefaultExecutor();

}
