package com.github.loadup.components.gateway.core.communication.http.model;

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

import com.github.loadup.components.gateway.core.common.enums.PropertyName;

/**
 * 通讯属性配置
 */
public enum HttpProperty implements PropertyName {

    /**
     * http请求类型
     **/
    HTTP_TYPE("HttpMethod类型", "post"),

    /**
     * 是否需要http响应码
     **/
    NEEDS_HTTP_STATUS_CODE("是否需要http响应码", "true"),

    /**
     * 是否http短连接
     **/
    IS_HTTP_SHORT_CONNECTION("是否http短连接", "true"),

    /**
     * http协议模式
     */
    HTTPS_SCHEMA("http协议模式", ""),

    /**
     * 是否跳转
     */
    IS_REDIRECT("是否跳转", "false"),

    /**
     * 错误跳转页面地址
     */
    ERROR_REDIRECT_URL("错误跳转页面地址", ""),

    /**
     * 闲置超时时间,默认3分钟
     */
    IDEL_TIMEOUT("闲置超时时间", "180000"),

    /**
     * 是否rest形式,如果true,则在处理来报失败的时候,返回httpCode 500
     */
    IS_REST("是否rest返回", "false"),

    /**
     * 是否需要httpHeader
     */
    NEED_REQ_HEADER("是否需要httpHeader", "true"),

    /**
     * need to check SSL client
     */
    NEED_CHECK_SSL_CLIENT("need to check SSL client", "false");

    /**
     * 描述
     */
    private final String description;

    /**
     * 默认值
     */
    private final String defaultValue;

    /**
     * 私有构造函数。
     */
    private HttpProperty(String description, String defaultValue) {
        this.description = description;
        this.defaultValue = defaultValue;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getName() {
        return this.name();
    }

    @Override
    public String getDefaultValue() {
        return defaultValue;
    }
}
