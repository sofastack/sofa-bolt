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

import com.alipay.remoting.exception.CodecException;

final class RpcCommandDecoderLengthValidator {

    private RpcCommandDecoderLengthValidator() {
    }

    static int validateAndGetTotalLength(short classLen, short headerLen, int contentLen,
                                         int trailerLen) throws CodecException {
        if (classLen < 0 || headerLen < 0 || contentLen < 0 || trailerLen < 0) {
            throw new CodecException("Illegal RPC command length: classLen=" + classLen
                                     + ", headerLen=" + headerLen + ", contentLen=" + contentLen
                                     + ", trailerLen=" + trailerLen);
        }

        long totalLength = (long) classLen + headerLen + contentLen + trailerLen;
        if (totalLength > Integer.MAX_VALUE) {
            throw new CodecException("RPC command length exceeds integer range: " + totalLength);
        }
        return (int) totalLength;
    }
}
