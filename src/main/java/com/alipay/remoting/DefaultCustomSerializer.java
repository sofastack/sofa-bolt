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

import com.alipay.remoting.exception.DeserializationException;
import com.alipay.remoting.exception.SerializationException;
import com.alipay.remoting.rpc.RequestCommand;
import com.alipay.remoting.rpc.ResponseCommand;

/**
 * The default custom serializer, which does nothing. 
 * Extend this class and override the methods you want to custom.
 * 
 * @author jiangping
 * @version $Id: DefaultCustomSerializer.java, v 0.1 2015-10-8 AM11:09:49 tao Exp $
 */
public class DefaultCustomSerializer implements CustomSerializer {

    /** 
     * @see com.alipay.remoting.CustomSerializer#serializeHeader(com.alipay.remoting.rpc.RequestCommand, InvokeContext)
     */
    @Override
    public <T extends RequestCommand> boolean serializeHeader(T request, InvokeContext invokeContext)
                                                                                                     throws SerializationException {
        return false;
    }

    /** 
     * @see com.alipay.remoting.CustomSerializer#serializeHeader(com.alipay.remoting.rpc.ResponseCommand)
     */
    @Override
    public <T extends ResponseCommand> boolean serializeHeader(T response)
                                                                          throws SerializationException {
        return false;
    }

    /** 
     * @see com.alipay.remoting.CustomSerializer#deserializeHeader(com.alipay.remoting.rpc.RequestCommand)
     */
    @Override
    public <T extends RequestCommand> boolean deserializeHeader(T request)
                                                                          throws DeserializationException {
        return false;
    }

    /** 
     * @see com.alipay.remoting.CustomSerializer#deserializeHeader(com.alipay.remoting.rpc.ResponseCommand, InvokeContext)
     */
    @Override
    public <T extends ResponseCommand> boolean deserializeHeader(T response,
                                                                 InvokeContext invokeContext)
                                                                                             throws DeserializationException {
        return false;
    }

    /** 
     * @see com.alipay.remoting.CustomSerializer#serializeContent(com.alipay.remoting.rpc.RequestCommand, InvokeContext)
     */
    @Override
    public <T extends RequestCommand> boolean serializeContent(T request,
                                                               InvokeContext invokeContext)
                                                                                           throws SerializationException {
        return false;
    }

    /** 
     * @see com.alipay.remoting.CustomSerializer#serializeContent(com.alipay.remoting.rpc.ResponseCommand)
     */
    @Override
    public <T extends ResponseCommand> boolean serializeContent(T response)
                                                                           throws SerializationException {
        return false;
    }

    /** 
     * @see com.alipay.remoting.CustomSerializer#deserializeContent(com.alipay.remoting.rpc.RequestCommand)
     */
    @Override
    public <T extends RequestCommand> boolean deserializeContent(T request)
                                                                           throws DeserializationException {
        return false;
    }

    /** 
     * @see com.alipay.remoting.CustomSerializer#deserializeContent(com.alipay.remoting.rpc.ResponseCommand, InvokeContext)
     */
    @Override
    public <T extends ResponseCommand> boolean deserializeContent(T response,
                                                                  InvokeContext invokeContext)
                                                                                              throws DeserializationException {
        return false;
    }

}
