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
package com.alipay.remoting.rpc.common;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PortScan {
    private static final Logger logger = LoggerFactory.getLogger(PortScan.class);

    static public int select() {
        int port = -1;
        ServerSocket ss = null;
        try {
            ss = new ServerSocket();
            ss.bind(null);
            port = ss.getLocalPort();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            try {
                ss.close();
                logger.warn("Server socket close status: {}", ss.isClosed());
            } catch (IOException e) {
            }
        }
        return port;
    }

    public static void main(String[] args) throws Exception {
        int port = PortScan.select();
        ServerSocket ss = new ServerSocket();
        ss.bind(new InetSocketAddress(port));
        logger.warn("listening on port：{}", port);

        Thread.sleep(100);
        Socket s = new Socket("localhost", port);
        System.out.println(s.isConnected());
        System.out.println("local port: " + s.getLocalPort());
        System.out.println("remote port: " + s.getPort());
        Object lock = new Object();

        synchronized (lock) {
            lock.wait();
        }
    }
}
