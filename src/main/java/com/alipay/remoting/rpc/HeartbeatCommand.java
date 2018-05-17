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

import com.alipay.remoting.CommonCommandCode;
import com.alipay.remoting.util.IDGenerator;

/**
 * Heart beat.
 * 
 * @author jiangping
 * @version $Id: HeartbeatCommand.java, v 0.1 2015-9-10 AM9:46:36 tao Exp $
 */
public class HeartbeatCommand extends RequestCommand {

    /** For serialization  */
    private static final long serialVersionUID = 4949981019109517725L;

    /**
     * Construction.
     */
    public HeartbeatCommand() {
        super(CommonCommandCode.HEARTBEAT);
        this.setId(IDGenerator.nextId());
    }

}
