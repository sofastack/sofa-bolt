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

import com.alipay.remoting.log.BoltLoggerFactory;
import org.slf4j.Logger;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Server template for remoting.
 * 
 * @author jiangping
 * @version $Id: AbstractRemotingServer.java, v 0.1 2015-9-5 PM7:37:48 tao Exp $
 * @author chengyi
 * @version $Id: AbstractRemotingServer.java, v 0.1 2018-6-16 AM08:20:41 tao Exp $
 */
public abstract class AbstractRemotingServer implements RemotingServer {

    private static final Logger logger  = BoltLoggerFactory.getLogger("CommonDefault");

    private AtomicBoolean       started = new AtomicBoolean(false);
    private String              ip;
    private int                 port;

    public AbstractRemotingServer(int port) {
        this(new InetSocketAddress(port).getAddress().getHostAddress(), port);
    }

    public AbstractRemotingServer(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    @Override
    public boolean start() {
        if (started.compareAndSet(false, true)) {
            doInit();

            try {
                logger.warn("Server started on port: " + port);
                return doStart();
            } catch (Throwable t) {
                started.set(false);
                this.stop();
                throw new IllegalStateException("ERROR: Failed to start the Server!", t);
            }
        } else {
            String errMsg = "ERROR: The server has already started!";
            logger.error(errMsg);
            throw new IllegalStateException(errMsg);
        }
    }

    @Override
    public boolean stop() {
        if (started.compareAndSet(true, false)) {
            return this.doStop();
        } else {
            throw new IllegalStateException("ERROR: The server has already stopped!");
        }
    }

    @Override
    public String ip() {
        return ip;
    }

    @Override
    public int port() {
        return port;
    }

    protected abstract void doInit();

    protected abstract boolean doStart() throws InterruptedException;

    protected abstract boolean doStop();

}
