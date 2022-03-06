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
package com.alipay.remoting.simpledemo;

import com.alipay.remoting.exception.RemotingException;
import com.alipay.remoting.rpc.RpcClient;
import com.alipay.remoting.rpc.RpcServer;

public class QuickStartServerAndClient {
    public static void main(String[] args) throws RemotingException, InterruptedException {
        RpcServer rpcServer = new RpcServer(9876);
        rpcServer.registerUserProcessor(new SimpleUserProcessor());
        rpcServer.startup();

        RpcClient rpcClient = new RpcClient();
        rpcClient.startup();
        for (int i = 0; i < 10; i++) {
            SimpleResponse response = (SimpleResponse) rpcClient.invokeSync("127.0.0.1:9876",
                new SimpleRequest(i), 1000);
            System.out.println("i=" + i + " res=" + response.getRes());
            Thread.sleep(1000);
        }

        rpcClient.shutdown();
        rpcServer.shutdown();
    }
}
