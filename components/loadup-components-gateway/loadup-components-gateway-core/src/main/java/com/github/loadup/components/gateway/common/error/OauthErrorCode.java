package com.github.loadup.components.gateway.common.error;

import com.github.loadup.components.gateway.common.exception.ErrorCode;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * OauthErrorCode.java
 * </p>
 */
public enum OauthErrorCode implements ErrorCode {

    /**
     * INVALID AUTH CLIENT
     */
    INVALID_AUTH_CLIENT("INVALID_AUTH_CLIENT", "The auth client id is invalid.", "U"),

    /**
     * INVALID ACCESS TOKEN
     */
    INVALID_ACCESS_TOKEN("INVALID_ACCESS_TOKEN", "The access token is invalid.", "F");

    /**
     * error message
     */
    private final String message;

    /**
     * error code
     */
    private final String code;

    /**
     * status
     */
    private final String status;

    OauthErrorCode(String code, String message, String status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }

    /**
     * 获取枚举值
     */
    public static OauthErrorCode getEnumByCode(String code) {
        for (OauthErrorCode codeEnum : OauthErrorCode.values()) {
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