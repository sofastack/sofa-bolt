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

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import com.alipay.remoting.config.switches.GlobalSwitch;
import com.alipay.remoting.exception.RemotingException;
import com.alipay.remoting.rpc.RpcClient;

/**
 * @author jiachun.fjc
 */
@State(Scope.Benchmark)
public class JMHBenchmarkClient {

    /**
     * CODEC_FLUSH_CONSOLIDATION=turnOn
     * Benchmark                     Mode  Cnt   Score    Error   Units
     * JMHBenchmarkClient.send1024  thrpt    3  51.310 ± 11.585  ops/ms
     * JMHBenchmarkClient.send128   thrpt    3  68.493 ± 20.078  ops/ms
     *
     * CODEC_FLUSH_CONSOLIDATION=turnOff
     * Benchmark                     Mode  Cnt   Score    Error   Units
     * JMHBenchmarkClient.send1024  thrpt    3  37.769 ± 31.279  ops/ms
     * JMHBenchmarkClient.send128   thrpt    3  44.074 ± 32.570  ops/ms
     */

    public static final int     CONCURRENCY = 32;

    private static final byte[] BYTES_128   = new byte[128];
    private static final byte[] BYTES_1024  = new byte[1024];

    static {
        new Random().nextBytes(BYTES_128);
        new Random().nextBytes(BYTES_1024);
    }

    private RpcClient           client;

    @Setup
    public void setup() {
        System.setProperty("bolt.netty.buffer.high.watermark", String.valueOf(64 * 1024 * 1024));
        System.setProperty("bolt.netty.buffer.low.watermark", String.valueOf(32 * 1024 * 1024));
        client = new RpcClient();
        client.switches().turnOn(GlobalSwitch.CODEC_FLUSH_CONSOLIDATION);
        client.startup();
    }

    @TearDown
    public void tearDown() {
        client.shutdown();
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void send128() throws RemotingException, InterruptedException {
        client.invokeSync("127.0.0.1:18090", new Request<byte[]>(BYTES_128), 5000);
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void send1024() throws RemotingException, InterruptedException {
        client.invokeSync("127.0.0.1:18090", new Request<byte[]>(BYTES_1024), 5000);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()//
            .include(JMHBenchmarkClient.class.getSimpleName())//
            .warmupIterations(3)//
            .warmupTime(TimeValue.seconds(10))//
            .measurementIterations(3)//
            .measurementTime(TimeValue.seconds(10))//
            .threads(CONCURRENCY)//
            .forks(1)//
            .build();

        new Runner(opt).run();
    }
}
