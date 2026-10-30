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
package io.github.loadup.commons.result;

/**
 * Contract for result codes carried by {@link Result} and result enums.
 */
public interface ResultCode {
    /**
     * Validates the enum-only public error vocabulary at response/exception boundaries.
     */
    static ResultCode requireFailure(ResultCode value) {
        java.util.Objects.requireNonNull(value, "resultCode");
        if (!(value instanceof Enum<?>)) {
            throw new IllegalArgumentException("ResultCode must be an enum");
        }
        if (value.getCode() == null || !value.getCode().matches("[A-Z][A-Z0-9_]{0,63}")) {
            throw new IllegalArgumentException("Invalid result code format");
        }
        if (!io.github.loadup.commons.enums.ResultStatusEnum.FAIL.getCode().equals(value.getStatus())
                && !io.github.loadup.commons.enums.ResultStatusEnum.UNKNOWN
                        .getCode()
                        .equals(value.getStatus())) {
            throw new IllegalArgumentException("Failure result code must have failure or unknown status");
        }
        if (value.getMessage() == null
                || value.getMessage().isBlank()
                || value.getMessage().length() > 256) {
            throw new IllegalArgumentException("Invalid result code message");
        }
        return value;
    }

    String getCode();

    String getStatus();

    String getMessage();
}
