/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.gateway.common.exception.util;

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

import com.github.loadup.commons.error.CommonException;
import com.github.loadup.commons.result.ResultCode;
import com.github.loadup.components.gateway.core.common.GatewayErrorCode;
import java.util.Arrays;
import org.apache.commons.lang3.StringUtils;

/**
 * Assert Util
 */
public final class AssertUtil {

    /**
     * Expect is true，Actual is false，throw <code>GatewayException</code>
     *
     * @throws CommonException
     */
    public static void isEqual(String sourceString, String destString) throws CommonException {
        if (!StringUtils.equals(sourceString, destString)) {
            throw new CommonException(GatewayErrorCode.SYSTEM_ERROR);
        }
    }

    /**
     *
     */
    public static void isNotBlank(String... itemArray) {

        for (String item : itemArray) {
            if (StringUtils.isBlank(item)) {
                throw new CommonException(GatewayErrorCode.PARAM_ILLEGAL, Arrays.toString(itemArray) + " exist blank");
            }
        }
    }

    /**
     * Expect is not empty，if actual is <code>null</code>，throw <code>GatewayException</code>
     *
     * @throws CommonException
     */
    public static void isNotNull(Object object, ResultCode resultCode) throws CommonException {
        if (object == null) {
            throw new CommonException(resultCode);
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
            throw new IllegalArgumentException((StringUtils.isBlank(msg) ? "Parameter should not be null！" : msg));
        }
    }

    /**
     * Expect is not empty，if actual is <code>null</code>，throw <code>GatewayException</code>
     *
     * @throws CommonException
     */
    public static void isNotNull(Object object, GatewayErrorCode resutlCode, String message) throws CommonException {
        if (object == null) {
            throw new CommonException(resutlCode, message);
        }
    }

    /**
     * Expect is not empty，if actual is <code>null</code>，throw <code>GatewayException</code>
     *
     * @throws CommonException
     */
    public static void isNotBlank(String text, ResultCode errorCode) throws CommonException {
        if (StringUtils.isBlank(text)) {
            throw new CommonException(errorCode);
        }
    }

    /**
     * Expect is not empty，if actual is <code>null</code>，throw <code>GatewayException</code>
     */
    public static void isNotBlank(String text, ResultCode errorCode, String message) {
        if (StringUtils.isBlank(text)) {
            throw new CommonException(errorCode, message);
        }
    }

    /**
     * Expect is true，if actual is <code>null</code>，throw <code>GatewayException</code>
     */
    public static void isTrue(boolean result, ResultCode errorCode, String message) {
        if (!result) {
            throw new CommonException(errorCode, message);
        }
    }

    /**
     * Expect is false，if actual is <code>null</code>，throw <code>GatewayException</code>
     */
    public static void isFalse(boolean result, ResultCode errorCode, String message) {
        if (result) {
            throw new CommonException(errorCode, message);
        }
    }
}
