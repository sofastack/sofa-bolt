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

import com.alipay.remoting.exception.CodecException;
import com.alipay.remoting.exception.DeserializationException;
import com.alipay.remoting.exception.SerializationException;
import com.alipay.remoting.rpc.RequestCommand;
import com.alipay.remoting.rpc.ResponseCommand;

/**
 * Define custom serializers for command header and content.
 * 
 * @author jiangping
 * @version $Id: CustomSerializer.java, v 0.1 2015-10-7 AM11:37:36 tao Exp $
 */
public interface CustomSerializer {
    /**
     * Serialize the header of RequestCommand.
     * 
     * @param request
     * @param invokeContext
     * @return
     * @throws CodecException
     */
    <T extends RequestCommand> boolean serializeHeader(T request, InvokeContext invokeContext)
                                                                                              throws SerializationException;

    /**
     * Serialize the header of ResponseCommand.
     *
     * @param response
     * @return
     * @throws CodecException
     */
    <T extends ResponseCommand> boolean serializeHeader(T response) throws SerializationException;

    /**
     * Deserialize the header of RequestCommand.
     *
     * @param request
     * @return
     * @throws CodecException
     */
    <T extends RequestCommand> boolean deserializeHeader(T request) throws DeserializationException;

    /**
     * Deserialize the header of ResponseCommand.
     * 
     * @param response
     * @param invokeContext
     * @return
     * @throws CodecException
     */
    <T extends ResponseCommand> boolean deserializeHeader(T response, InvokeContext invokeContext)
                                                                                                  throws DeserializationException;

    /**
     * Serialize the content of RequestCommand.
     *
     * @param request
     * @param invokeContext
     * @return
     * @throws CodecException
     */
    <T extends RequestCommand> boolean serializeContent(T request, InvokeContext invokeContext)
                                                                                               throws SerializationException;

    /**
     * Serialize the content of ResponseCommand.
     * 
     * @param response
     * @return
     * @throws CodecException
     */
    <T extends ResponseCommand> boolean serializeContent(T response) throws SerializationException;

    /**
     * Deserialize the content of RequestCommand.
     * 
     * @param request
     * @return
     * @throws CodecException
     */
    <T extends RequestCommand> boolean deserializeContent(T request)
                                                                    throws DeserializationException;

    /**
     * Deserialize the content of ResponseCommand.
     * 
     * @param response
     * @param invokeContext
     * @return
     * @throws CodecException
     */
    <T extends ResponseCommand> boolean deserializeContent(T response, InvokeContext invokeContext)
                                                                                                   throws DeserializationException;
}