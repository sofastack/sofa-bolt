package com.alipay.remoting.simpledemo;

import com.alipay.remoting.AsyncContext;
import com.alipay.remoting.BizContext;
import com.alipay.remoting.rpc.protocol.AsyncUserProcessor;

public class SimpleUserProcessor extends AsyncUserProcessor<SimpleRequest> {

    @Override
    public void handleRequest(BizContext bizCtx, AsyncContext asyncCtx, SimpleRequest request) {
        int res = request.getNum() * request.getNum();
        asyncCtx.sendResponse(new SimpleResponse(res));
    }

    @Override
    public String interest() {
        return SimpleRequest.class.getName();
    }
}
