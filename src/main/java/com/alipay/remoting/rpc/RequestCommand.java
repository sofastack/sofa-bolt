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

import com.alipay.remoting.CommandCode;

/**
 * Command of request.
 * 
 * @author jiangping
 * @version $Id: RequestCommand.java, v 0.1 2015-9-10 AM10:27:59 tao Exp $
 */
public abstract class RequestCommand extends RpcCommand {

    /** For serialization  */
    private static final long serialVersionUID = -3457717009326601317L;
    /** timeout, -1 stands for no timeout */
    private int               timeout          = -1;

    public RequestCommand() {
        super(RpcCommandType.REQUEST);
    }

    public RequestCommand(CommandCode code) {
        super(RpcCommandType.REQUEST, code);
    }

    public RequestCommand(byte type, CommandCode code) {
        super(type, code);
    }

    public RequestCommand(byte version, byte type, CommandCode code) {
        super(version, type, code);
    }

    /**
     * Getter method for property <tt>timeout</tt>.
     * 
     * @return property value of timeout
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * Setter method for property <tt>timeout</tt>.
     * 
     * @param timeout value to be assigned to property timeout
     */
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
