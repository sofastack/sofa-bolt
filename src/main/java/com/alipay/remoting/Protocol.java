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
 * A protocol contains a group of commands.
 * 
 * @author jiangping
 * @version $Id: Protocol.java, v 0.1 2015-12-11 PM5:02:48 tao Exp $
 */
public interface Protocol {
    /**
     * Get the newEncoder for the protocol.
     * 
     * @return
     */
    CommandEncoder getEncoder();

    /**
     * Get the decoder for the protocol.
     * 
     * @return
     */
    CommandDecoder getDecoder();

    /**
     * Get the heartbeat trigger for the protocol.
     * 
     * @return
     */
    HeartbeatTrigger getHeartbeatTrigger();

    /**
     * Get the command handler for the protocol.
     * 
     * @return
     */
    CommandHandler getCommandHandler();

    /**
     * Get the command factory for the protocol.
     * @return
     */
    CommandFactory getCommandFactory();
}
