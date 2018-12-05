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
package com.alipay.remoting.log;

import java.io.File;

import org.slf4j.Logger;

import com.alipay.remoting.util.StringUtils;
import com.alipay.sofa.common.log.LoggerSpaceManager;

/**
 * Customized logger factory
 *
 * This can use middleware-log in sofa-common-tools to detect specific log implementation and initialize with the given log template.
 *
 * @author tsui
 * @version $Id: BoltLoggerFactory.java, v 0.1 2017-09-05 16:06 tsui Exp $
 */
public class BoltLoggerFactory {
    public static final String  BOLT_LOG_SPACE_PROPERTY   = "bolt.log.space";

    private static String       BOLT_LOG_SPACE            = "com.alipay.remoting";

    private static final String LOG_PATH                  = "logging.path";
    private static final String LOG_PATH_DEFAULT          = System.getProperty("user.home")
                                                            + File.separator + "logs";
    private static final String CLIENT_LOG_LEVEL          = "com.alipay.remoting.client.log.level";
    private static final String CLIENT_LOG_LEVEL_DEFAULT  = "INFO";
    private static final String CLIENT_LOG_ENCODE         = "com.alipay.remoting.client.log.encode";
    private static final String COMMON_ENCODE             = "file.encoding";
    private static final String CLIENT_LOG_ENCODE_DEFAULT = "UTF-8";

    static {
        String logSpace = System.getProperty(BOLT_LOG_SPACE_PROPERTY);
        if (null != logSpace && !logSpace.isEmpty()) {
            BOLT_LOG_SPACE = logSpace;
        }

        String logPath = System.getProperty(LOG_PATH);
        if (StringUtils.isBlank(logPath)) {
            System.setProperty(LOG_PATH, LOG_PATH_DEFAULT);
        }

        String logLevel = System.getProperty(CLIENT_LOG_LEVEL);
        if (StringUtils.isBlank(logLevel)) {
            System.setProperty(CLIENT_LOG_LEVEL, CLIENT_LOG_LEVEL_DEFAULT);
        }

        String commonEncode = System.getProperty(COMMON_ENCODE);
        if (StringUtils.isNotBlank(commonEncode)) {
            System.setProperty(CLIENT_LOG_ENCODE, commonEncode);
        } else {
            String logEncode = System.getProperty(CLIENT_LOG_ENCODE);
            if (StringUtils.isBlank(logEncode)) {
                System.setProperty(CLIENT_LOG_ENCODE, CLIENT_LOG_ENCODE_DEFAULT);
            }
        }
    }

    public static Logger getLogger(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }
        return getLogger(clazz.getCanonicalName());
    }

    public static Logger getLogger(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        return LoggerSpaceManager.getLoggerBySpace(name, BOLT_LOG_SPACE);
    }
}