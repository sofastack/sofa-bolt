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
import com.alipay.remoting.rpc.RequestCommand;
import com.alipay.remoting.serialization.SerializerManager;
import com.alipay.remoting.util.IDGenerator;

/**
 * Request command for Rpc.
 * 
 * @author jiangping
 * @version $Id: RpcRequestCommand.java, v 0.1 2015-9-25 PM2:13:35 tao Exp $
 */
public class RpcRequestCommand extends RequestCommand {
    /** For serialization  */
    private static final long serialVersionUID = -4602613826188210946L;
    private Object            requestObject;
    private String            requestClass;

    private CustomSerializer  customSerializer;
    private Object            requestHeader;

    private transient long    arriveTime       = -1;

    /**
     * create request command without id
     */
    public RpcRequestCommand() {
        super(RpcCommandCode.RPC_REQUEST);
    }

    /**
     * create request command with id and request object
     * @param request request object
     */
    public RpcRequestCommand(Object request) {
        super(RpcCommandCode.RPC_REQUEST);
        this.requestObject = request;
        this.setId(IDGenerator.nextId());
    }

    @Override
    public void serializeClazz() throws SerializationException {
        if (this.requestClass != null) {
            try {
                byte[] clz = this.requestClass.getBytes(Configs.DEFAULT_CHARSET);
                this.setClazz(clz);
            } catch (UnsupportedEncodingException e) {
                throw new SerializationException("Unsupported charset: " + Configs.DEFAULT_CHARSET,
                    e);
            }
        }
    }

    @Override
    public void deserializeClazz() throws DeserializationException {
        if (this.getClazz() != null && this.getRequestClass() == null) {
            try {
                this.setRequestClass(new String(this.getClazz(), Configs.DEFAULT_CHARSET));
            } catch (UnsupportedEncodingException e) {
                throw new DeserializationException("Unsupported charset: "
                                                   + Configs.DEFAULT_CHARSET, e);
            }
        }
    }

    @Override
    public void serializeHeader(InvokeContext invokeContext) throws SerializationException {
        if (this.getCustomSerializer() != null) {
            try {
                this.getCustomSerializer().serializeHeader(this, invokeContext);
            } catch (SerializationException e) {
                throw e;
            } catch (Exception e) {
                throw new SerializationException(
                    "Exception caught when serialize header of rpc request command!", e);
            }
        }
    }

    @Override
    public void deserializeHeader(InvokeContext invokeContext) throws DeserializationException {
        if (this.getHeader() != null && this.getRequestHeader() == null) {
            if (this.getCustomSerializer() != null) {
                try {
                    this.getCustomSerializer().deserializeHeader(this);
                } catch (DeserializationException e) {
                    throw e;
                } catch (Exception e) {
                    throw new DeserializationException(
                        "Exception caught when deserialize header of rpc request command!", e);
                }
            }
        }
    }

    @Override
    public void serializeContent(InvokeContext invokeContext) throws SerializationException {
        if (this.requestObject != null) {
            try {
                if (this.getCustomSerializer() != null
                    && this.getCustomSerializer().serializeContent(this, invokeContext)) {
                    return;
                }

                this.setContent(SerializerManager.getSerializer(this.getSerializer()).serialize(
                    this.requestObject));
            } catch (SerializationException e) {
                throw e;
            } catch (Exception e) {
                throw new SerializationException(
                    "Exception caught when serialize content of rpc request command!", e);
            }
        }
    }

    @Override
    public void deserializeContent(InvokeContext invokeContext) throws DeserializationException {
        if (this.getRequestObject() == null) {
            try {
                if (this.getCustomSerializer() != null
                    && this.getCustomSerializer().deserializeContent(this)) {
                    return;
                }
                if (this.getContent() != null) {
                    this.setRequestObject(SerializerManager.getSerializer(this.getSerializer())
                        .deserialize(this.getContent(), this.requestClass));
                }
            } catch (DeserializationException e) {
                throw e;
            } catch (Exception e) {
                throw new DeserializationException(
                    "Exception caught when deserialize content of rpc request command!", e);
            }
        }
    }

    /**
     * Getter method for property <tt>requestObject</tt>.
     * 
     * @return property value of requestObject
     */
    public Object getRequestObject() {
        return requestObject;
    }

    /**
     * Setter method for property <tt>requestObject</tt>.
     * 
     * @param requestObject value to be assigned to property requestObject
     */
    public void setRequestObject(Object requestObject) {
        this.requestObject = requestObject;
    }

    /**
     * Getter method for property <tt>requestHeader</tt>.
     * 
     * @return property value of requestHeader
     */
    public Object getRequestHeader() {
        return requestHeader;
    }

    /**
     * Setter method for property <tt>requestHeader</tt>.
     * 
     * @param requestHeader value to be assigned to property requestHeader
     */
    public void setRequestHeader(Object requestHeader) {
        this.requestHeader = requestHeader;
    }

    /**
     * Getter method for property <tt>requestClass</tt>.
     * 
     * @return property value of requestClass
     */
    public String getRequestClass() {
        return requestClass;
    }

    /**
     * Setter method for property <tt>requestClass</tt>.
     * 
     * @param requestClass value to be assigned to property requestClass
     */
    public void setRequestClass(String requestClass) {
        this.requestClass = requestClass;
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
        if (this.requestClass != null) {
            this.customSerializer = CustomSerializerManager.getCustomSerializer(this.requestClass);
        }
        if (this.customSerializer == null) {
            this.customSerializer = CustomSerializerManager.getCustomSerializer(this.getCmdCode());
        }
        return this.customSerializer;
    }

    /**
     * Getter method for property <tt>arriveTime</tt>.
     * 
     * @return property value of arriveTime
     */
    public long getArriveTime() {
        return arriveTime;
    }

    /**
     * Setter method for property <tt>arriveTime</tt>.
     * 
     * @param arriveTime value to be assigned to property arriveTime
     */
    public void setArriveTime(long arriveTime) {
        this.arriveTime = arriveTime;
    }
}
