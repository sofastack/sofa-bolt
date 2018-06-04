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
 * Status of the response.
 * 
 * @author jiangping
 * @version $Id: ResponseStatus.java, v 0.1 2015-9-28 PM3:08:12 tao Exp $
 */
public enum ResponseStatus {
    SUCCESS, // Ok
    ERROR, // Error caught
    SERVER_EXCEPTION, // Exception caught
    UNKNOWN, // Unknown...
    SERVER_THREADPOOL_BUSY, // Process thread pool busy
    ERROR_COMM, // Error of communication
    NO_PROCESSOR, // No processor find
    TIMEOUT, // Timeout
    CLIENT_SEND_ERROR, // Send failed
    CODEC_EXCEPTION, // Exception in encode or decode
    CONNECTION_CLOSED, // Connection closed.
    SERVER_SERIAL_EXCEPTION, // server serialize exception
    SERVER_DESERIAL_EXCEPTION // server deserialize exception
    ;

    /**
     * Convert to short.
     * @return
     */
    public short getValue() {
        switch (this) {
            case SUCCESS:
                return 0x0000;
            case ERROR:
                return 0x0001;
            case SERVER_EXCEPTION:
                return 0x0002;
            case UNKNOWN:
                return 0x0003;
            case SERVER_THREADPOOL_BUSY:
                return 0x0004;
            case ERROR_COMM:
                return 0x0005;
            case NO_PROCESSOR:
                return 0x0006;
            case TIMEOUT:
                return 0x0007;
            case CLIENT_SEND_ERROR:
                return 0x0008;
            case CODEC_EXCEPTION:
                return 0x0009;
            case CONNECTION_CLOSED:
                return 0x0010;
            case SERVER_SERIAL_EXCEPTION:
                return 0x0011;
            case SERVER_DESERIAL_EXCEPTION:
                return 0x0012;

        }
        throw new IllegalArgumentException("Unknown status," + this);
    }

    /**
     * Convert to ResponseStatus.
     * 
     * @param value
     * @return
     */
    public static ResponseStatus valueOf(short value) {
        switch (value) {
            case 0x0000:
                return SUCCESS;
            case 0x0001:
                return ERROR;
            case 0x0002:
                return SERVER_EXCEPTION;
            case 0x0003:
                return UNKNOWN;
            case 0x0004:
                return SERVER_THREADPOOL_BUSY;
            case 0x0005:
                return ERROR_COMM;
            case 0x0006:
                return NO_PROCESSOR;
            case 0x0007:
                return TIMEOUT;
            case 0x0008:
                return CLIENT_SEND_ERROR;
            case 0x0009:
                return CODEC_EXCEPTION;
            case 0x0010:
                return CONNECTION_CLOSED;
            case 0x0011:
                return SERVER_SERIAL_EXCEPTION;
            case 0x0012:
                return SERVER_DESERIAL_EXCEPTION;
        }
        throw new IllegalArgumentException("Unknown status value ," + value);
    }
}
