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
 * 超网配置表枚举
 */
public enum TableName {

    /**
     * 接口定义表
     */
    LGW_INTERFACE("lgw_interface", "interface_id", "接口定义表"),

    /**
     * 应用定义表
     */
    LGW_APP("lgw_app", "app_id", "应用定义表"),

    /**
     * 机构定义表
     */
    LGW_INST("lgw_inst", "inst_id", "机构定义表"),

    /**
     * 报文处理定义表
     */
    LGW_MESSAGE_PROCESS("lgw_message_process", "message_process_id", "报文处理定义表"),

    /**
     * 应用接口映射表
     */
    LGW_APP_INTERFACE_MAP("lgw_app_interface_map", "app_id", "应用接口映射表"),

    /**
     * 通信定义表
     */
    LGW_COMMUNICATION("lgw_communication", "communication_id", "通信定义表"),

    /**
     * 通信定义表-入口
     */
    LGW_COMMUNICATION_S("lgw_communication", "interface_id", "通信定义表-入口"),

    /**
     * 前置分流规则表
     */
    LGW_ROUTE_RULE("lgw_route_rule", "route_id", "前置分流表"),

    /**
     * 前置分流表
     */
    LGW_ROUTE_CONFIG("lgw_route_config", "route_id", "前置分流表"),

    /**
     * 限流配置表
     */
    LGW_LIMIT_RULE_CONFIG("lgw_limit_rule_config", "limit_id", "限流配置表"),

    /**
     * 证书配置表
     */
    LGW_CERT_CONFIG("lgw_cert_config", "cert_code", "证书配置表"),

    /**
     * 算法映射表
     */
    LGW_CERT_ALGO_MAP("lgw_cert_algo_map", "cert_code", "算法映射表"),

    /**
     * openapi表
     */
    LGW_OPENAPI("lgw_openapi", "interface_id", "openapi表"),

    /**
     * 通用报文组件定义表
     */
    LGW_MSG_DEF("lgw_msg_def", "message_id", "通用报文组件报文定义表"),

    /**
     * 通用报文组件安全定义表
     */
    LGW_MSG_SECURITY_DEF("lgw_msg_security_def", "message_id", "通用报文组件安全定义表"),

    /**
     * 通用报文组件字段定义表
     */
    LGW_MSG_FIELD_DEF("lgw_msg_field_def", "message_id", "通用报文组件字段定义表"),

    /**
     * 通用报文组件附加处理表
     */
    LGW_MSG_EXTRA_ACTION("lgw_msg_extra_action", "message_id", "通用报文组件附加处理表"),

    /**
     * "通用报文组件字段转换器表
     */
    LGW_MSG_CONVERTOR("lgw_msg_convertor", "convertor_bean", "通用报文组件字段转换器表"),

    /**
     * 通用报文组件字段转换值对内容表，例如枚举值内外不同值
     */
    LGW_MSG_FIELD_TRANSFER_DEF("lgw_msg_field_transfer_def", "message_id", "通用报文组件字段转换值对内容表"),

    /**
     * openapi字段表
     */
    LGW_OPENAPI_FIELD("lgw_openapi_field", "interface_id", "openapi字段表"),

    /**
     * 文件传输配置表
     */
    LGW_FILE_MOVING_CONFIG("lgw_file_moving_config", "file_process_code", "文件传输配置表"),

    /**
     * 文件资源配置表
     */
    LGW_FILE_RESOURCE_CONFIG("lgw_file_resource_config", "id", "文件资源配置表"),

    /**
     * GROOVY脚本配置表
     */
    LGW_FILE_GROOVY("lgw_file_groovy", "beanName", "GROOVY脚本配置表"),

    /**
     * 文件路径配置表
     */
    LGW_FILE_PATH_CONFIG("lgw_file_path_config", "interface_id", "文件路径配置"),

    /**
     * 日志脱敏配置
     */
    LGW_SHIELD_CONF("lgw_shield_conf", "interface_id", "日志脱敏配置"),

    /**
     * api转发规则配置表
     */
    LGW_DISPATCH_RULE_CONFIG("lgw_dispatch_rule_config", "match_uri", "api转发规则配置表"),

    /**
     * dynamic script bean config
     */
    LGW_DYNAMIC_SCRIPT_BEAN_CONFIG("lgw_dynamic_script_bean", "bean_name", "dynamic script bean config"),

    /**
     * interface router config
     */
    LGW_INTERFACE_ROUTER("lgw_interface_router", "interface_id", "interface router config"),
    ;

    /**
     * 表名
     */
    private final String tableName;

    /**
     * 描述
     */
    private final String description;

    /**
     * 对应表格
     */
    private final String refreshKey;

    /**
     *
     */
    private TableName(String tableName, String refreshKey, String description) {
        this.tableName = tableName;
        this.refreshKey = refreshKey;
        this.description = description;
    }

    /**
     * 根据枚举代码获取枚举信息
     */
    public static TableName get(String code) {
        for (TableName cacheName : TableName.values()) {
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
     * 
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * 
     */
    public String getRefreshKey() {
        return refreshKey;
    }
}
