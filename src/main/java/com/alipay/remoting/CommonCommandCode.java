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
 * The common command code, especially for heart beat command.
 * 
 * @author jiangping
 * @version $Id: CommonCommandCode.java, v 0.1 2015-9-21 PM5:05:59 tao Exp $
 */
public enum CommonCommandCode implements CommandCode {

    HEARTBEAT(CommandCode.HEARTBEAT_VALUE);

    private short value;

    CommonCommandCode(short value) {
        this.value = value;
    }

    @Override
    public short value() {
        return this.value;
    }

    public static CommonCommandCode valueOf(short value) {
        switch (value) {
            case CommandCode.HEARTBEAT_VALUE:
                return HEARTBEAT;
        }
        throw new IllegalArgumentException("Unknown Rpc command code value ," + value);
    }

}