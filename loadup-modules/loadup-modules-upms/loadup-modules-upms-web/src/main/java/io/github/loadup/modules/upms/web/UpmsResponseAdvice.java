/*
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 *
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
 */
package io.github.loadup.modules.upms.web;

import io.github.loadup.commons.enums.CommonResultCodeEnum;
import io.github.loadup.commons.result.FailureResponse;
import io.github.loadup.commons.result.IResponse;
import io.github.loadup.commons.result.PageDTO;
import io.github.loadup.commons.result.SuccessResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/** Applies the result/data contract to UPMS JSON controllers. */
@RestControllerAdvice(basePackages = {"io.github.loadup.modules.upms.web", "io.github.loadup.modules.upms.authserver"})
public class UpmsResponseAdvice implements ResponseBodyAdvice<Object> {
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response) {
        if (body instanceof IResponse<?>) return body;
        if (body instanceof PageDTO<?> page) return SuccessResponse.ofPage(page);
        return SuccessResponse.of(body);
    }

    @ExceptionHandler({
        IllegalArgumentException.class,
        HttpMessageNotReadableException.class,
        MethodArgumentNotValidException.class,
        HttpMediaTypeNotSupportedException.class
    })
    public FailureResponse<Void> badRequest(Exception exception) {
        return FailureResponse.of(CommonResultCodeEnum.PARAM_ILLEGAL);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public FailureResponse<Void> accessDenied(AccessDeniedException exception) {
        return FailureResponse.of(CommonResultCodeEnum.ACCESS_DENIED);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public FailureResponse<Void> notFound(NoResourceFoundException exception) {
        return FailureResponse.of(CommonResultCodeEnum.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public FailureResponse<Void> serverError(Exception exception) {
        return FailureResponse.of(CommonResultCodeEnum.SYS_ERROR);
    }
}
