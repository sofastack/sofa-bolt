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
package com.alipay.remoting.util;

import com.alipay.remoting.Connection;
import com.alipay.remoting.InvokeFuture;

import io.netty.channel.Channel;
import io.netty.util.Attribute;

/**
 * connection util
 * 
 * @author yunliang.shi
 * @version $Id: ConnectionUtil.java, v 0.1 Mar 10, 2016 11:36:40 AM yunliang.shi Exp $
 */
public class ConnectionUtil {

    public static Connection getConnectionFromChannel(Channel channel) {
        if (channel == null) {
            return null;
        }

        Attribute<Connection> connAttr = channel.attr(Connection.CONNECTION);
        if (connAttr != null) {
            Connection connection = connAttr.get();
            return connection;
        }
        return null;
    }

    public static void addIdPoolKeyMapping(Integer id, String group, Channel channel) {
        Connection connection = getConnectionFromChannel(channel);
        if (connection != null) {
            connection.addIdPoolKeyMapping(id, group);
        }
    }

    public static String removeIdPoolKeyMapping(Integer id, Channel channel) {
        Connection connection = getConnectionFromChannel(channel);
        if (connection != null) {
            return connection.removeIdPoolKeyMapping(id);
        }

        return null;
    }

    public static void addIdGroupCallbackMapping(Integer id, InvokeFuture callback, Channel channel) {
        Connection connection = getConnectionFromChannel(channel);
        if (connection != null) {
            connection.addInvokeFuture(callback);
        }
    }

    public static InvokeFuture removeIdGroupCallbackMapping(Integer id, Channel channel) {
        Connection connection = getConnectionFromChannel(channel);
        if (connection != null) {
            return connection.removeInvokeFuture(id);
        }
        return null;
    }

    public static InvokeFuture getGroupRequestCallBack(Integer id, Channel channel) {
        Connection connection = getConnectionFromChannel(channel);
        if (connection != null) {
            return connection.getInvokeFuture(id);
        }

        return null;
    }
}
