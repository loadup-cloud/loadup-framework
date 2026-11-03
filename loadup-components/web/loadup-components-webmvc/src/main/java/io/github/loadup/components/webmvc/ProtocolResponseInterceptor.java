package io.github.loadup.components.webmvc;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

/** Captures the protocol boundary before binding, exceptions, or a direct sendError replaces the handler. */
final class ProtocolResponseInterceptor implements HandlerInterceptor {
    private final ApiPathMatcher paths;

    ProtocolResponseInterceptor(ApiPathMatcher paths) {
        this.paths = paths;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        paths.rememberProtocol(request, handler);
        return true;
    }
}
