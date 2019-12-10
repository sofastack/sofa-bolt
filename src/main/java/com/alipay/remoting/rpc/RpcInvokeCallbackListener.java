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
package com.alipay.remoting.rpc;

import java.util.concurrent.RejectedExecutionException;

import org.slf4j.Logger;

import com.alipay.remoting.InvokeCallback;
import com.alipay.remoting.InvokeCallbackListener;
import com.alipay.remoting.InvokeFuture;
import com.alipay.remoting.ResponseStatus;
import com.alipay.remoting.exception.CodecException;
import com.alipay.remoting.exception.ConnectionClosedException;
import com.alipay.remoting.log.BoltLoggerFactory;
import com.alipay.remoting.rpc.exception.InvokeException;
import com.alipay.remoting.rpc.exception.InvokeServerBusyException;
import com.alipay.remoting.rpc.exception.InvokeServerException;
import com.alipay.remoting.rpc.exception.InvokeTimeoutException;
import com.alipay.remoting.rpc.protocol.RpcResponseCommand;

/**
 * Listener which listens the Rpc invoke result, and then invokes the call back.
 * 
 * @author jiangping
 * @version $Id: RpcInvokeCallbackListener.java, v 0.1 2015-9-30 AM10:36:34 tao Exp $
 */
public class RpcInvokeCallbackListener implements InvokeCallbackListener {

    private static final Logger logger = BoltLoggerFactory.getLogger("RpcRemoting");

    private String              address;

    public RpcInvokeCallbackListener() {

    }

    public RpcInvokeCallbackListener(String address) {
        this.address = address;
    }

    /** 
     * @see com.alipay.remoting.InvokeCallbackListener#onResponse(com.alipay.remoting.InvokeFuture)
     */
    @Override
    public void onResponse(InvokeFuture future) {
        InvokeCallback callback = future.getInvokeCallback();
        if (callback != null) {
            CallbackTask task = new CallbackTask(this.getRemoteAddress(), future);
            if (callback.getExecutor() != null) {
                // There is no need to switch classloader, because executor is provided by user.
                try {
                    callback.getExecutor().execute(task);
                } catch (RejectedExecutionException e) {
                    logger.warn("Callback thread pool busy.");
                }
            } else {
                task.run();
            }
        }
    }

    class CallbackTask implements Runnable {

        InvokeFuture future;
        String       remoteAddress;

        /**
         * 
         */
        public CallbackTask(String remoteAddress, InvokeFuture future) {
            this.remoteAddress = remoteAddress;
            this.future = future;
        }

        /** 
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
            InvokeCallback callback = future.getInvokeCallback();
            // a lot of try-catches to protect thread pool
            ResponseCommand response = null;

            try {
                response = (ResponseCommand) future.waitResponse(0);
            } catch (InterruptedException e) {
                String msg = "Exception caught when getting response from InvokeFuture. The address is "
                             + this.remoteAddress;
                logger.error(msg, e);
            }
            if (response == null || response.getResponseStatus() != ResponseStatus.SUCCESS) {
                try {
                    Exception e;
                    if (response == null) {
                        e = new InvokeException("Exception caught in invocation. The address is "
                                                + this.remoteAddress + " responseStatus:"
                                                + ResponseStatus.UNKNOWN, future.getCause());
                    } else {
                        response.setInvokeContext(future.getInvokeContext());
                        switch (response.getResponseStatus()) {
                            case TIMEOUT:
                                e = new InvokeTimeoutException(
                                    "Invoke timeout when invoke with callback.The address is "
                                            + this.remoteAddress);
                                break;
                            case CONNECTION_CLOSED:
                                e = new ConnectionClosedException(
                                    "Connection closed when invoke with callback.The address is "
                                            + this.remoteAddress);
                                break;
                            case SERVER_THREADPOOL_BUSY:
                                e = new InvokeServerBusyException(
                                    "Server thread pool busy when invoke with callback.The address is "
                                            + this.remoteAddress);
                                break;
                            case SERVER_EXCEPTION:
                                String msg = "Server exception when invoke with callback.Please check the server log! The address is "
                                             + this.remoteAddress;
                                RpcResponseCommand resp = (RpcResponseCommand) response;
                                resp.deserialize();
                                Object ex = resp.getResponseObject();
                                if (ex != null && ex instanceof Throwable) {
                                    e = new InvokeServerException(msg, (Throwable) ex);
                                } else {
                                    e = new InvokeServerException(msg);
                                }
                                break;
                            default:
                                e = new InvokeException(
                                    "Exception caught in invocation. The address is "
                                            + this.remoteAddress + " responseStatus:"
                                            + response.getResponseStatus(), future.getCause());

                        }
                    }
                    callback.onException(e);
                } catch (Throwable e) {
                    logger
                        .error(
                            "Exception occurred in user defined InvokeCallback#onException() logic, The address is {}",
                            this.remoteAddress, e);
                }
            } else {
                ClassLoader oldClassLoader = null;
                try {
                    if (future.getAppClassLoader() != null) {
                        oldClassLoader = Thread.currentThread().getContextClassLoader();
                        Thread.currentThread().setContextClassLoader(future.getAppClassLoader());
                    }
                    response.setInvokeContext(future.getInvokeContext());
                    RpcResponseCommand rpcResponse = (RpcResponseCommand) response;
                    response.deserialize();
                    try {
                        callback.onResponse(rpcResponse.getResponseObject());
                    } catch (Throwable e) {
                        logger
                            .error(
                                "Exception occurred in user defined InvokeCallback#onResponse() logic.",
                                e);
                    }
                } catch (CodecException e) {
                    logger
                        .error(
                            "CodecException caught on when deserialize response in RpcInvokeCallbackListener. The address is {}.",
                            this.remoteAddress, e);
                } catch (Throwable e) {
                    logger.error(
                        "Exception caught in RpcInvokeCallbackListener. The address is {}",
                        this.remoteAddress, e);
                } finally {
                    if (oldClassLoader != null) {
                        Thread.currentThread().setContextClassLoader(oldClassLoader);
                    }
                }
            } // enf of else
        } // end of run
    }

    /** 
     * @see com.alipay.remoting.InvokeCallbackListener#getRemoteAddress()
     */
    @Override
    public String getRemoteAddress() {
        return this.address;
    }
}
