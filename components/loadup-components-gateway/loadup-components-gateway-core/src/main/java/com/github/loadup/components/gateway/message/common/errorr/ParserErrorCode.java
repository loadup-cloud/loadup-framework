package com.github.loadup.components.gateway.message.common.errorr;

import com.github.loadup.components.gateway.common.exception.ErrorCode;
import com.github.loadup.components.gateway.core.common.GatewayliteErrorCode;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * ParserErrorCode.java
 * </p>
 */
public enum ParserErrorCode implements ErrorCode {

    /**
     * PARSE_ERROR
     */
    PARSE_ERROR("PARSE_ERROR", "parse error", "U"),

    /**
     * NOT_EXIST_SCRIPT
     */
    NOT_EXIST_SCRIPT("NOT_EXIST_SCRIPT", "not exist script", "F");

    /**
     * error message
     */
    private final String message;

    /**
     * error code
     */
    private final String code;

    private final String status;

    ParserErrorCode(String code, String message, String status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }

    /**
     * 获取枚举值
     */
    public static GatewayliteErrorCode getEnumByCode(String code) {
        for (GatewayliteErrorCode codeEnum : GatewayliteErrorCode.values()) {
            if (StringUtils.equals(code, codeEnum.getCode())) {
                return codeEnum;
            }
        }
        return null;
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