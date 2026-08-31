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
 * System exception
 */
public class SystemException extends GatewayException {

    private static final String MODULE = "SYSTEM";

    public SystemException(ErrorCode errorCode, String message) {
        super(errorCode.getCode(), ErrorType.SYSTEM, MODULE, errorCode.getMessage() + ":" + message);
    }

    public SystemException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode.getCode(), ErrorType.SYSTEM, MODULE, errorCode.getMessage() + ":" + message, cause);
    }

    // Convenience methods
    public static SystemException configurationError(String message) {
        return new SystemException(ErrorCode.CONFIGURATION_ERROR, message);
    }

    public static SystemException initializationError(String component, Throwable cause) {
        return new SystemException(ErrorCode.INITIALIZATION_ERROR, component, cause);
    }

    public static SystemException internalError(String message) {
        return new SystemException(ErrorCode.SYSTEM_ERROR, message);
    }

    public static SystemException internalError(String message, Throwable cause) {
        return new SystemException(ErrorCode.SYSTEM_ERROR, message, cause);
    }

    public static SystemException operationNotSupported(String operation) {
        return new SystemException(ErrorCode.OPERATION_NOT_SUPPORTED, operation);
    }
}
