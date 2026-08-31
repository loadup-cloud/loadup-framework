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
 * Base gateway exception All gateway-related exceptions should extend this class
 */
public class GatewayException extends RuntimeException {

    /**
     * Error code
     */
    private final String errorCode;

    /**
     * Error type
     */
    private final ErrorType errorType;

    /**
     * Module name
     */
    private final String module;

    /**
     * Constructor
     *
     * @param errorCode error code
     * @param errorType error type
     * @param module    module name
     * @param message   error message
     */
    public GatewayException(String errorCode, ErrorType errorType, String module, String message) {
        super(message);
        this.errorCode = errorCode;
        this.errorType = errorType;
        this.module = module;
    }

    /**
     * Constructor with cause
     *
     * @param errorCode error code
     * @param errorType error type
     * @param module    module name
     * @param message   error message
     * @param cause     cause throwable
     */
    public GatewayException(String errorCode, ErrorType errorType, String module, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.errorType = errorType;
        this.module = module;
    }

    /**
     * Get error code
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Get error type
     */
    public ErrorType getErrorType() {
        return errorType;
    }

    /**
     * Get module name
     */
    public String getModule() {
        return module;
    }

    /**
     * Get the full error message
     */
    public String getFullErrorMessage() {
        return String.format("[%s] %s:%s - %s", module, errorType, errorCode, getMessage());
    }

    @Override
    public String toString() {
        return String.format(
                "GatewayException{errorCode='%s', errorType=%s, module='%s', message='%s'}",
                errorCode, errorType.getDescription(), module, getMessage());
    }
}
