package com.alipay.remoting;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * test {@link RpcAddress} apis
 *
 * @author je
 * @Date 2021/12/13
 */
public class RpcAddressTest {

    private final List<String> ipv4s = Arrays.asList(
            "200.0.255.255:",
            "200.01.255.255:",
            "200.10.255.255:",
            "200.001.255.255:",
            "200.255.0.255:",
            "200.255.01.255:",
            "200.255.10.255:",
            "200.255.001.255:",
            "200.255.255.0:",
            "200.255.255.01:",
            "200.255.255.10:",
            "200.255.255.001:",
            "0.0.0.1:",
            "1.0.0.1:",
            "200.255.255.255:",
            "223.255.255.255:",
            "192.0.0.1:"
    );
    private final List<String> illegalIpv4s = Arrays.asList(
            "200.256.255.255:",
            "200.255.256.255:",
            "_200.255.256.255:",
            "200.255.256.256",
            "200",
            "200.1",
            "200.1.1",
            "200.1.1.1.1",
            "-.0.0.1");

    private final List<String> ipv6s = Arrays.asList(
            "[1:2:3:4:5:6:7::]:",
            "[1:2:3:4:5:6:7:8]:",
            "[1:2:3:4:5:6::]:",
            "[1:2:3:4:5:6::8]:",
            "[1:2:3:4:5::]:",
            "[1:2:3:4:5::8]:",
            "[3ffe:2a00:100:7031::1]:",
            "[1::]:",
            "[1::8]:",
            "[::]:",
            "[::8]:",
            "[::5:6:7:8]:",
            "[A:0f:0F:FFFF:5:6:7:8]:"
    );
    private final List<String> illegalIpv6s = Arrays.asList(
            "[A:0f:0F:FFFF1:5:6:7:8]:",
            "[G:0f:0F:FFFF:5:6:7:8]:",
            "[200.255.256.256]",
            "[-.0.0.1]");


    @Test
    public void testTryParseV4() {
        for (String illegalIpv4 : illegalIpv4s) {
            try {
                //expect exception
                RpcAddress.newRpcAddress(illegalIpv4);
                throw new RuntimeException("ip [" + illegalIpv4 + "] is illegal, but no exception is thrown");
            } catch (IllegalArgumentException ignore) {
            }
        }
        for (String ipv4 : ipv4s) {
            RpcAddress.newRpcAddress(ipv4);
        }
    }

    @Test
    public void testTryParseV6() {
        for (String illegalIpv6 : illegalIpv6s) {
            try {
                //expect exception
                RpcAddress.newRpcAddress(illegalIpv6);
                throw new RuntimeException("ip [" + illegalIpv6 + "] is illegal, but no exception is thrown");
            } catch (IllegalArgumentException ignore) {
            }
        }
        for (String ipv6 : ipv6s) {
            RpcAddress.newRpcAddress(ipv6);
        }
    }

}