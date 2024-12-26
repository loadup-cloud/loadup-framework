package com.github.loadup.components.gateway.common.exception.util;

import com.github.loadup.components.gateway.common.exception.ErrorCode;
import com.github.loadup.components.gateway.common.exception.GatewayException;
import com.github.loadup.components.gateway.core.common.GatewayliteErrorCode;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * Assert Util
 */
public final class AssertUtil {

    /**
     * Expect is true，Actual is false，throw <code>GatewayException</code>
     *
     * @throws GatewayException
     */
    public static void isEqual(String sourceString, String destString) throws GatewayException {
        if (!StringUtils.equals(sourceString, destString)) {
            throw new GatewayException(GatewayliteErrorCode.SYSTEM_ERROR);
        }
    }

    /**
     *
     */
    public static void isNotBlank(String... itemArray) {

        for (String item : itemArray) {
            if (StringUtils.isBlank(item)) {
                throw new GatewayException(GatewayliteErrorCode.PARAM_ILLEGAL,
                        Arrays.toString(itemArray) + " exist blank");
            }
        }

    }

    /**
     * Expect is not empty，if actual is <code>null</code>，throw <code>GatewayException</code>
     *
     * @throws GatewayException
     */
    public static void isNotNull(Object object, ErrorCode resultCode) throws GatewayException {
        if (object == null) {
            throw new GatewayException(resultCode);
        }
    }

    /**
     * Expect is not empty，if actual is <code>null</code>，throw <code>GatewayException</code>
     */
    public static void isNotNull(Object object) {
        if (object == null) {
            throw new IllegalArgumentException("Parameter is nulll");
        }
    }

    /**
     * Expect is not empty，if actual is <code>null</code>，throw <code>GatewayException</code>
     */
    public static void isNotNull(Object object, String msg) {
        if (object == null) {
            throw new IllegalArgumentException(
                    (StringUtils.isBlank(msg) ? "Parameter should not be null！" : msg));
        }
    }

    /**
     * Expect is not empty，if actual is <code>null</code>，throw <code>GatewayException</code>
     *
     * @throws GatewayException
     */
    public static void isNotNull(Object object, GatewayliteErrorCode resutlCode,
                                 String message) throws GatewayException {
        if (object == null) {
            throw new GatewayException(resutlCode, message);
        }
    }

    /**
     * Expect is not empty，if actual is <code>null</code>，throw <code>GatewayException</code>
     *
     * @throws GatewayException
     */
    public static void isNotBlank(String text, ErrorCode errorCode) throws GatewayException {
        if (StringUtils.isBlank(text)) {
            throw new GatewayException(errorCode);
        }
    }

    /**
     * Expect is not empty，if actual is <code>null</code>，throw <code>GatewayException</code>
     */
    public static void isNotBlank(String text, ErrorCode errorCode, String message) {
        if (StringUtils.isBlank(text)) {
            throw new GatewayException(errorCode, message);
        }
    }

    /**
     * Expect is true，if actual is <code>null</code>，throw <code>GatewayException</code>
     */
    public static void isTrue(boolean result, ErrorCode errorCode, String message) {
        if (!result) {
            throw new GatewayException(errorCode, message);
        }
    }

    /**
     * Expect is false，if actual is <code>null</code>，throw <code>GatewayException</code>
     */
    public static void isFalse(boolean result, ErrorCode errorCode, String message) {
        if (result) {
            throw new GatewayException(errorCode, message);
        }
    }

}
