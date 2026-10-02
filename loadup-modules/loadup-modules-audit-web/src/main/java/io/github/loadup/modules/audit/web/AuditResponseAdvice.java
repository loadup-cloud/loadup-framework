package io.github.loadup.modules.audit.web;

import io.github.loadup.commons.result.FailureResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/** Marks logical failures that use HTTP 200 and the common response envelope. */
@RestControllerAdvice
public class AuditResponseAdvice implements ResponseBodyAdvice<Object> {
    static final String FAILURE_ATTRIBUTE = AuditResponseAdvice.class.getName() + ".failure";

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
        if (body instanceof FailureResponse<?> && request instanceof ServletServerHttpRequest servletRequest) {
            servletRequest.getServletRequest().setAttribute(FAILURE_ATTRIBUTE, Boolean.TRUE);
        }
        return body;
    }
}
