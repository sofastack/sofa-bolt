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
     * Benchmark                                       Mode      Cnt   Score    Error   Units
     * JMHBenchmarkClient.send1024                    thrpt        3  46.887 ± 22.766  ops/ms
     * JMHBenchmarkClient.send128                     thrpt        3  66.376 ± 28.429  ops/ms
     * JMHBenchmarkClient.send1024                     avgt        3   0.674 ±  0.986   ms/op
     * JMHBenchmarkClient.send128                      avgt        3   0.479 ±  0.239   ms/op
     * JMHBenchmarkClient.send1024                   sample  1525751   0.629 ±  0.001   ms/op
     * JMHBenchmarkClient.send1024:send1024·p0.00    sample            0.150            ms/op
     * JMHBenchmarkClient.send1024:send1024·p0.50    sample            0.602            ms/op
     * JMHBenchmarkClient.send1024:send1024·p0.90    sample            0.775            ms/op
     * JMHBenchmarkClient.send1024:send1024·p0.95    sample            0.854            ms/op
     * JMHBenchmarkClient.send1024:send1024·p0.99    sample            1.614            ms/op
     * JMHBenchmarkClient.send1024:send1024·p0.999   sample            2.249            ms/op
     * JMHBenchmarkClient.send1024:send1024·p0.9999  sample            4.280            ms/op
     * JMHBenchmarkClient.send1024:send1024·p1.00    sample            6.431            ms/op
     * JMHBenchmarkClient.send128                    sample  1913189   0.501 ±  0.001   ms/op
     * JMHBenchmarkClient.send128:send128·p0.00      sample            0.150            ms/op
     * JMHBenchmarkClient.send128:send128·p0.50      sample            0.481            ms/op
     * JMHBenchmarkClient.send128:send128·p0.90      sample            0.618            ms/op
     * JMHBenchmarkClient.send128:send128·p0.95      sample            0.688            ms/op
     * JMHBenchmarkClient.send128:send128·p0.99      sample            1.190            ms/op
     * JMHBenchmarkClient.send128:send128·p0.999     sample            2.097            ms/op
     * JMHBenchmarkClient.send128:send128·p0.9999    sample            4.391            ms/op
     * JMHBenchmarkClient.send128:send128·p1.00      sample            7.053            ms/op
     * JMHBenchmarkClient.send1024                       ss        3   5.179 ± 18.142   ms/op
     * JMHBenchmarkClient.send128                        ss        3   3.788 ± 10.388   ms/op
     *
     * CODEC_FLUSH_CONSOLIDATION=turnOff
     * Benchmark                                       Mode      Cnt   Score    Error   Units
     * JMHBenchmarkClient.send1024                    thrpt        3  35.946 ± 10.332  ops/ms
     * JMHBenchmarkClient.send128                     thrpt        3  41.364 ± 36.002  ops/ms
     * JMHBenchmarkClient.send1024                     avgt        3   0.846 ±  0.272   ms/op
     * JMHBenchmarkClient.send128                      avgt        3   0.731 ±  0.306   ms/op
     * JMHBenchmarkClient.send1024                   sample  1003714   0.956 ±  0.001   ms/op
     * JMHBenchmarkClient.send1024:send1024·p0.00    sample            0.183            ms/op
     * JMHBenchmarkClient.send1024:send1024·p0.50    sample            0.886            ms/op
     * JMHBenchmarkClient.send1024:send1024·p0.90    sample            1.294            ms/op
     * JMHBenchmarkClient.send1024:send1024·p0.95    sample            1.495            ms/op
     * JMHBenchmarkClient.send1024:send1024·p0.99    sample            2.241            ms/op
     * JMHBenchmarkClient.send1024:send1024·p0.999   sample            3.977            ms/op
     * JMHBenchmarkClient.send1024:send1024·p0.9999  sample            8.448            ms/op
     * JMHBenchmarkClient.send1024:send1024·p1.00    sample           21.660            ms/op
     * JMHBenchmarkClient.send128                    sample  1346879   0.712 ±  0.001   ms/op
     * JMHBenchmarkClient.send128:send128·p0.00      sample            0.124            ms/op
     * JMHBenchmarkClient.send128:send128·p0.50      sample            0.684            ms/op
     * JMHBenchmarkClient.send128:send128·p0.90      sample            0.927            ms/op
     * JMHBenchmarkClient.send128:send128·p0.95      sample            1.016            ms/op
     * JMHBenchmarkClient.send128:send128·p0.99      sample            1.571            ms/op
     * JMHBenchmarkClient.send128:send128·p0.999     sample            2.322            ms/op
     * JMHBenchmarkClient.send128:send128·p0.9999    sample            4.375            ms/op
     * JMHBenchmarkClient.send128:send128·p1.00      sample            5.865            ms/op
     * JMHBenchmarkClient.send1024                       ss        3   5.932 ± 13.160   ms/op
     * JMHBenchmarkClient.send128                        ss        3   5.028 ± 10.273   ms/op
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
    @BenchmarkMode(Mode.All)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void send128() throws RemotingException, InterruptedException {
        client.invokeSync("127.0.0.1:18090", new Request<byte[]>(BYTES_128), 5000);
    }

    @Benchmark
    @BenchmarkMode(Mode.All)
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
