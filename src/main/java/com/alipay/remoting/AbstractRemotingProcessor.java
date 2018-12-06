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
package com.alipay.remoting;

import com.alipay.remoting.log.BoltLoggerFactory;
import com.alipay.remoting.util.RemotingUtil;
import org.slf4j.Logger;

import java.util.concurrent.ExecutorService;

/**
 * Processor to process remoting command.
 *
 * @author jiangping
 * @version $Id: RemotingProcessor.java, v 0.1 2015-9-6 PM2:50:51 tao Exp $
 * @param <T>
 */
// TODO: 2018/4/24 by zmyer
public abstract class AbstractRemotingProcessor<T extends RemotingCommand> implements
                                                                           RemotingProcessor<T> {
    //日志
    private static final Logger logger = BoltLoggerFactory.getLogger("CommonDefault");
    //指令执行器
    private ExecutorService     executor;
    //指令工厂
    private CommandFactory      commandFactory;

    /**
     * Default constructor.
     */
    // TODO: 2018/4/24 by zmyer
    public AbstractRemotingProcessor() {

    }

    /**
     * Constructor.
     */
    // TODO: 2018/4/24 by zmyer
    public AbstractRemotingProcessor(CommandFactory commandFactory) {
        this.commandFactory = commandFactory;
    }

    /**
     * Constructor.
     * @param executor
     */
    // TODO: 2018/4/24 by zmyer
    public AbstractRemotingProcessor(ExecutorService executor) {
        this.executor = executor;
    }

    /**
     * Constructor.
     * @param executor
     */
    // TODO: 2018/4/24 by zmyer
    public AbstractRemotingProcessor(CommandFactory commandFactory, ExecutorService executor) {
        this.commandFactory = commandFactory;
        this.executor = executor;
    }

    /**
     * Do the process.
     *
     * @param ctx
     * @param msg
     * @throws Exception
     */
    // TODO: 2018/4/24 by zmyer
    public abstract void doProcess(RemotingContext ctx, T msg) throws Exception;

    /**
     * Process the remoting command with its own executor or with the defaultExecutor if its own if null.
     *
     * @param ctx
     * @param msg
     * @param defaultExecutor
     * @throws Exception
     */
    // TODO: 2018/4/24 by zmyer
    @Override
    public void process(RemotingContext ctx, T msg, ExecutorService defaultExecutor)
                                                                                    throws Exception {
        //封装处理任务对象
        ProcessTask task = new ProcessTask(ctx, msg);
        if (this.getExecutor() != null) {
            //将当前的任务对象放入执行线程池中，并执行
            this.getExecutor().execute(task);
        } else {
            //如果当前的处理器没有自带的执行器，则直接使用默认的处理器
            defaultExecutor.execute(task);
        }
    }

    /**
     * Getter method for property <tt>executor</tt>.
     *
     * @return property value of executor
     */
    // TODO: 2018/4/24 by zmyer
    public ExecutorService getExecutor() {
        return executor;
    }

    /**
     * Setter method for property <tt>executor</tt>.
     *
     * @param executor value to be assigned to property executor
     */
    // TODO: 2018/4/24 by zmyer
    public void setExecutor(ExecutorService executor) {
        this.executor = executor;
    }

    // TODO: 2018/4/24 by zmyer
    public CommandFactory getCommandFactory() {
        return commandFactory;
    }

    // TODO: 2018/4/24 by zmyer
    public void setCommandFactory(CommandFactory commandFactory) {
        this.commandFactory = commandFactory;
    }

    /**
     * Task for asynchronous process.
     *
     * @author jiangping
     * @version $Id: RemotingProcessor.java, v 0.1 2015-10-14 PM7:40:44 tao Exp $
     */
    // TODO: 2018/4/24 by zmyer
    class ProcessTask implements Runnable {
        //指令处理上下文对象
        RemotingContext ctx;
        //处理消息
        T               msg;

        // TODO: 2018/4/24 by zmyer
        public ProcessTask(RemotingContext ctx, T msg) {
            this.ctx = ctx;
            this.msg = msg;
        }

        /**
         * @see java.lang.Runnable#run()
         */
        // TODO: 2018/4/24 by zmyer
        @Override
        public void run() {
            try {
                //开始处理消息
                AbstractRemotingProcessor.this.doProcess(ctx, msg);
            } catch (Throwable e) {
                //protect the thread running this task
                String remotingAddress = RemotingUtil.parseRemoteAddress(ctx.getChannelContext()
                    .channel());
                logger
                    .error(
                        "Exception caught when process rpc request command in AbstractRemotingProcessor, Id="
                                + msg.getId() + "! Invoke source address is [" + remotingAddress
                                + "].", e);
            }
        }

    }

}
