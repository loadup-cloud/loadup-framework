package com.github.loadup.components.gateway.core.communication.http.util;

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

import com.github.loadup.components.gateway.core.communication.http.model.HttpProperty;
import com.github.loadup.components.gateway.core.model.CommunicationConfig;
import com.github.loadup.components.gateway.core.prototype.util.ExceptionUtil;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * HTTP组件配置工具类
 */
public final class HttpConfigUtil {

    /**
     * 禁用构造函数
     */
    private HttpConfigUtil() {
        // 禁用构造函数
    }

    /**
     * 获取http类型
     */
    public static String getHttpType(CommunicationConfig config) {
        return config.getProperty(HttpProperty.HTTP_TYPE);
    }

    /**
     * 是否需要http状态码
     */
    public static boolean needHttpStatusCode(CommunicationConfig config) {
        String isNeedHttpStatusCode = config.getProperty(HttpProperty.NEEDS_HTTP_STATUS_CODE);
        return Boolean.parseBoolean(isNeedHttpStatusCode);
    }

    /**
     * 是否短连接
     */
    public static boolean isHttpShortConnection(CommunicationConfig config) {
        String isHttpShortConnection = config.getProperty(HttpProperty.IS_HTTP_SHORT_CONNECTION);
        return Boolean.parseBoolean(isHttpShortConnection);
    }

    /**
     * 获取https协议模式
     */
    public static String getHttpsSchema(CommunicationConfig config) {
        return config.getProperty(HttpProperty.HTTPS_SCHEMA);
    }

    /**
     * 获取https协议
     */
    public static boolean isRedirect(CommunicationConfig config) {
        String isRedirect = config.getProperty(HttpProperty.IS_REDIRECT);
        return Boolean.parseBoolean(isRedirect);
    }

    /**
     * 获取https跳转错误页面地址
     */
    public static String getErrorRedirectURL(CommunicationConfig config) {
        return config.getProperty(HttpProperty.ERROR_REDIRECT_URL);
    }

    /**
     * 获取URL路径，非法路径会返回null，调用方判断
     */
    public static String getHttpKey(String url) {

        try {
            URL uri = new URL(url);
            return uri.getPath();
        } catch (MalformedURLException e) {
            ExceptionUtil.caught(e, "invalid HTTP URL,url=", url);
        }

        return null;
    }

    /**
     * 获取闲置超时时间
     */
    public static long getIdelTimeout(CommunicationConfig config) {
        String timeout_str = config.getProperty(HttpProperty.IDEL_TIMEOUT);
        try {
            return Long.valueOf(timeout_str);
        } catch (Exception e) {
            ExceptionUtil.caught(e, "invalid IdelTimeout,", timeout_str);
            return 0;
        }
    }

    /**
     * 是否restful
     */
    public static boolean isRest(CommunicationConfig config) {
        String isRest = config.getProperty(HttpProperty.IS_REST);
        return Boolean.parseBoolean(isRest);
    }

    /**
     * 是否需要httpHeader
     */
    public static boolean needReqHeader(CommunicationConfig config) {
        String needHeader = config.getProperty(HttpProperty.NEED_REQ_HEADER);
        return Boolean.parseBoolean(needHeader);
    }

    /**
     * need to check SSL client cert info
     */
    public static boolean needCheckSSLClient(CommunicationConfig config) {
        String needMutualTLS = config.getProperty(HttpProperty.NEED_CHECK_SSL_CLIENT);
        return Boolean.parseBoolean(needMutualTLS);
    }
}
