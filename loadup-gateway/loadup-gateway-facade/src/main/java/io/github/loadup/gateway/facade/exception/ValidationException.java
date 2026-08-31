package io.github.loadup.gateway.facade.exception;

/*-
 * #%L
 * LoadUp Gateway Facade
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

/**
 * Parameter validation exception
 */
public class ValidationException extends GatewayException {

    private static final String MODULE = "VALIDATION";

    public ValidationException(ErrorCode errorCode, String message) {
        super(errorCode.getCode(), ErrorType.VALIDATION, MODULE, errorCode.getMessage() + ":" + message);
    }

    public ValidationException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode.getCode(), ErrorType.VALIDATION, MODULE, errorCode.getMessage() + ":" + message, cause);
    }

    // Convenience methods
    public static ValidationException required(String paramName) {
        return new ValidationException(ErrorCode.PARAM_REQUIRED, paramName);
    }

    public static ValidationException invalidFormat(String paramName, String expectedFormat) {
        return new ValidationException(
                ErrorCode.PARAM_INVALID_FORMAT, paramName + ", Expected format: " + expectedFormat);
    }

    public static ValidationException outOfRange(String paramName, String range) {
        return new ValidationException(ErrorCode.PARAM_OUT_OF_RANGE, paramName + ", Valid range: " + range);
    }
}
