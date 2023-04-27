package com.alipay.remoting.rpc.common;

import com.alipay.remoting.BizContext;
import com.alipay.remoting.rpc.protocol.SyncUserProcessor;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class FifoServerUserProcessor extends SyncUserProcessor<RequestBody> {
    //key point: create a single thread pool to fifo process request
    private Executor executor = Executors.newSingleThreadExecutor(r -> new Thread(r, "FifoThread"));
    private Integer previousOrder = null;

    @Override
    public Object handleRequest(BizContext bizCtx, RequestBody request) throws Exception {
        System.out.println("thread[" + Thread.currentThread().getName() + "] Request received:" + request + ", arriveTimestamp:" + bizCtx.getArriveTimestamp());
        Integer currentOrder = request.getId();
        if (previousOrder != null) {
            if (currentOrder - previousOrder != 1) {
                System.out.println("error: not in fifo");
            }
        }
        previousOrder = currentOrder;
        return RequestBody.DEFAULT_SERVER_RETURN_STR;
    }

    @Override
    public Executor getExecutor() {
        return executor;
    }

    @Override
    public String interest() {
        return RequestBody.class.getName();
    }
}
