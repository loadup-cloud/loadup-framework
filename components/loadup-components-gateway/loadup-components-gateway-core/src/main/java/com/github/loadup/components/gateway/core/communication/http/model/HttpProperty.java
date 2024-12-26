package com.github.loadup.components.gateway.core.communication.http.model;

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
