/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.gateway.core.communication.common.model;

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
public enum CommunicationProperty implements PropertyName {

    /**
     * 接收字符编码类型
     **/
    RECV_CHARSET("接收字符编码类型", "UTF-8"),

    /**
     * 发送字符编码类型
     **/
    SEND_CHARSET("发送字符编码类型", "UTF-8"),

    /**
     * 接收缓存区大小
     **/
    RECV_BUFFER_SIZE("接收缓存区大小", "8192"),

    /**
     * 发送缓存区大小
     **/
    SEND_BUFFER_SIZE("发送缓存区大小", "8192"),

    /**
     * 连接池大小
     **/
    CONNECTION_POOL_SIZE("连接池大小", "20"),

    /**
     * 发送或者接收后是否需要响应消息
     **/
    NEEDS_RESPONSE("是否需要响应", "true"),

    /**
     * SSL协议
     */
    SSL_PROTOCOL("SSL协议类型", "TLSv1"),

    /**
     * 是否敏感
     */
    IS_SENSITIVE("是否敏感", "false"),

    /**
     * 代理客户端名称
     */
    PROXY_NAME("代理客户端名称", ""),

    /**
     * 走INSTPROXY协议,属性配置名称
     */
    INSTPROXY_TARGET_ID("INSTPROXY协议目标通信配置ID", ""),

    /**
     * 组装报文格式,使用tcp协议时用到，用于转换二级制数据
     */
    MESSAGE_CONTENT_FORMAT("组装报文格式", ""),

    /**
     * interface router type
     */
    ROUTER_TYPE("接口路由类型", ""),

    /**
     * max connection per host
     **/
    CONNECTION_SIZE_PER_HOST("connection size per host", "20"),

    /**
     * message format
     **/
    MESSAGE_FORMAT("message format", "");

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
    private CommunicationProperty(String description, String defaultValue) {
        this.description = description;
        this.defaultValue = defaultValue;
    }

    /**
     * @see PropertyName#getDescription()
     */
    @Override
    public String getDescription() {
        return description;
    }

    /**
     * @see PropertyName#getName()
     */
    @Override
    public String getName() {
        return this.name();
    }

    /**
     * @see PropertyName#getDefaultValue()
     */
    @Override
    public String getDefaultValue() {
        return defaultValue;
    }
}
