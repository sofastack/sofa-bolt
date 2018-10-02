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

import java.io.Serializable;

import com.alipay.remoting.config.switches.ProtocolSwitch;
import com.alipay.remoting.exception.DeserializationException;
import com.alipay.remoting.exception.SerializationException;

/**
 * Remoting command.
 * 
 * @author jiangping
 * @version $Id: RemotingCommand.java, v 0.1 2015-12-11 PM10:17:11 tao Exp $
 */
public interface RemotingCommand extends Serializable {
    /**
     * Get the code of the protocol that this command belongs to
     *
     * @return protocol code
     */
    ProtocolCode getProtocolCode();

    /**
     * Get the command code for this command
     *
     * @return command code
     */
    CommandCode getCmdCode();

    /**
     * Get the id of the command
     *
     * @return an int value represent the command id
     */
    int getId();

    /**
     * Get invoke context for this command
     *
     * @return context
     */
    InvokeContext getInvokeContext();

    /**
     * Get serializer type for this command
     *
     * @return
     */
    byte getSerializer();

    /**
     * Get the protocol switch status for this command
     *
     * @return
     */
    ProtocolSwitch getProtocolSwitch();

    /**
     * Serialize all parts of remoting command
     *
     * @throws SerializationException
     */
    void serialize() throws SerializationException;

    /**
     * Deserialize all parts of remoting command
     *
     * @throws DeserializationException
     */
    void deserialize() throws DeserializationException;

    /**
     * Serialize content of remoting command
     *
     * @param invokeContext
     * @throws SerializationException
     */
    void serializeContent(InvokeContext invokeContext) throws SerializationException;

    /**
     * Deserialize content of remoting command
     *
     * @param invokeContext
     * @throws DeserializationException
     */
    void deserializeContent(InvokeContext invokeContext) throws DeserializationException;
}
