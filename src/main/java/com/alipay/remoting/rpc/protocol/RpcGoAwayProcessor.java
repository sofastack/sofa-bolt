package com.alipay.remoting.rpc.protocol;

import com.alipay.remoting.AbstractRemotingProcessor;
import com.alipay.remoting.RemotingCommand;
import com.alipay.remoting.RemotingContext;
import com.alipay.remoting.rpc.GoAwayCommand;

public class RpcGoAwayProcessor extends AbstractRemotingProcessor {
    @Override
    public void doProcess(RemotingContext ctx, RemotingCommand msg) throws Exception {
        if (msg instanceof GoAwayCommand) {// process the goAway
            ctx.getConnection().setGoAway();
            if (ctx.getConnection().isInvokeFutureMapFinish()){
                ctx.getConnection().close();
            }
        }
    }
}
