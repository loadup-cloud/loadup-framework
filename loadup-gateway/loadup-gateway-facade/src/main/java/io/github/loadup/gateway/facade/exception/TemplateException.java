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
 * Template processing exception
 */
public class TemplateException extends GatewayException {

    private static final String MODULE = "TEMPLATE";

    public TemplateException(ErrorCode errorCode, String message) {
        super(errorCode.getCode(), ErrorType.TEMPLATE, MODULE, errorCode.getMessage() + ":" + message);
    }

    public TemplateException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode.getCode(), ErrorType.TEMPLATE, MODULE, errorCode.getMessage() + ":" + message, cause);
    }

    // Convenience methods
    public static TemplateException notFound(String templateName) {
        return new TemplateException(ErrorCode.TEMPLATE_NOT_FOUND, templateName);
    }

    public static TemplateException parseError(String templateName, Throwable cause) {
        return new TemplateException(ErrorCode.TEMPLATE_PARSE_ERROR, templateName, cause);
    }

    public static TemplateException executionError(String templateName, Throwable cause) {
        return new TemplateException(ErrorCode.TEMPLATE_EXECUTION_ERROR, templateName, cause);
    }
}
