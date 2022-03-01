package com.alipay.remoting.rpc;

import com.alipay.remoting.rpc.protocol.RpcRequestCommand;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


public class RpcCommandTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void setClazz_normal_len() {
        RpcCommand rpcCommand = new RpcRequestCommand();
        byte[] clazz = new byte[100];
        rpcCommand.setClazz(clazz);
        Assert.assertNotNull(rpcCommand.getClazz());
    }

    @Test
    public void setClazz_exceed_maximum(){
        RpcCommand rpcCommand = new RpcRequestCommand();
        byte[] clazz = new byte[Short.MAX_VALUE + 1];
        thrown.expect(RuntimeException.class);
        thrown.expectMessage("class length exceed maximum, len=" + clazz.length);
        rpcCommand.setClazz(clazz);
    }

    @Test
    public void setHeader_normal_len() {
        RpcCommand rpcCommand = new RpcRequestCommand();
        byte[] header = new byte[100];
        rpcCommand.setHeader(header);
        Assert.assertNotNull(rpcCommand.getHeader());
    }

    @Test
    public void setHeader_exceed_maximum(){
        RpcCommand rpcCommand = new RpcRequestCommand();
        byte[] header = new byte[Short.MAX_VALUE + 1];
        thrown.expect(RuntimeException.class);
        thrown.expectMessage("header length exceed maximum, len=" + header.length);
        rpcCommand.setHeader(header);
    }
}