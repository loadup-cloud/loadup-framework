package com.github.loadup.commons.result;

/*-
 * #%L
 * loadup-commons-dto
 * %%
 * Copyright (C) 2022 - 2024 loadup_cloud
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

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

@Getter
public enum CommonResultCodeEnum implements ResultCode {
    SUCCESS(ResultStatusEnum.SUCCESS, "Success."),
    UNKNOWN(ResultStatusEnum.UNKNOWN, "Unknown failed."),
    PARAM_ILLEGAL(ResultStatusEnum.FAIL, "Parameter illegal."),
    PROCESS_FAIL(ResultStatusEnum.FAIL, "Process fail."),
    ACCESS_DENIED(ResultStatusEnum.FAIL, "Access denied."),
    INVALID_CLIENT(ResultStatusEnum.FAIL, "Invalid client."),
    NOT_FOUND(ResultStatusEnum.FAIL, "Key is not found."),
    ;
    private String status;

    private String message;

    CommonResultCodeEnum(ResultStatusEnum status, String message) {
        this.status = status.getCode();
        this.message = message;
    }

    public static CommonResultCodeEnum getByResultCode(String resultCode) {
        return Arrays.stream(CommonResultCodeEnum.values()).filter(value -> StringUtils.equals(value.getCode(), resultCode))
                .findFirst().orElse(null);
    }

    @Override
    public String getCode() {
        return name();
    }

    @Override
    public String getStatus() {
        return status;
    }

    @Override
    public String getMessage() {
        return message;
    }

}
