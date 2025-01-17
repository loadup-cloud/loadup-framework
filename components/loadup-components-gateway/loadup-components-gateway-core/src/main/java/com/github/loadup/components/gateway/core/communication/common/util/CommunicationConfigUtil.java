package com.github.loadup.components.gateway.core.communication.common.util;

/*-
 * #%L
 * loadup-components-gateway-core
 * %%
 * Copyright (C) 2022 - 2025 loadup_cloud
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import com.github.loadup.components.gateway.core.communication.common.model.CommunicationProperty;
import com.github.loadup.components.gateway.core.model.CommunicationConfig;

/**
 * HTTP组件配置工具类
 */
public final class CommunicationConfigUtil {

    /**
     * 禁用构造函数
     */
    private CommunicationConfigUtil() {
        // 禁用构造函数
    }

    /**
     * 获取发送字符集
     */
    public static String getSendCharset(CommunicationConfig config) {
        return config.getProperty(CommunicationProperty.SEND_CHARSET);
    }

    /**
     * 获取接收字符集
     */
    public static String getRecvCharset(CommunicationConfig config) {
        return config.getProperty(CommunicationProperty.RECV_CHARSET);
    }

    /**
     * 获取连接池大小
     */
    public static int getConnectionPoolSize(CommunicationConfig config) {
        String size = config.getProperty(CommunicationProperty.CONNECTION_POOL_SIZE);
        return Integer.parseInt(size);
    }

    /**
     * 获取发送缓冲大小
     */
    public static int getSendBufferSize(CommunicationConfig config) {
        String size = config.getProperty(CommunicationProperty.SEND_BUFFER_SIZE);
        return Integer.parseInt(size);
    }

    /**
     * 获取接收缓冲大小
     */
    public static int getReceiveBufferSize(CommunicationConfig config) {
        String size = config.getProperty(CommunicationProperty.RECV_BUFFER_SIZE);
        return Integer.parseInt(size);
    }

    /**
     * 是否需要响应
     */
    public static boolean isNeedResponse(CommunicationConfig config) {
        String isNeedResponse = config.getProperty(CommunicationProperty.NEEDS_RESPONSE);
        return Boolean.parseBoolean(isNeedResponse);
    }

    /**
     * 是否敏感
     */
    public static boolean isSensitive(CommunicationConfig config) {
        String isSensitive = config.getProperty(CommunicationProperty.IS_SENSITIVE);
        return Boolean.parseBoolean(isSensitive);
    }

    /**
     * 获取https协议
     */
    public static String getSSLProtocal(CommunicationConfig config) {
        return config.getProperty(CommunicationProperty.SSL_PROTOCOL);
    }

    /**
     * get proxy name
     */
    public static String getProxyName(CommunicationConfig config) {
        return config.getProperty(CommunicationProperty.PROXY_NAME);
    }

    /**
     * get instproxy target communication id
     */
    public static String getInstproxyTargetId(CommunicationConfig config) {
        return config.getProperty(CommunicationProperty.INSTPROXY_TARGET_ID);
    }

    /**
     * 组装报文是否是base64格式
     */
    public static boolean isBase64Format(CommunicationConfig config) {
        String format = config.getProperty(CommunicationProperty.MESSAGE_CONTENT_FORMAT);
        return "BASE64".equals(format);
    }

    /**
     * 获取连接池大小
     */
    public static int getConnectionSizePerHost(CommunicationConfig config) {
        String size = config.getProperty(CommunicationProperty.CONNECTION_SIZE_PER_HOST);
        return Integer.parseInt(size);
    }
}
