package com.alipay.remoting.demo;

import com.alipay.remoting.Connection;
import com.alipay.remoting.exception.RemotingException;
import com.alipay.remoting.rpc.RpcClient;
import com.alipay.remoting.rpc.RpcConfigs;
import com.alipay.remoting.rpc.common.BoltServer;
import com.alipay.remoting.rpc.common.FifoServerUserProcessor;
import com.alipay.remoting.rpc.common.PortScan;
import com.alipay.remoting.rpc.common.RequestBody;

public class FifoDemoByMain {

    public static void main(String[] args) throws RemotingException, InterruptedException {
        int port = PortScan.select();
        System.out.println("port is " + port);

        // 1. create a Rpc server with port assigned
        BoltServer server = new BoltServer(port);

        // 2. register user processor for client request
        FifoServerUserProcessor serverUserProcessor = new FifoServerUserProcessor();
        server.registerUserProcessor(serverUserProcessor);

        // 3. key point: close the ability to dispatch msg list to another thead pool
        System.setProperty(RpcConfigs.DISPATCH_MSG_LIST_IN_DEFAULT_EXECUTOR,"false");

        // 4. server start
        server.start();

        // 5. create a rpc client
        RpcClient client = new RpcClient();

        // 6. client start
        client.startup();

        // 7. key point: send requests in the same connection in order
        Connection connection = client.getConnection("127.0.0.1:" + port, 1000);
        for (int i = 0; i < 1000; i++) {
            RequestBody req = new RequestBody(i, "fifo message");
            client.oneway(connection, req);
        }

        Thread.sleep(1000);

        //8. close
        client.shutdown();
        server.stop();
    }
}
