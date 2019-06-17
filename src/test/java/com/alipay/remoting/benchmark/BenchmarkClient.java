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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

import com.alipay.remoting.config.switches.GlobalSwitch;
import com.alipay.remoting.exception.RemotingException;
import com.alipay.remoting.rpc.RpcClient;
import com.alipay.remoting.rpc.RpcResponseFuture;

/**
 * @author jiachun.fjc
 */
public class BenchmarkClient {

    /**
     * body=4096, CODEC_FLUSH_CONSOLIDATION=turnOff
     * Request count: 10240000, time: 347 second, qps: 29510
     *
     * body=4096, CODEC_FLUSH_CONSOLIDATION=turnOn
     * Request count: 10240000, time: 311 second, qps: 32926
     *
     * -------------------------------------------------------
     *
     * body=1024, CODEC_FLUSH_CONSOLIDATION=turnOff
     * Request count: 10240000, time: 206 second, qps: 49708
     *
     * body=1024, CODEC_FLUSH_CONSOLIDATION=turnOn
     * Request count: 10240000, time: 148 second, qps: 69189
     *
     * -------------------------------------------------------
     *
     * body=128, CODEC_FLUSH_CONSOLIDATION=turnOff
     * Request count: 10240000, time: 148 second, qps: 69189
     *
     * body=128, CODEC_FLUSH_CONSOLIDATION=turnOn
     * Request count: 10240000, time: 69 second, qps: 148405
     *
     */

    private static final byte[] BYTES = new byte[128];

    static {
        new Random().nextBytes(BYTES);
    }

    public static void main(String[] args) throws RemotingException, InterruptedException {
        System.setProperty("bolt.netty.buffer.high.watermark", String.valueOf(64 * 1024 * 1024));
        System.setProperty("bolt.netty.buffer.low.watermark", String.valueOf(32 * 1024 * 1024));
        RpcClient rpcClient = new RpcClient();
        rpcClient.switches().turnOn(GlobalSwitch.CODEC_FLUSH_CONSOLIDATION);
        rpcClient.startup();

        int processors = Runtime.getRuntime().availableProcessors();
        callWithFuture(rpcClient, "127.0.0.1:18090", processors << 4);
    }

    @SuppressWarnings("SameParameterValue")
    private static void callWithFuture(final RpcClient rpcClient, final String address, int threads) {
        // warmup
        for (int i = 0; i < 10000; i++) {
            try {
                RpcResponseFuture f = rpcClient.invokeWithFuture(address,
                    new Request<byte[]>(BYTES), 5000);
                f.get();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        final int t = 80000;
        long start = System.currentTimeMillis();
        final CountDownLatch latch = new CountDownLatch(threads);
        final AtomicLong count = new AtomicLong();
        final int futureSize = 80;
        for (int i = 0; i < threads; i++) {
            new Thread(new Runnable() {

                List<RpcResponseFuture> futures = new ArrayList<RpcResponseFuture>(futureSize);

                @Override
                public void run() {
                    for (int i = 0; i < t; i++) {
                        try {
                            RpcResponseFuture f = rpcClient.invokeWithFuture(address,
                                new Request<byte[]>(BYTES), 5000);
                            futures.add(f);
                            if (futures.size() == futureSize) {
                                int fSize = futures.size();
                                for (int j = 0; j < fSize; j++) {
                                    try {
                                        futures.get(j).get();
                                    } catch (Throwable t) {
                                        t.printStackTrace();
                                    }
                                }
                                futures.clear();
                            }
                            if (count.getAndIncrement() % 10000 == 0) {
                                System.out.println("count=" + count.get());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (!futures.isEmpty()) {
                        int fSize = futures.size();
                        for (int j = 0; j < fSize; j++) {
                            try {
                                futures.get(j).get();
                            } catch (Throwable t) {
                                t.printStackTrace();
                            }
                        }
                        futures.clear();
                    }
                    latch.countDown();
                }
            }, "benchmark_" + i).start();
        }

        try {
            latch.await();
            System.out.println("count=" + count.get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long second = (System.currentTimeMillis() - start) / 1000;
        System.out.println("Request count: " + count.get() + ", time: " + second + " second, qps: "
                           + count.get() / second);
    }
}
