package io.github.loadup.components.webmvc;

import io.github.loadup.commons.result.IResponse;
import io.github.loadup.commons.result.PageDTO;
import io.github.loadup.commons.result.SuccessResponse;
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
import tools.jackson.databind.ObjectMapper;

/** Applies the common result envelope to every JSON API controller. */
@RestControllerAdvice
public class ApiResponseAdvice implements ResponseBodyAdvice<Object> {
    private final ApiPathMatcher pathMatcher;
    private final ObjectMapper objectMapper;

    public ApiResponseAdvice(ApiPathMatcher pathMatcher, ObjectMapper objectMapper) {
        this.pathMatcher = pathMatcher;
        this.objectMapper = objectMapper;
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
        if (body instanceof IResponse<?>) return body;
        Object envelope = body instanceof PageDTO<?> page ? SuccessResponse.ofPage(page) : SuccessResponse.of(body);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        if (StringHttpMessageConverter.class.isAssignableFrom(selectedConverterType)) {
            return objectMapper.writeValueAsString(envelope);
        }
        return envelope;
    }
}
