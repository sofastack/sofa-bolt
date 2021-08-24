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
package com.alipay.remoting.benchmark;

import com.alipay.remoting.config.BoltServerOption;
import com.alipay.remoting.rpc.RpcServer;

/**
 * @author jiachun.fjc
 */
public class BenchmarkServer {

    public static void main(String[] args) {
        System.setProperty("bolt.netty.buffer.high.watermark", String.valueOf(64 * 1024 * 1024));
        System.setProperty("bolt.netty.buffer.low.watermark", String.valueOf(32 * 1024 * 1024));
        RpcServer rpcServer = new RpcServer(18090, true, true);
        rpcServer.option(BoltServerOption.NETTY_FLUSH_CONSOLIDATION, true);
        rpcServer.registerUserProcessor(new BenchmarkUserProcessor());
        rpcServer.startup();
    }
}
