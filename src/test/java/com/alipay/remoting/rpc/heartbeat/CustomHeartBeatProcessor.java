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
package com.alipay.remoting.rpc.heartbeat;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alipay.remoting.AbstractRemotingProcessor;
import com.alipay.remoting.RemotingCommand;
import com.alipay.remoting.RemotingContext;

/**
 * CustomHeartBeatProcessor
 * 
 * @author xiaomin.cxm
 * @version $Id: CustomHeartBeatProcessor.java, v 0.1 Apr 12, 2016 12:05:19 PM xiaomin.cxm Exp $
 */
public class CustomHeartBeatProcessor extends AbstractRemotingProcessor<RemotingCommand> {
    static Logger         logger         = LoggerFactory.getLogger(CustomHeartBeatProcessor.class);

    private AtomicInteger heartBeatTimes = new AtomicInteger();

    public int getHeartBeatTimes() {
        return heartBeatTimes.get();
    }

    public void reset() {
        this.heartBeatTimes.set(0);
    }

    @Override
    public void doProcess(RemotingContext ctx, RemotingCommand msg) throws Exception {
        heartBeatTimes.incrementAndGet();
        java.text.DateFormat format1 = new java.text.SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        logger.warn("heart beat received:" + format1.format(new Date()));
    }
}
