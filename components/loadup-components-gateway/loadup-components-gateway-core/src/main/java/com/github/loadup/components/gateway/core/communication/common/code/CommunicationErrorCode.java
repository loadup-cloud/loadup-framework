package com.github.loadup.components.gateway.core.communication.common.code;

import com.github.loadup.components.gateway.common.exception.ErrorCode;

/**
 * 通信错误码
 */
public enum CommunicationErrorCode implements ErrorCode {

    /**
     * 系统异常
     */
    SYSTEM_ERROR("系统异常", "LGW20001", "F"),

    /**
     * 参数错误
     */
    ILLEGAL_PARAMS("参数错误", "LGW20002", "F"),

    /**
     * 初始化证书容器发生异常
     */
    KEY_SECURITY_EXCEPTION("初始化证书容器发生异常", "LGW20003", "F"),

    /**
     * 报文编码格式不支持
     */
    ENCODE_UNSUPPORTED("报文编码格式不支持", "LGW20004", "F"),

    /**
     * 通讯配置不能为空
     */
    CONFIG_ERROR("通讯配置不能为空", "LGW20005", "F"),

    /**
     * 通讯配置不能为空
     */
    CLIENT_ERROR("通讯客户端实例不能为空", "LGW20006", "F"),

    /**
     * 协议错误
     */
    PROTOCOL_EXCEPTION("协议错误", "LGW20006", "F"),

    /**
     * 实例错误
     */
    INSTANCE_EXCEPTION("实例错误", "LGW20007", "F"),

    /**
     * SOCKET连接失败
     */
    SOCKET_TIME_OUT("SOCKET连接超时", "LGW21001", "F"),

    /**
     * 传输异常
     */
    IO_EXCEPTION("IO传输异常", "LGW21002", "F"),

    /**
     * READ TIME OUT异常
     */
    READ_TIME_OUT("读取超时", "LGW21003", "F"),

    /**
     * HTTP通信异常，非200状态码
     */
    HTTP_STATUS_ERROR("HTTP通信异常，非200状态码", "LGW21004", "F"),

    /**
     * 响应为空
     */
    RESPONSE_EMPTY("响应为空", "LGW21005", "F"),

    /**
     * 连接超时
     */
    CONNECT_TIME_OUT("连接超时", "LGW21006", "F"),

    /**
     * 连接被拒绝
     */
    CONNECT_REFUSED("连接被拒绝", "LGW21007", "F"),

    /**
     * 无代理客户端名称
     */
    NO_PROXY_NAME("无代理客户端名称", "LGW21009", "F"),

    /**
     * 无代理客户端
     */
    NO_PROXY_CLIENT("无代理客户端", "LGW21009", "F");

    /**
     * error message
     */
    private final String message;

    /**
     * error code
     */
    private final String code;

    private final String status;

    CommunicationErrorCode(String code, String message, String status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }

    /**
     * Gets get code.
     */
    @Override
    public String getCode() {
        return this.code;
    }

    /**
     * Gets get message.
     */
    @Override
    public String getMessage() {
        return this.message;
    }

    /**
     * Get the status
     */
    @Override
    public String getStatus() {
        return this.status;
    }

}
