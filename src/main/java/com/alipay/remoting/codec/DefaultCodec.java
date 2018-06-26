package com.alipay.remoting.codec;

import com.alipay.remoting.ProtocolCode;
import com.alipay.remoting.rpc.protocol.RpcProtocolDecoder;
import com.alipay.remoting.rpc.protocol.RpcProtocolManager;
import com.alipay.remoting.rpc.protocol.RpcProtocolV2;
import io.netty.channel.ChannelHandler;

/**
 * @author muyun.cyt
 * @version 2018/6/26 下午3:51
 */
public class DefaultCodec implements Codec {


    @Override
    public ChannelHandler newEncoder() {
        return new ProtocolCodeBasedEncoder(
                ProtocolCode.fromBytes(RpcProtocolV2.PROTOCOL_CODE));
    }

    @Override
    public ChannelHandler newDecoder() {
        return new RpcProtocolDecoder(RpcProtocolManager.DEFAULT_PROTOCOL_CODE_LENGTH);
    }
}
