/*
 * #%L
 * LoadUp Common Error
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.github.loadup.commons.error;

import io.github.loadup.commons.result.ResultCode;
import java.io.Serial;

/** Coded unchecked failure; outward messages come exclusively from the enum. */
public class CommonException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 2713503013175560520L;

    private final ResultCode resultCode;

    public CommonException(ResultCode resultCode) {
        this(resultCode, null);
    }

    public CommonException(ResultCode resultCode, Throwable cause) {
        super(ResultCode.requireFailure(resultCode).getMessage(), cause);
        this.resultCode = resultCode;
    }

    public ResultCode getResultCode() {
        return resultCode;
    }

    @Override
    public String toString() {
        return "{\"code\":\"" + resultCode.getCode() + "\",\"message\":\"" + escape(resultCode.getMessage()) + "\"}";
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
