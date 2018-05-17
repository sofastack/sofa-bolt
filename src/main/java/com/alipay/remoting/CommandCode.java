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
 * Remoting command code stands for a specific remoting command, and every kind of command has its own code.
 * 
 * @author jiangping
 * @version $Id: CommandCode.java, v 0.1 2015-9-7 PM7:10:18 tao Exp $
 */
public interface CommandCode {
    // value 0 is occupied by heartbeat, don't use value 0 for other commands
    short HEARTBEAT_VALUE = 0;

    /**
     * 
     * @return the short value of the code
     */
    short value();

}
