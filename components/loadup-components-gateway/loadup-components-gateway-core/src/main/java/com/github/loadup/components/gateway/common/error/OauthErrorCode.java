/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.gateway.common.error;

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

import com.github.loadup.commons.result.ResultCode;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * OauthErrorCode.java
 * </p>
 */
public enum OauthErrorCode implements ResultCode {

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
