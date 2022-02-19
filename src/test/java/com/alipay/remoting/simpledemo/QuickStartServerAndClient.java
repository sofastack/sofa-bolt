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
        for (int i = 0; i < 10; i++) {
            SimpleResponse response = (SimpleResponse) rpcClient.invokeSync("127.0.0.1:9876", new SimpleRequest(i), 1000);
            System.out.println("i=" + i + " res=" + response.getRes());
            Thread.sleep(1000);
        }

        rpcClient.shutdown();
        rpcServer.shutdown();
    }
}
