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
package com.alipay.remoting.rpc.connectionmanage;

import com.alipay.remoting.Connection;
import com.alipay.remoting.ConnectionEventProcessor;
import com.alipay.remoting.ConnectionEventType;
import com.alipay.remoting.exception.RemotingException;
import com.alipay.remoting.rpc.RpcClient;
import com.alipay.remoting.rpc.common.BoltServer;
import com.alipay.remoting.rpc.common.CONNECTEventProcessor;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author chengyi (mark.lx@antfin.com) 2019-06-27 13:49
 */
public class ConnectionExceptionTest {

    @Test
    public void testConnectionException() throws RemotingException, InterruptedException {
        CONNECTEventProcessor serverConnectProcessor = new CONNECTEventProcessor();

        BoltServer boltServer = new BoltServer(1024);
        boltServer.addConnectionEventProcessor(ConnectionEventType.CONNECT, serverConnectProcessor);
        boltServer.start();

        final String[] closedUrl = new String[1];
        RpcClient client = new RpcClient();
        client.enableReconnectSwitch();
        client.addConnectionEventProcessor(ConnectionEventType.CLOSE,
            new ConnectionEventProcessor() {
                @Override
                public void onEvent(String remoteAddr, Connection conn) {
                    closedUrl[0] = remoteAddr;
                }
            });
        client.init();

        Connection connection = client.getConnection("127.0.0.1:1024", 1000);
        Thread.sleep(10);
        Assert.assertEquals(1, serverConnectProcessor.getConnectTimes());

        connection.getChannel().close();

        Thread.sleep(100);
        Assert.assertEquals("127.0.0.1:1024", closedUrl[0]);

        // connection has been created by ReconnectManager
        Thread.sleep(1000 * 2);
        Assert.assertEquals(2, serverConnectProcessor.getConnectTimes());
        connection = client.getConnection("127.0.0.1:1024", 1000);
        Assert.assertTrue(connection.isFine());
        Assert.assertEquals(2, serverConnectProcessor.getConnectTimes());

        boltServer.stop();
    }

}
