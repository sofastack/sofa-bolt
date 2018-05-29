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
package com.alipay.remoting.util;

import java.util.zip.CRC32;

/**
 * CRC32 utility.
 * @author jiangping
 * @version $Id: CrcUtil2, v 0.1 2017-06-05 11:29 Timo Exp $
 */
public class CrcUtil {

    private static final ThreadLocal<CRC32> CRC_32_THREAD_LOCAL = new ThreadLocal<CRC32>() {
                                                                    @Override
                                                                    protected CRC32 initialValue() {
                                                                        return new CRC32();
                                                                    }
                                                                };

    /**
     * Compute CRC32 code for byte[].
     *
     * @param array
     * @return
     */
    public static final int crc32(byte[] array) {
        if (array != null) {
            return crc32(array, 0, array.length);
        }

        return 0;
    }

    /**
     * Compute CRC32 code for byte[].
     *
     * @param array
     * @param offset
     * @param length
     * @return
     */
    public static final int crc32(byte[] array, int offset, int length) {
        CRC32 crc32 = CRC_32_THREAD_LOCAL.get();
        crc32.update(array, offset, length);
        int ret = (int) crc32.getValue();
        crc32.reset();
        return ret;
    }

}
