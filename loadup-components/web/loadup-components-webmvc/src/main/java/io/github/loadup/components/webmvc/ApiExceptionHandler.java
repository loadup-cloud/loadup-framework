/*
 * #%L
 * LoadUp Web MVC
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
package io.github.loadup.components.webmvc;

import io.github.loadup.commons.enums.CommonResultCodeEnum;
import io.github.loadup.commons.error.CommonException;
import io.github.loadup.commons.log.LogUtil;
import io.github.loadup.commons.result.FailureResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

/** Preserves business result codes while keeping non-API protocol behavior intact. */
@RestControllerAdvice
public final class ApiExceptionHandler {
    private final ApiPathMatcher paths;

    public ApiExceptionHandler(ApiPathMatcher paths) {
        this.paths = paths;
    }

    @ExceptionHandler(CommonException.class)
    public ResponseEntity<FailureResponse<Void>> business(CommonException failure, HttpServletRequest request)
            throws Exception {
        requireApi(request, failure);
        if (failure.getCause() != null || failure.getResultCode() == CommonResultCodeEnum.SYS_ERROR) {
            LogUtil.error(
                    ApiExceptionHandler.class,
                    "Coded API failure; code={}; causeType={}",
                    failure.getResultCode().getCode(),
                    failure.getCause() == null
                            ? "none"
                            : failure.getCause().getClass().getName());
        }
        return ResponseEntity.ok(FailureResponse.of(failure.getResultCode()));
    }

    @ExceptionHandler({
        MethodArgumentNotValidException.class,
        ConstraintViolationException.class,
        HandlerMethodValidationException.class,
        HttpMessageNotReadableException.class,
        IllegalArgumentException.class
    })
    public ResponseEntity<FailureResponse<Void>> invalid(Exception failure, HttpServletRequest request)
            throws Exception {
        requireApi(request, failure);
        // Record constructors can reject a request before Jackson finishes binding.
        Throwable cause = failure;
        for (int depth = 0; cause != null && depth < 16; depth++, cause = cause.getCause()) {
            if (cause instanceof CommonException business) return business(business, request);
        }
        return ResponseEntity.ok(FailureResponse.of(CommonResultCodeEnum.PARAM_ILLEGAL));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<FailureResponse<Void>> system(Exception failure, HttpServletRequest request)
            throws Exception {
        requireApi(request, failure);
        if (isSecurityFailure(failure)) throw failure;
        LogUtil.error(
                ApiExceptionHandler.class,
                "API request failed; failureType={}",
                failure.getClass().getName());
        return ResponseEntity.ok(FailureResponse.of(CommonResultCodeEnum.SYS_ERROR));
    }

    // Security filters own authentication and access-denied protocol responses.
    // Detect the base types without making Spring Security a mandatory dependency.
    private boolean isSecurityFailure(Exception failure) {
        for (Class<?> type = failure.getClass(); type != null; type = type.getSuperclass()) {
            if (type.getName().equals("org.springframework.security.access.AccessDeniedException")
                    || type.getName().equals("org.springframework.security.core.AuthenticationException")) return true;
        }
        return false;
    }

    private void requireApi(HttpServletRequest request, Exception failure) throws Exception {
        if (!paths.matches(request, request.getRequestURI())) throw failure;
    }
}
