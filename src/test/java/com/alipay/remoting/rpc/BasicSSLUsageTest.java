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

import static org.junit.Assert.assertFalse;
import java.io.File;

public class BasicSSLUsageTest extends BasicUsageTest {

    private static final String KEYSTORE_TYPE = "pkcs12";
    private static final String PASSWORD      = "sfbolt";

    @Override
    public void init() {
        // Setup SSL settings

        // For server
        System.setProperty(RpcConfigs.SRV_SSL_ENABLE, "true");
        System.setProperty(RpcConfigs.SRV_SSL_KEYSTORE, getResourceFile("bolt.pfx")
            .getAbsolutePath());
        System.setProperty(RpcConfigs.SRV_SSL_KEYSTORE_PASS, PASSWORD);
        System.setProperty(RpcConfigs.SRV_SSL_KEYTSTORE_YPE, KEYSTORE_TYPE);
        // For client
        System.setProperty(RpcConfigs.CLI_SSL_ENABLE, "true");
        System.setProperty(RpcConfigs.CLI_SSL_KEYSTORE, getResourceFile("cbolt.pfx")
            .getAbsolutePath());
        System.setProperty(RpcConfigs.CLI_SSL_KEYSTORE_PASS, PASSWORD);
        System.setProperty(RpcConfigs.CLI_SSL_KEYTSTORE_YPE, KEYSTORE_TYPE);

        super.init();
    }

    private File getResourceFile(String name) {
        ClassLoader classLoader = getClass().getClassLoader();
        return new File(classLoader.getResource(name).getFile());
    }

    @Override
    public void stop() {
        super.stop();
        // Clear SSL settings
        System.setProperty(RpcConfigs.SRV_SSL_ENABLE, "false");
        System.setProperty(RpcConfigs.SRV_SSL_NEED_CLIENT_AUTH, "false");
        System.setProperty(RpcConfigs.CLI_SSL_ENABLE, "false");
        assertFalse(RpcConfigManager.server_ssl_enable());
        assertFalse(RpcConfigManager.client_ssl_enable());
    }

}
