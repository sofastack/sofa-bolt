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

import java.lang.ref.SoftReference;
import java.util.Properties;

import com.alipay.remoting.RemotingAddressParser;
import com.alipay.remoting.Url;
import com.alipay.remoting.config.Configs;
import com.alipay.remoting.rpc.protocol.RpcProtocol;
import com.alipay.remoting.rpc.protocol.RpcProtocolV2;
import com.alipay.remoting.util.StringUtils;

/**
 * This is address parser for RPC.
 * <h3>Normal format</h3>
 * <pre>host:port?paramkey1=paramvalue1&amp;paramkey2=paramvalue2</pre>
 * 
 * <h4>Normal format example</h4>
 * <pre>127.0.0.1:12200?KEY1=VALUE1&KEY2=VALUE2</pre>
 * 
 * <h4>Illegal format example</h4>
 * <pre>
 * 127.0.0.1
 * 127.0.0.1:
 * 127.0.0.1:12200?
 * 127.0.0.1:12200?key1=
 * 127.0.0.1:12200?key1=value1&
 * </pre>
 * 
 * @author xiaomin.cxm
 * @version $Id: RpcAddressParser.java, v 0.1 Mar 11, 2016 5:56:45 PM xiaomin.cxm Exp $
 */
public class RpcAddressParser implements RemotingAddressParser {

    /**
     * @see com.alipay.remoting.RemotingAddressParser#parse(java.lang.String)
     */
    @Override
    public Url parse(String url) {
        if (StringUtils.isBlank(url)) {
            throw new IllegalArgumentException("Illegal format address string [" + url
                                               + "], should not be blank! ");
        }
        Url parsedUrl = this.tryGet(url);
        if (null != parsedUrl) {
            return parsedUrl;
        }
        String ip = null;
        String port = null;
        Properties properties = null;

        int size = url.length();
        int pos = 0;
        for (int i = 0; i < size; ++i) {
            if (COLON == url.charAt(i)) {
                ip = url.substring(pos, i);
                pos = i;
                // should not end with COLON
                if (i == size - 1) {
                    throw new IllegalArgumentException("Illegal format address string [" + url
                                                       + "], should not end with COLON[:]! ");
                }
                break;
            }
            // must have one COLON
            if (i == size - 1) {
                throw new IllegalArgumentException("Illegal format address string [" + url
                                                   + "], must have one COLON[:]! ");
            }
        }

        for (int i = pos; i < size; ++i) {
            if (QUES == url.charAt(i)) {
                port = url.substring(pos + 1, i);
                pos = i;
                if (i == size - 1) {
                    // should not end with QUES
                    throw new IllegalArgumentException("Illegal format address string [" + url
                                                       + "], should not end with QUES[?]! ");
                }
                break;
            }
            // end without a QUES
            if (i == size - 1) {
                port = url.substring(pos + 1, i + 1);
                pos = size;
            }
        }

        if (pos < (size - 1)) {
            properties = new Properties();
            while (pos < (size - 1)) {
                String key = null;
                String value = null;
                for (int i = pos; i < size; ++i) {
                    if (EQUAL == url.charAt(i)) {
                        key = url.substring(pos + 1, i);
                        pos = i;
                        if (i == size - 1) {
                            // should not end with EQUAL
                            throw new IllegalArgumentException(
                                "Illegal format address string [" + url
                                        + "], should not end with EQUAL[=]! ");
                        }
                        break;
                    }
                    if (i == size - 1) {
                        // must have one EQUAL
                        throw new IllegalArgumentException("Illegal format address string [" + url
                                                           + "], must have one EQUAL[=]! ");
                    }
                }
                for (int i = pos; i < size; ++i) {
                    if (AND == url.charAt(i)) {
                        value = url.substring(pos + 1, i);
                        pos = i;
                        if (i == size - 1) {
                            // should not end with AND
                            throw new IllegalArgumentException("Illegal format address string ["
                                                               + url
                                                               + "], should not end with AND[&]! ");
                        }
                        break;
                    }
                    // end without more AND
                    if (i == size - 1) {
                        value = url.substring(pos + 1, i + 1);
                        pos = size;
                    }
                }
                properties.put(key, value);
            }
        }
        parsedUrl = new Url(url, ip, Integer.parseInt(port), properties);
        this.initUrlArgs(parsedUrl);
        Url.parsedUrls.put(url, new SoftReference<Url>(parsedUrl));
        return parsedUrl;
    }

