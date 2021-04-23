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
package com.alipay.remoting.rpc.protocol;

import java.io.UnsupportedEncodingException;

import com.alipay.remoting.CustomSerializer;
import com.alipay.remoting.CustomSerializerManager;
import com.alipay.remoting.InvokeContext;
import com.alipay.remoting.config.Configs;
import com.alipay.remoting.exception.DeserializationException;
import com.alipay.remoting.exception.SerializationException;
import com.alipay.remoting.rpc.ResponseCommand;
import com.alipay.remoting.serialization.SerializerManager;

/**
 * Response command for Rpc.
 * 
 * @author jiangping
 * @version $Id: RpcResponseCommand.java, v 0.1 2015-9-25 PM2:15:41 tao Exp $
 */
public class RpcResponseCommand extends ResponseCommand {
    /** For serialization  */
    private static final long serialVersionUID = 5667111367880018776L;
    private Object            responseObject;

    private String            responseClass;

    private CustomSerializer  customSerializer;
    private Object            responseHeader;

    private String            errorMsg;

    public RpcResponseCommand() {
        super(RpcCommandCode.RPC_RESPONSE);
    }

    public RpcResponseCommand(Object response) {
        super(RpcCommandCode.RPC_RESPONSE);
        this.responseObject = response;
    }

    public RpcResponseCommand(int id, Object response) {
        super(RpcCommandCode.RPC_RESPONSE, id);
        this.responseObject = response;
    }

    /**
     * Getter method for property <tt>responseObject</tt>.
     * 
     * @return property value of responseObject
     */
    public Object getResponseObject() {
        return responseObject;
    }

    /**
     * Setter method for property <tt>responseObject</tt>.
     * 
     * @param response value to be assigned to property responseObject
     */
    public void setResponseObject(Object response) {
        this.responseObject = response;
    }

    @Override
    public void serializeClazz() throws SerializationException {
        if (this.getResponseClass() != null) {
            try {
                byte[] clz = this.getResponseClass().getBytes(Configs.DEFAULT_CHARSET);
                this.setClazz(clz);
            } catch (UnsupportedEncodingException e) {
                throw new SerializationException("Unsupported charset: " + Configs.DEFAULT_CHARSET,
                    e);
            }
        }
    }

    @Override
    public void deserializeClazz() throws DeserializationException {
        if (this.getClazz() != null && this.getResponseClass() == null) {
            try {
                this.setResponseClass(new String(this.getClazz(), Configs.DEFAULT_CHARSET));
            } catch (UnsupportedEncodingException e) {
                throw new DeserializationException("Unsupported charset: "
                                                   + Configs.DEFAULT_CHARSET, e);
            }
        }
    }

    @Override
    public void serializeContent(InvokeContext invokeContext) throws SerializationException {
        if (this.getResponseObject() != null) {
            try {
                if (this.getCustomSerializer() != null
                    && this.getCustomSerializer().serializeContent(this)) {
                    return;
                }

                this.setContent(SerializerManager.getSerializer(this.getSerializer()).serialize(
                    this.responseObject));
            } catch (SerializationException e) {
                throw e;
            } catch (Exception e) {
                throw new SerializationException(
                    "Exception caught when serialize content of rpc response command!", e);
            }
        }
    }

    @Override
    public void deserializeContent(InvokeContext invokeContext) throws DeserializationException {
        if (this.getResponseObject() == null) {
            try {
                if (this.getCustomSerializer() != null
                    && this.getCustomSerializer().deserializeContent(this, invokeContext)) {
                    return;
                }
                if (this.getContent() != null) {
                    this.setResponseObject(SerializerManager.getSerializer(this.getSerializer())
                        .deserialize(this.getContent(), this.responseClass));
                }
            } catch (DeserializationException e) {
                throw e;
            } catch (Exception e) {
                throw new DeserializationException(
                    "Exception caught when deserialize content of rpc response command!", e);
            }
        }

    }

    @Override
    public void serializeHeader(InvokeContext invokeContext) throws SerializationException {
        if (this.getCustomSerializer() != null) {
            try {
                this.getCustomSerializer().serializeHeader(this);
            } catch (SerializationException e) {
                throw e;
            } catch (Exception e) {
                throw new SerializationException(
                    "Exception caught when serialize header of rpc response command!", e);
            }
        }
    }

    @Override
    public void deserializeHeader(InvokeContext invokeContext) throws DeserializationException {
        if (this.getHeader() != null && this.getResponseHeader() == null) {
            if (this.getCustomSerializer() != null) {
                try {
                    this.getCustomSerializer().deserializeHeader(this, invokeContext);
                } catch (DeserializationException e) {
                    throw e;
                } catch (Exception e) {
                    throw new DeserializationException(
                        "Exception caught when deserialize header of rpc response command!", e);
                }
            }
        }
    }

    /**
     * Getter method for property <tt>responseClass</tt>.
     * 
     * @return property value of responseClass
     */
    public String getResponseClass() {
        return responseClass;
    }

    /**
     * Setter method for property <tt>responseClass</tt>.
     * 
     * @param responseClass value to be assigned to property responseClass
     */
    public void setResponseClass(String responseClass) {
        this.responseClass = responseClass;
    }

    /**
     * Getter method for property <tt>responseHeader</tt>.
     * 
     * @return property value of responseHeader
     */
    public Object getResponseHeader() {
        return responseHeader;
    }

    /**
     * Setter method for property <tt>responseHeader</tt>.
     * 
     * @param responseHeader value to be assigned to property responseHeader
     */
    public void setResponseHeader(Object responseHeader) {
        this.responseHeader = responseHeader;
    }

    /**
     * Getter method for property <tt>customSerializer</tt>.
     * 
     * @return property value of customSerializer
     */
    public CustomSerializer getCustomSerializer() {
        if (this.customSerializer != null) {
            return customSerializer;
        }
        if (this.responseClass != null) {
            this.customSerializer = CustomSerializerManager.getCustomSerializer(this.responseClass);
        }
        if (this.customSerializer == null) {
            this.customSerializer = CustomSerializerManager.getCustomSerializer(this.getCmdCode());
        }
        return this.customSerializer;
    }

    /**
     * Getter method for property <tt>errorMsg</tt>.
     * 
     * @return property value of errorMsg
     */
    public String getErrorMsg() {
        return errorMsg;
    }

    /**
     * Setter method for property <tt>errorMsg</tt>.
     * 
     * @param errorMsg value to be assigned to property errorMsg
     */
    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
}
