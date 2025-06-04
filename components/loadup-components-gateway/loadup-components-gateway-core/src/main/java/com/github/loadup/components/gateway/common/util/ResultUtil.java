/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.gateway.common.util;

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
import com.github.loadup.components.gateway.core.common.GatewayErrorCode;
import com.github.loadup.components.gateway.facade.model.Result;

/**
 *
 */
public class ResultUtil {

    /**
     *
     */
    public static Result buildResult(GatewayErrorCode errorCode, String message) {

        Result result = new Result();
        result.setResultCode(errorCode.getCode());
        result.setResultStatus(errorCode.getStatus());
        result.setResultMessage(errorCode.getMessage());
        return result;
    }

    /**
     *
     */
    public static Result buildResult(GatewayErrorCode errorCode) {
        return buildResult(errorCode, errorCode.getMessage());
    }

    /**
     *
     */
    public static Result buildResult(ResultCode errorCode) {
        Result result = new Result();
        result.setResultCode(errorCode.getCode());
        result.setResultStatus(errorCode.getStatus());
        result.setResultMessage(errorCode.getMessage());
        return result;
    }

    /**
     *
     */
    public static Result buildSuccessResult() {
        return buildResult(GatewayErrorCode.SUCCESS);
    }
}
