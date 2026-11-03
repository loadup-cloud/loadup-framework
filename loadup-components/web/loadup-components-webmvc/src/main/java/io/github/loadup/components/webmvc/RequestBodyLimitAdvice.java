package io.github.loadup.components.webmvc;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

/** Bounds the complete input before JSON binding, including unknown-length and trailing input. */
@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public final class RequestBodyLimitAdvice extends RequestBodyAdviceAdapter {
    private final String pathPrefix;
    private final int maxBytes;

    public RequestBodyLimitAdvice(RequestBodyLimitProperties properties) {
        pathPrefix = properties.pathPrefix();
        maxBytes = Math.toIntExact(properties.maxSize().toBytes());
    }

    @Override
    public boolean supports(
            MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converter) {
        return true;
    }

    @Override
    public HttpInputMessage beforeBodyRead(
            HttpInputMessage input,
            MethodParameter parameter,
            Type targetType,
            Class<? extends HttpMessageConverter<?>> converter)
            throws IOException {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) {
            return input;
        }
        var request = attributes.getRequest();
        // Use the matched MVC route: encoded URI spelling and a context path cannot bypass the prefix.
        Object mapping = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        String path = mapping instanceof String pattern ? pattern : request.getServletPath();
        if (!path.startsWith(pathPrefix)) {
            return input;
        }
        if (input.getHeaders().getContentLength() > maxBytes) {
            throw new RequestBodyLimitException();
        }
        // Read at most limit + 1 before handing any bytes to the converter; Content-Length is not trusted.
        byte[] body = input.getBody().readNBytes(maxBytes + 1);
        if (body.length > maxBytes) {
            throw new RequestBodyLimitException();
        }
        return new HttpInputMessage() {
            @Override
            public InputStream getBody() {
                return new ByteArrayInputStream(body);
            }

            @Override
            public HttpHeaders getHeaders() {
                return input.getHeaders();
            }
        };
    }
}
