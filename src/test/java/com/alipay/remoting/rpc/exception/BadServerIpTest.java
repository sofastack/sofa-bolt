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
package com.alipay.remoting.rpc.exception;

import java.util.concurrent.Executor;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alipay.remoting.BizContext;
import com.alipay.remoting.rpc.RpcServer;
import com.alipay.remoting.rpc.common.RequestBody;
import com.alipay.remoting.rpc.protocol.SyncUserProcessor;

/**
 * 
 * @author jiangping
 * @version $Id: BadServerIpTest.java, v 0.1 2015-12-3 PM5:01:30 tao Exp $
 */
public class BadServerIpTest {

    Logger logger = LoggerFactory.getLogger(BadServerIpTest.class);

    @Test
    public void cantAssignTest() {
        BadServer server = new BadServer("59.66.132.166");
        try {
            server.startServer();
        } catch (Exception e) {
            logger.error("Start server failed!", e);
        }
    }

    @Test
    public void cantResolveTest() {
        BadServer server = new BadServer("59.66.132.1666");
        try {
            server.startServer();
        } catch (Exception e) {
            logger.error("Start server failed!", e);
        }
    }

    class BadServer {

        Logger    logger = LoggerFactory.getLogger(BadServer.class);
        RpcServer server;
        String    ip;

        public BadServer(String ip) {
            this.ip = ip;
        }

        public void startServer() {
            server = new RpcServer(ip, 1111);
            server.registerUserProcessor(new SyncUserProcessor<RequestBody>() {
                @Override
                public Object handleRequest(BizContext bizCtx, RequestBody request)
                                                                                   throws Exception {
                    logger.warn("Request received:" + request);
                    return "hello world!";
                }

                @Override
                public String interest() {
                    return String.class.getName();
                }

                @Override
                public Executor getExecutor() {
                    return null;
                }

            });
            server.start();
        }
    }

}