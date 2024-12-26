package com.github.loadup.components.gateway.certification.exception;

import com.github.loadup.components.gateway.common.exception.ErrorCode;
import com.github.loadup.components.gateway.common.exception.GatewayException;
import com.github.loadup.components.gateway.core.common.GatewayliteErrorCode;

/**
 * 安全组件异常
 */
public class CertificationException extends GatewayException {
    /**
     * Constructor.
     */
    public CertificationException(String message) {
        super(GatewayliteErrorCode.SYSTEM_ERROR, message);
    }

    /**
     * Constructor.
     */
    public CertificationException(ErrorCode resultCode) {
        super(resultCode);
    }

    /**
     * Constructor.
     */
    public CertificationException(ErrorCode resultCode, String message) {
        super(resultCode, message);
    }

    /**
     * Constructor.
     */
    public CertificationException(ErrorCode resultCode, Throwable e) {
        super(resultCode, e);
    }

    /**
     * Constructor.
     */
    public CertificationException(ErrorCode resultCode, String message, Throwable e) {
        super(resultCode, message, e);
    }
}
