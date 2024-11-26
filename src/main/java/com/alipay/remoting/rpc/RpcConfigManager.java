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

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import com.alipay.remoting.config.ConfigManager;

/**
 * RPC framework config manager.
 *
 * @author dennis
 */
public class RpcConfigManager {
    public static boolean dispatch_msg_list_in_default_executor() {
        return ConfigManager.getBool(RpcConfigs.DISPATCH_MSG_LIST_IN_DEFAULT_EXECUTOR,
            RpcConfigs.DISPATCH_MSG_LIST_IN_DEFAULT_EXECUTOR_DEFAULT);
    }

    @Deprecated
    public static boolean server_ssl_enable() {
        return ConfigManager.getBool(RpcConfigs.SRV_SSL_ENABLE, "false");
    }

    @Deprecated
    public static boolean server_ssl_need_client_auth() {
        return ConfigManager.getBool(RpcConfigs.SRV_SSL_NEED_CLIENT_AUTH, "false");
    }

    @Deprecated
    public static String server_ssl_keystore() {
        return ConfigManager.getString(RpcConfigs.SRV_SSL_KEYSTORE, null);
    }

    @Deprecated
    public static String server_ssl_keystore_pass() {
        return ConfigManager.getString(RpcConfigs.SRV_SSL_KEYSTORE_PASS, null);
    }

    @Deprecated
    public static String server_ssl_keystore_type() {
        return ConfigManager.getString(RpcConfigs.SRV_SSL_KEYTSTORE_YPE, null);
    }

    @Deprecated
    public static String server_ssl_kmf_algorithm() {
        return ConfigManager.getString(RpcConfigs.SRV_SSL_KMF_ALGO,
            KeyManagerFactory.getDefaultAlgorithm());
    }

    @Deprecated
    public static boolean client_ssl_enable() {
        return ConfigManager.getBool(RpcConfigs.CLI_SSL_ENABLE, "false");
    }

    @Deprecated
    public static String client_ssl_keystore() {
        return ConfigManager.getString(RpcConfigs.CLI_SSL_KEYSTORE, null);
    }

    @Deprecated
    public static String client_ssl_keystore_pass() {
        return ConfigManager.getString(RpcConfigs.CLI_SSL_KEYSTORE_PASS, null);
    }

    @Deprecated
    public static String client_ssl_keystore_type() {
        return ConfigManager.getString(RpcConfigs.CLI_SSL_KEYTSTORE_YPE, null);
    }

    @Deprecated
    public static String client_ssl_tmf_algorithm() {
        return ConfigManager.getString(RpcConfigs.CLI_SSL_TMF_ALGO,
            TrustManagerFactory.getDefaultAlgorithm());
    }
}
