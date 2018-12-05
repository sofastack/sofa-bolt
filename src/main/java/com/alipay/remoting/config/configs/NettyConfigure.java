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
package com.alipay.remoting.config.configs;

/**
 * netty related configuration items
 *
 * @author tsui
 * @version $Id: NettyConfigure.java, v 0.1 2018-07-30 21:42 tsui Exp $$ 
 */
public interface NettyConfigure {
    /**
     * Initialize netty write buffer water mark for remoting instance.
     * <p>
     * Notice: This api should be called before init remoting instance.
     *
     * @param low [0, high]
     * @param high [high, Integer.MAX_VALUE)
     */
    void initWriteBufferWaterMark(int low, int high);

    /**
     * get the low water mark for netty write buffer
     * @return low watermark
     */
    int netty_buffer_low_watermark();

    /**
     * get the high water mark for netty write buffer
     * @return high watermark
     */
    int netty_buffer_high_watermark();
}