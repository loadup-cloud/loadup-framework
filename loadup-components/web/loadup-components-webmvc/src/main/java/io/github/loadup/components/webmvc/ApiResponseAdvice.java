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

import io.github.loadup.commons.enums.ResultStatusEnum;
import io.github.loadup.commons.result.IResponse;
import io.github.loadup.commons.result.PageDTO;
import io.github.loadup.commons.result.PageResponse;
import io.github.loadup.commons.result.SuccessResponse;
import io.github.loadup.components.observability.ApiResultMetrics;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/** Applies the common result envelope to every JSON API controller. */
@RestControllerAdvice
public class ApiResponseAdvice implements ResponseBodyAdvice<Object> {
    private final ApiPathMatcher pathMatcher;
    private final ApiMaskingJson masking;
    private final ApiResultMetrics resultMetrics;

    public ApiResponseAdvice(ApiPathMatcher pathMatcher, ApiMaskingJson masking, ApiResultMetrics resultMetrics) {
        this.pathMatcher = pathMatcher;
        this.masking = masking;
        this.resultMetrics = resultMetrics;
    }

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
        if (!(request instanceof ServletServerHttpRequest servletRequest)
                || !pathMatcher.matches(
                        servletRequest.getServletRequest(),
                        servletRequest.getServletRequest().getRequestURI())
                || body instanceof byte[]
                || (!MediaType.APPLICATION_JSON.isCompatibleWith(selectedContentType)
                        && !selectedContentType.getSubtype().endsWith("+json")
                        && !StringHttpMessageConverter.class.isAssignableFrom(selectedConverterType))) {
            return body;
        }
        response.setStatusCode(HttpStatus.OK);
        IResponse<?> envelope = body instanceof IResponse<?> existing
                ? existing
                : body instanceof PageDTO<?> page ? PageResponse.of(page) : SuccessResponse.of(body);
        resultMetrics.record(envelope.getResult() != null
                && ResultStatusEnum.SUCCESS
                        .getCode()
                        .equals(envelope.getResult().getStatus()));
        if (body instanceof IResponse<?>) return body;
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        if (StringHttpMessageConverter.class.isAssignableFrom(selectedConverterType)) {
            return masking.write(envelope);
        }
        return envelope;
    }
}
