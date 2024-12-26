package com.github.loadup.components.gateway.common.error;

import com.github.loadup.components.gateway.common.exception.ErrorCode;
import org.apache.commons.lang3.StringUtils;

/**
 * Limit rule error code
 *
 * @date 2020-3-25 PM. 4:31:30
 */

public enum LimitRuleErrorCode implements ErrorCode {

    /**
     * LIMIT NO TOKEN
     */
    LIMIT_NO_TOKEN("LIMIT_NO_TOKEN", "no token.", "U");

    /**
     * error message
     */
    private final String message;

    /**
     * error code
     */
    private final String code;

    private final String status;

    /**
     * limit rule error code
     */
    LimitRuleErrorCode(String code, String message, String status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }

    /**
     * Get error code instance by code
     */
    public static LimitRuleErrorCode getEnumByCode(String code) {
        for (LimitRuleErrorCode codeEnum : LimitRuleErrorCode.values()) {
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
