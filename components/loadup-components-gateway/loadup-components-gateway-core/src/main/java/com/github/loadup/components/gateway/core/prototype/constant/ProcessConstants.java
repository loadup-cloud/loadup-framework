package com.github.loadup.components.gateway.core.prototype.constant;

/**
 * 定义交易过程中的一些常量类
 */
public class ProcessConstants {

    /**
     * 当外部报文不是json个时候的时候，将所有内容透传给内部系统
     */
    public static final String KEY_REQUEST_HEADER = "request-header";
    public static final String KEY_REQUEST_BODY   = "request-body";

    /**
     * 定义的下游集成的URL信息
     */
    public static final String KEY_HTTP_INTEGRATION_URL = "integration-url";

    /**
     * the interface we got from the out side
     */
    public static final String KEY_HTTP_INTERFACE_ID = "interface-id";

    /**
     * 定义的下游集成的URL信息
     */
    public static final String KEY_REQUEST_HTTP_INTEGRATION_URL = "request-integration-url";

    /**
     * 签名信息所在字段
     */
    public static final String KEY_SIGNATURE = "signature";

    /**
     * 请求时间
     */
    public static final String KEY_HTTP_REQUEST_TIME = "request-time";

    /**
     * clientId
     */
    public static final String KEY_HTTP_CLIENT_ID = "client-id";

    /**
     * 响应时间
     */
    public static final String KEY_HTTP_RESPONSE_TIME = "response-time";

    /**
     * 来报信息的解析结果
     */
    public static final String KEY_INCOMING_PARSED_RESULT = "incoming-parsed-result";

    /**
     * 来报时候对应interfaceId信息
     */
    public static final String KEY_INCOMING_INTERFACE_ID = "incoming-interface-id";

    /**
     * 请求访问者对应的安全信息
     */
    public static final String KEY_INCOMING_CERT_CODE = "incoming-security-strategy-code";

    /**
     * 请求发送者对应的安全信息
     */
    public static final String KEY_CERT_CODE = "security-strategy-code";

    /**
     * 请求发送者对应的安全信息
     */
    public static final String KEY_REQUEST_CERT_CODE = "request-security-strategy-code";

    /**
     * 解析错误的设置错误信息
     */
    public static final String KEY_PARSE_EXCEPTION = "parse_error_exception";

    /**
     * 解析结果保存出
     */
    public static final String KEY_PARSE_RESULT = "parse_result";

    /**
     * http请求的URI
     */
    public static final String KEY_HTTP_REQUEST_URI = "request-uri";

    /**
     * http请求方法
     */
    public static final String KEY_HTTP_METHOD = "request_http_method";

    /**
     * 签名证书版本
     */
    public static final String KEY_SIGNATURE_VERSION = "1";

    /**
     * 签名证书算法
     */
    public static final String KEY_SIGNATURE_ALGO = "KEY_SIGNATURE_ALGO";

    /**
     * send 请求对外暴露的http服务
     */
    public static final String KEY_HTTP_SEND_METHOD = "/gateway/messageservie/send";

}