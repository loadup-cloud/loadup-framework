package com.github.loadup.components.gateway.certification.exception;

import com.github.loadup.components.gateway.common.exception.ErrorCode;
import com.github.loadup.components.gateway.core.common.GatewayliteErrorCode;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * BizErrorCode.java
 * </p>
 */
public enum CertificationErrorCode implements ErrorCode {

    /**
     * UNSUPPORTED_OPERATION
     */
    UNSUPPORTED_OPERATION("UNSUPPORTED_OPERATION", "unsupported operation", "F"),

    /**
     * SIGN_ERROR
     */
    SIGN_ERROR("SIGN_ERROR", "sign error", "F"),

    /**
     * VERIFY_ERROR
     */
    VERIFY_ERROR("VERIFY_ERROR", "verify error", ""),

    /**
     * RECOVER_KEY_ERROR
     */
    RECOVER_KEY_ERROR("RECOVER_KEY_ERROR", "recover key error", "F"),

    /**
     * ENCRYPT_ERROR
     */
    ENCRYPT_ERROR("ENCRYPT_ERROR", "encrypt error", "F"),

    /**
     * DECRYPT_ERROR
     */
    DECRYPT_ERROR("DECRYPT_ERROR", "decrypt error", "F"),

    /**
     * DIGEST_ERROR
     */
    DIGEST_ERROR("DIGEST_ERROR", "digest error", "F"),

    /**
     * RECOVER_PRIVATE_KEY_ERROR
     */
    RECOVER_PRIVATE_KEY_ERROR("RECOVER_PRIVATE_KEY_ERROR", "recover private key error", "F"),

    /**
     * RECOVER_PUBLIC_KEY_ERROR
     */
    RECOVER_PUBLIC_KEY_ERROR("RECOVER_PUBLIC_KEY_ERROR", "recover public key error", "F"),

    /**
     * GET_CERT_CONTENT_ERROR
     */
    GET_CERT_CONTENT_ERROR("GET_CERT_CONTENT_ERROR", "get cert content error", "F"),

    /**
     * CONFIG_ERROR
     */
    CONFIG_ERROR("CONFIG_ERROR", "config error", "F"),

    /**
     * NO_OUT_SERVICE
     */
    NO_OUT_SERVICE("NO_OUT_SERVICE", "no out service", "F"),

    /**
     * UNSUPPORTED_ALGORITHM
     */
    UNSUPPORTED_ALGORITHM("UNSUPPORTED_ALGORITHM", "unsupported algorithm", "F"),

    /**
     * UNSUPPORTED_FORMAT
     */
    UNSUPPORTED_FORMAT("UNSUPPORTED_FORMAT", "unsupported format", "F"),

    /**
     * OUTPUT_CONVERT_ERROR
     */
    OUTPUT_CONVERT_ERROR("OUTPUT_CONVERT_ERROR", "output covert error", "F"),

    /**
     * INPUT_CONVERT_ERROR
     */
    INPUT_CONVERT_ERROR("INPUT_CONVERT_ERROR", "input covert error", "F");

    /**
     * error message
     */
    private final String message;

    /**
     * error code
     */
    private final String code;

    private final String status;

    CertificationErrorCode(String code, String message, String status) {
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