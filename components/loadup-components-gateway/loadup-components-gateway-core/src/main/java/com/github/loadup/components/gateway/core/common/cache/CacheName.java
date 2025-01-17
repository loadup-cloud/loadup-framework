package com.github.loadup.components.gateway.core.common.cache;

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

/**
 * 缓存域枚举
 * 一个域可以对应多张表，一个表只能有一个域
 */
public enum CacheName {

    /**
     * 接口定义缓存
     */
    INTERFACE("接口定义缓存", new TableName[]{TableName.LGW_INTERFACE}),

    /**
     * 应用缓存
     */
    APP("应用缓存", new TableName[]{TableName.LGW_APP}),

    /**
     * 机构缓存
     */
    INST("机构缓存", new TableName[]{TableName.LGW_INST}),

    /**
     * 报文处理缓存
     */
    MESSAGE_PROCESS("报文处理缓存", new TableName[]{TableName.LGW_MESSAGE_PROCESS}),

    /**
     * Message Receiver
     */
    MESSAGE_RECEIVER("报文接受者", new TableName[]{}),

    /**
     * Message Sender
     */
    MESSAGE_SENDER("报文发送方", new TableName[]{}),

    /**
     * 接口关系缓存
     */
    APP_INTERFACE("接口关系缓存", new TableName[]{TableName.LGW_APP_INTERFACE_MAP}),

    /**
     * 通信缓存
     */
    COMMUNICATION("通信缓存", new TableName[]{TableName.LGW_COMMUNICATION}),

    /**
     * 前置分流缓存
     */
    ROUTE("前置分流缓存", new TableName[]{TableName.LGW_ROUTE_RULE, TableName.LGW_ROUTE_CONFIG}),

    /**
     * 限流配置缓存
     */
    LIMIT_RULE("限流配置缓存", new TableName[]{TableName.LGW_LIMIT_RULE_CONFIG}),

    /**
     * 证书配置缓存
     */
    CERT("证书配置缓存", new TableName[]{TableName.LGW_CERT_ALGO_MAP, TableName.LGW_CERT_CONFIG}),

    /**
     * 证书详细信息
     */
    CERT_CONFIG("证书详细信息", new TableName[]{}),

    /**
     * 证书算法相关信息
     */
    CERT_ALGO_CONFIG("证书算法信息", new TableName[]{}),

    /**
     * 传输配置
     */
    FILE_MOVING_CONFIG("传输配置", new TableName[]{TableName.LGW_FILE_MOVING_CONFIG}),

    /**
     * 资源配置
     */
    FILE_RESOURCE_CONFIG("资源配置", new TableName[]{TableName.LGW_FILE_RESOURCE_CONFIG}),

    /**
     * GROOVY脚本配置
     */
    FILE_GROOVY("GROOVY脚本配置", new TableName[]{TableName.LGW_FILE_GROOVY}),

    /**
     * 敏感字段配置
     */
    SHIELD_CONFIG("敏感字段配置", new TableName[]{TableName.LGW_SHIELD_CONF}),

    /**
     * api转发规则配置表
     */
    DISPATCH_RULE_CONFIG("api转发规则配置表", new TableName[]{TableName.LGW_DISPATCH_RULE_CONFIG}),

    /**
     * dynamic script bean config
     */
    DYNAMIC_SCRIPT_BEAN_CONFIG("dynamic script bean config", new TableName[]{TableName.LGW_DYNAMIC_SCRIPT_BEAN_CONFIG}),

    /**
     * 适配没有缓存域的表
     **/
    NULL("空的缓存域", new TableName[]{});

    /**
     * 描述
     */
    private final String description;

    /**
     * 对应表格
     */
    private final TableName[] tables;

    /**
     *
     */
    private CacheName(String description, TableName[] tables) {
        this.description = description;
        this.tables = tables;
    }

    /**
     * 根据枚举代码获取枚举信息
     */
    public static CacheName get(String code) {
        for (CacheName cacheName : CacheName.values()) {
            if (cacheName.getCode().equalsIgnoreCase(code)) {
                return cacheName;
            }
        }

        return null;
    }

    /**
     * 获取枚举代码
     */
    public String getCode() {
        return this.name();
    }

    /**
     * 获取枚举描述信息
     */
    public String getDescription() {
        return description;
    }

    /**
     * Getter method for property <tt>tables</tt>.
     */
    public TableName[] getTables() {
        return tables;
    }

}
