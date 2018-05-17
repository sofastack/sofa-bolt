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
package com.alipay.remoting.rpc.userprocessor.executorselector;

import static com.alipay.remoting.rpc.userprocessor.executorselector.DefaultExecutorSelector.EXECUTOR1;

import java.io.UnsupportedEncodingException;

import com.alipay.remoting.DefaultCustomSerializer;
import com.alipay.remoting.InvokeContext;
import com.alipay.remoting.exception.DeserializationException;
import com.alipay.remoting.exception.SerializationException;
import com.alipay.remoting.rpc.RequestCommand;
import com.alipay.remoting.rpc.ResponseCommand;
import com.alipay.remoting.rpc.protocol.RpcRequestCommand;
import com.alipay.remoting.rpc.protocol.RpcResponseCommand;

/**
 *
 * @author tsui
 * @version $Id: CustomHeaderSerializer.java, v 0.1 2017-04-24 11:48 tsui Exp $
 */
public class CustomHeaderSerializer extends DefaultCustomSerializer {
    /**
     * @see com.alipay.remoting.CustomSerializer#serializeHeader(com.alipay.remoting.rpc.RequestCommand, InvokeContext)
     */
    @Override
    public <T extends RequestCommand> boolean serializeHeader(T request, InvokeContext invokeContext)
                                                                                                     throws SerializationException {
        if (request instanceof RpcRequestCommand) {
            RpcRequestCommand requestCommand = (RpcRequestCommand) request;
            try {
                requestCommand.setHeader(EXECUTOR1.getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                System.err.println("UnsupportedEncodingException");
            }
            return true;
        }
        return false;
    }

    /**
     * @see com.alipay.remoting.CustomSerializer#deserializeHeader(com.alipay.remoting.rpc.RequestCommand)
     */
    @Override
    public <T extends RequestCommand> boolean deserializeHeader(T request)
                                                                          throws DeserializationException {
        if (request instanceof RpcRequestCommand) {
            RpcRequestCommand requestCommand = (RpcRequestCommand) request;
            byte[] header = requestCommand.getHeader();
            try {
                requestCommand.setRequestHeader(new String(header, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                System.err.println("UnsupportedEncodingException");
            }
            return true;
        }
        return false;
    }

    /**
     * @see com.alipay.remoting.CustomSerializer#serializeHeader(com.alipay.remoting.rpc.ResponseCommand)
     */
    @Override
    public <T extends ResponseCommand> boolean serializeHeader(T response)
                                                                          throws SerializationException {
        if (response instanceof RpcResponseCommand) {
            RpcResponseCommand responseCommand = (RpcResponseCommand) response;
            try {
                responseCommand.setHeader(EXECUTOR1.getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                System.err.println("UnsupportedEncodingException");
            }
            return true;
        }
        return false;
    }

    /**
     * @see com.alipay.remoting.CustomSerializer#deserializeHeader(com.alipay.remoting.rpc.ResponseCommand, InvokeContext)
     */
    @Override
    public <T extends ResponseCommand> boolean deserializeHeader(T response,
                                                                 InvokeContext invokeContext)
                                                                                             throws DeserializationException {
        if (response instanceof RpcResponseCommand) {
            RpcResponseCommand responseCommand = (RpcResponseCommand) response;
            byte[] header = responseCommand.getHeader();
            try {
                responseCommand.setResponseHeader(new String(header, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                System.err.println("UnsupportedEncodingException");
            }
            return true;
        }
        return false;
    }
}