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
package com.alipay.remoting.rpc;

import java.net.InetSocketAddress;

import com.alipay.remoting.CommandCode;
import com.alipay.remoting.ResponseStatus;

/**
 * Command of response.
 * 
 * @author jiangping
 * @version $Id: ResponseCommand.java, v 0.1 2015-9-10 AM10:31:34 tao Exp $
 */
public class ResponseCommand extends RpcCommand {

    /** For serialization  */
    private static final long serialVersionUID = -5194754228565292441L;
    private ResponseStatus    responseStatus;
    private long              responseTimeMillis;
    private InetSocketAddress responseHost;
    private Throwable         cause;

    public ResponseCommand() {
        super(RpcCommandType.RESPONSE);
    }

    public ResponseCommand(CommandCode code) {
        super(RpcCommandType.RESPONSE, code);
    }

    public ResponseCommand(int id) {
        super(RpcCommandType.RESPONSE);
        this.setId(id);
    }

    public ResponseCommand(CommandCode code, int id) {
        super(RpcCommandType.RESPONSE, code);
        this.setId(id);
    }

    public ResponseCommand(byte version, byte type, CommandCode code, int id) {
        super(version, type, code);
        this.setId(id);
    }

    /**
     * Getter method for property <tt>responseTimeMillis</tt>.
     * 
     * @return property value of responseTimeMillis
     */
    public long getResponseTimeMillis() {
        return responseTimeMillis;
    }

    /**
     * Setter method for property <tt>responseTimeMillis</tt>.
     * 
     * @param responseTimeMillis value to be assigned to property responseTimeMillis
     */
    public void setResponseTimeMillis(long responseTimeMillis) {
        this.responseTimeMillis = responseTimeMillis;
    }

    /**
     * Getter method for property <tt>responseHost</tt>.
     * 
     * @return property value of responseHost
     */
    public InetSocketAddress getResponseHost() {
        return responseHost;
    }

    /**
     * Setter method for property <tt>responseHost</tt>.
     * 
     * @param responseHost value to be assigned to property responseHost
     */
    public void setResponseHost(InetSocketAddress responseHost) {
        this.responseHost = responseHost;
    }

    /**
     * Getter method for property <tt>responseStatus</tt>.
     * 
     * @return property value of responseStatus
     */
    public ResponseStatus getResponseStatus() {
        return responseStatus;
    }

    /**
     * Setter method for property <tt>responseStatus</tt>.
     * 
     * @param responseStatus value to be assigned to property responseStatus
     */
    public void setResponseStatus(ResponseStatus responseStatus) {
        this.responseStatus = responseStatus;
    }

    /**
     * Getter method for property <tt>cause</tt>.
     * 
     * @return property value of cause
     */
    public Throwable getCause() {
        return cause;
    }

    /**
     * Setter method for property <tt>cause</tt>.
     * 
     * @param cause value to be assigned to property cause
     */
    public void setCause(Throwable cause) {
        this.cause = cause;
    }

}
