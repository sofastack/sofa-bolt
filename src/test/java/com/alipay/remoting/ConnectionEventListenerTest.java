package com.alipay.remoting;

import com.alipay.remoting.rpc.RpcClient;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class ConnectionEventListenerTest {

    @Test
    public void addConnectionEventProcessorConcurrentTest() throws InterruptedException {
        int concurrentNum = 100;
        CountDownLatch countDownLatch = new CountDownLatch(concurrentNum);
        RpcClient rpcClient = new RpcClient();
        for (int i = 0; i < concurrentNum; ++i) {
            MyThread thread = new MyThread(countDownLatch, rpcClient);
            new Thread(thread).start();
        }
        Assert.assertTrue(countDownLatch.await(2, TimeUnit.SECONDS));
    }

    static class MyThread implements Runnable {
        CountDownLatch countDownLatch;
        RpcClient rpcClient;

        public MyThread(CountDownLatch countDownLatch, RpcClient rpcClient) {
            this.countDownLatch = countDownLatch;
            this.rpcClient = rpcClient;
        }

        @Override
        public void run() {
            try {
                rpcClient.addConnectionEventProcessor(ConnectionEventType.CONNECT, (remoteAddress, connection) -> {});
            } catch (Exception e) {
                fail();
            } finally {
                countDownLatch.countDown();
            }
        }
    }
}