    /**
     * @see com.alipay.remoting.RemotingAddressParser#parseUniqueKey(java.lang.String)
     */
    @Override
    public String parseUniqueKey(String url) {
        boolean illegal = false;
        if (StringUtils.isBlank(url)) {
            illegal = true;
        }

        String uniqueKey = StringUtils.EMPTY;
        String addr = url.trim();
        String[] sectors = StringUtils.split(addr, QUES);
        if (!illegal && sectors.length == 2 && StringUtils.isNotBlank(sectors[0])) {
            uniqueKey = sectors[0].trim();
        } else {
            illegal = true;
        }

        if (illegal) {
            throw new IllegalArgumentException("Illegal format address string: " + url);
        }
        return uniqueKey;
    }

    /**
     * @see com.alipay.remoting.RemotingAddressParser#parseProperty(java.lang.String, java.lang.String)
     */
    @Override
    public String parseProperty(String addr, String propKey) {
        if (addr.contains("?") && !addr.endsWith("?")) {
            String part = addr.split("\\?")[1];
            for (String item : part.split("&")) {
                String[] kv = item.split("=");
                String k = kv[0];
                if (k.equals(propKey)) {
                    return kv[1];
                }
            }
        }
        return null;
    }

    /**
     * @see com.alipay.remoting.RemotingAddressParser#initUrlArgs(Url)
     */
    @Override
    public void initUrlArgs(Url url) {
        String connTimeoutStr = url.getProperty(RpcConfigs.CONNECT_TIMEOUT_KEY);
        int connTimeout = Configs.DEFAULT_CONNECT_TIMEOUT;
        if (StringUtils.isNotBlank(connTimeoutStr)) {
            if (StringUtils.isNumeric(connTimeoutStr)) {
                connTimeout = Integer.parseInt(connTimeoutStr);
            } else {
                throw new IllegalArgumentException(
                    "Url args illegal value of key [" + RpcConfigs.CONNECT_TIMEOUT_KEY
                            + "] must be positive integer! The origin url is ["
                            + url.getOriginUrl() + "]");
            }
        }
        url.setConnectTimeout(connTimeout);

        String protocolStr = url.getProperty(RpcConfigs.URL_PROTOCOL);
        byte protocol = RpcProtocol.PROTOCOL_CODE;
        if (StringUtils.isNotBlank(protocolStr)) {
            if (StringUtils.isNumeric(protocolStr)) {
                protocol = Byte.parseByte(protocolStr);
            } else {
                throw new IllegalArgumentException(
                    "Url args illegal value of key [" + RpcConfigs.URL_PROTOCOL
                            + "] must be positive integer! The origin url is ["
                            + url.getOriginUrl() + "]");
            }
        }
        url.setProtocol(protocol);

        String versionStr = url.getProperty(RpcConfigs.URL_VERSION);
        byte version = RpcProtocolV2.PROTOCOL_VERSION_1;
        if (StringUtils.isNotBlank(versionStr)) {
            if (StringUtils.isNumeric(versionStr)) {
                version = Byte.parseByte(versionStr);
            } else {
                throw new IllegalArgumentException(
                    "Url args illegal value of key [" + RpcConfigs.URL_VERSION
                            + "] must be positive integer! The origin url is ["
                            + url.getOriginUrl() + "]");
            }
        }
        url.setVersion(version);

        String connNumStr = url.getProperty(RpcConfigs.CONNECTION_NUM_KEY);
        int connNum = Configs.DEFAULT_CONN_NUM_PER_URL;
        if (StringUtils.isNotBlank(connNumStr)) {
            if (StringUtils.isNumeric(connNumStr)) {
                connNum = Integer.parseInt(connNumStr);
            } else {
                throw new IllegalArgumentException(
                    "Url args illegal value of key [" + RpcConfigs.CONNECTION_NUM_KEY
                            + "] must be positive integer! The origin url is ["
                            + url.getOriginUrl() + "]");
            }
        }
        url.setConnNum(connNum);

        String connWarmupStr = url.getProperty(RpcConfigs.CONNECTION_WARMUP_KEY);
        boolean connWarmup = false;
        if (StringUtils.isNotBlank(connWarmupStr)) {
            connWarmup = Boolean.parseBoolean(connWarmupStr);
        }
        url.setConnWarmup(connWarmup);
    }

    /**
     * try get from cache
     * 
     * @param url
     * @return
     */
    private Url tryGet(String url) {
        SoftReference<Url> softRef = Url.parsedUrls.get(url);
        return (null == softRef) ? null : softRef.get();
    }
}