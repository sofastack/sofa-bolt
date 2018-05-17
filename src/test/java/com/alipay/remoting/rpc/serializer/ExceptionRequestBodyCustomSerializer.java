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
package com.alipay.remoting.rpc.serializer;

import java.util.concurrent.atomic.AtomicBoolean;

import com.alipay.remoting.CustomSerializer;
import com.alipay.remoting.DefaultCustomSerializer;
import com.alipay.remoting.InvokeContext;
import com.alipay.remoting.exception.DeserializationException;
import com.alipay.remoting.exception.SerializationException;
import com.alipay.remoting.rpc.RequestCommand;

/**
 * a request body serializer throw exception
 * 
 * @author xiaomin.cxm
 * @version $Id: NormalRequestBodyCustomSerializer.java, v 0.1 Apr 11, 2016 10:18:59 PM xiaomin.cxm Exp $
 */
public class ExceptionRequestBodyCustomSerializer extends DefaultCustomSerializer {

    private AtomicBoolean serialFlag               = new AtomicBoolean();
    private AtomicBoolean deserialFlag             = new AtomicBoolean();
    private boolean       serialException          = false;
    private boolean       serialRuntimeException   = true;
    private boolean       deserialException        = false;
    private boolean       deserialRuntimeException = true;

    public ExceptionRequestBodyCustomSerializer(boolean serialRuntimeException,
                                                boolean deserialRuntimeException) {
        this.serialRuntimeException = serialRuntimeException;
        this.deserialRuntimeException = deserialRuntimeException;
    }

    public ExceptionRequestBodyCustomSerializer(boolean serialException,
                                                boolean serialRuntimeException,
                                                boolean deserialException,
                                                boolean deserialRuntimeException) {
        this.serialException = serialException;
        this.serialRuntimeException = serialRuntimeException;
        this.deserialException = deserialException;
        this.deserialRuntimeException = deserialRuntimeException;
    }

    /** 
     * @see CustomSerializer#serializeContent(RequestCommand, InvokeContext)
     */
    @Override
    public <T extends RequestCommand> boolean serializeContent(T req, InvokeContext invokeContext)
                                                                                                  throws SerializationException {
        serialFlag.set(true);
        if (serialRuntimeException) {
            throw new RuntimeException(
                "serialRuntimeException in ExceptionRequestBodyCustomSerializer!");
        } else if (serialException) {
            throw new SerializationException(
                "serialException in ExceptionRequestBodyCustomSerializer!");
        } else {
            return false;// use default codec 
        }
    }

    /** 
     * @see CustomSerializer#deserializeContent(RequestCommand)
     */
    @Override
    public <T extends RequestCommand> boolean deserializeContent(T req)
                                                                       throws DeserializationException {
        deserialFlag.set(true);
        if (deserialRuntimeException) {
            throw new RuntimeException(
                "deserialRuntimeException in ExceptionRequestBodyCustomSerializer!");
        } else if (deserialException) {
            throw new DeserializationException(
                "deserialException in ExceptionRequestBodyCustomSerializer!");
        } else {
            return false;// use default codec 
        }
    }

    public boolean isSerialized() {
        return this.serialFlag.get();
    }

    public boolean isDeserialized() {
        return this.deserialFlag.get();
    }
}
