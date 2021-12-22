package com.alipay.remoting.rpc;

import com.alipay.remoting.RpcAddress;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * @author je
 * @Date 2021/12/13
 */
public class RpcAddressParserTest {

    private final List<String> ipv4s = Arrays.asList(
            "200.0.255.255:1044?name=key",
            "200.01.255.255:80?name=key",
            "200.01.255.255:80?name=key&age=11",
            "0.0.0.1:1414?name=1",
            "1.0.0.1:1414?name=1"
            );
    private final List<String> illegalIpv4s = Arrays.asList(
            "200.256.255.222:1414?name=key&age=",
            "200.256.255.222:1414?name=key?age=11",
            "200.256.255.222:1414?name=key:age=11",
            "200.255.256.255:1414?name=key",
            "_200.255.256.255:1414?name=key",
            "200.255.256.256:1414?name=key",
            "200.01.255.255:80?name=",
            "200.01.255.255?name=key",
            "200:1414?name=key",
            "200.1:1313?name=key?",
            "200.1:1313?=key?",
            "200.1?1313=key?");

    private final List<String> ipv6s = Arrays.asList(
            "[1:2:3:4:5:6:7::]:1313?name=key",
            "[1:2:3:4:5:6:7:8]:1313?name=key",
            "[1:2:3:4:5:6::]:1313?name=key",
            "[1:2:3:4:5:6::8]:1313?name=key",
            "[1:2:3:4:5::]:1313?name=key",
            "[1:2:3:4:5::8]:1313?name=key",
            "[fdbd:dc02:ff:1:9::1b]:1313?name=key",
            "[3ffe:2a00:100:7031::1]:1313?name=key&age=11",
            "[1::]:1313?name=key",
            "[1::8]:1313?name=key",
            "[::]:1313?name=key",
            "[::8]:1313?name=key",
            "[::5:6:7:8]:1313?name=key",
            "[A:0f:0F:FFFF:5:6:7:8]:1313?name=key"
    );
    private final List<String> illegalIpv6s = Arrays.asList(
            "[A:0f:0F:FFFF1:5:6:7:8]:1313?name=key",
            "[G:0f:0F:FFFF:5:6:7:8]:1313?name=key",
            "[200.255.256.256]:1313?name=key",
            "[::5:6:7:8]:1313?name",
            "[::5:6:7:8]?name=key",
            "[::5:6:7:8]?name=key?name=key?age=11",
            "[::5:6:7:8]:1313?name=key&",
            "[::5:6:7:8]:1313?name=");

    @Test
    public void parseV4() {
        for (String illegalIpv4 : illegalIpv4s) {
            try {
                //expect exception
                new RpcAddressParser().parse(illegalIpv4);
                throw new RuntimeException("ip [" + illegalIpv4 + "] is illegal, but no exception is thrown");
            } catch (IllegalArgumentException ignore) {
            }
        }
        for (String ipv4 : ipv4s) {
            RpcAddress.newRpcAddress(ipv4);
        }
    }

    @Test
    public void parseV6() {
        for (String illegalIpv6 : illegalIpv6s) {
            try {
                //expect exception
                new RpcAddressParser().parse(illegalIpv6);
                throw new RuntimeException("ip [" + illegalIpv6 + "] is illegal, but no exception is thrown");
            } catch (IllegalArgumentException ignore) {
            }
        }
        for (String ipv6 : ipv6s) {
            RpcAddress.newRpcAddress(ipv6);
        }
    }
}