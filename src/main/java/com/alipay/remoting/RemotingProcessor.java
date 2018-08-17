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
 * Remoting processor processes remoting commands.
 * 
 * @author jiangping
 * @version $Id: RemotingProcessor.java, v 0.1 Dec 22, 2015 11:48:43 AM tao Exp $
 */
public interface RemotingProcessor<T extends RemotingCommand> {

    /**
     * Process the remoting command.
     * 
     * @param ctx
     * @param msg
     * @param defaultExecutor
     * @throws Exception
     */
    void process(RemotingContext ctx, T msg, ExecutorService defaultExecutor) throws Exception;

    /**
     * Get the executor.
     * 
     * @return
     */
    ExecutorService getExecutor();

    /**
     * Set executor.
     * 
     * @param executor
     */
    void setExecutor(ExecutorService executor);

}
