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

import com.alipay.remoting.rpc.RpcCommandFactory;
import io.netty.channel.local.LocalChannel;
import org.junit.Test;

import static org.junit.Assert.assertSame;

public class BaseRemotingTest {

    @Test
    public void getCommandFactory() {
        RpcCommandFactory commandFactory = new RpcCommandFactory();
        BaseRemoting baseRemoting = new EmptyRemoting(commandFactory);
        assertSame(commandFactory, baseRemoting.getCommandFactory());
    }

    @Test
    public void getCommandFactoryFromProtocolCode() {
        RpcCommandFactory defaultCommand = new RpcCommandFactory();
        BaseRemoting baseRemoting = new EmptyRemoting(defaultCommand);

        // no 3a protocol
        CommandFactory commandFactory = baseRemoting.getCommandFactory(ProtocolCode
            .fromBytes((byte) 0x3a));
        assertSame(defaultCommand, commandFactory);

        // register 3a protocol
        RpcCommandFactory my3aCommandFactory = new RpcCommandFactory();
        ProtocolManager.registerProtocol(new MyProtocol(my3aCommandFactory), (byte) 0x3a);
        // get 3a
        commandFactory = baseRemoting.getCommandFactory(ProtocolCode.fromBytes((byte) 0x3a));
        assertSame(my3aCommandFactory, commandFactory);

        ProtocolManager.unRegisterProtocol((byte) 0x3a);
    }

    @Test
    public void getCommandFactoryFromConnection() {
        RpcCommandFactory defaultCommand = new RpcCommandFactory();
        BaseRemoting baseRemoting = new EmptyRemoting(defaultCommand);

        Connection connection = new Connection(new LocalChannel());

        // no 3a protocol
        CommandFactory commandFactory = baseRemoting.getCommandFactory(connection);
        assertSame(defaultCommand, commandFactory);

        // register 3a protocol
        RpcCommandFactory my3aCommandFactory = new RpcCommandFactory();
        ProtocolManager.registerProtocol(new MyProtocol(my3aCommandFactory), (byte) 0x3a);
        connection.getChannel().attr(Connection.PROTOCOL).set(ProtocolCode.fromBytes((byte) 0x3a));
        // get 3a
        commandFactory = baseRemoting.getCommandFactory(connection);
        assertSame(my3aCommandFactory, commandFactory);

        ProtocolManager.unRegisterProtocol((byte) 0x3a);
    }

    static class EmptyRemoting extends BaseRemoting {

        public EmptyRemoting(CommandFactory commandFactory) {
            super(commandFactory);
        }

        @Override
        protected InvokeFuture createInvokeFuture(RemotingCommand request,
                                                  InvokeContext invokeContext) {
            return null;
        }

        @Override
        protected InvokeFuture createInvokeFuture(Connection conn, RemotingCommand request,
                                                  InvokeContext invokeContext,
                                                  InvokeCallback invokeCallback) {
            return null;
        }
    }

    static class MyProtocol implements Protocol {

        private CommandFactory commandFactory;

        public MyProtocol(CommandFactory commandFactory) {
            this.commandFactory = commandFactory;
        }

        @Override
        public CommandEncoder getEncoder() {
            return null;
        }

        @Override
        public CommandDecoder getDecoder() {
            return null;
        }

        @Override
        public HeartbeatTrigger getHeartbeatTrigger() {
            return null;
        }

        @Override
        public CommandHandler getCommandHandler() {
            return null;
        }

        @Override
        public CommandFactory getCommandFactory() {
            return commandFactory;
        }
    }

}