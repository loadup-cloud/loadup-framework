package io.github.loadup.components.webmvc;

import jakarta.servlet.http.HttpServletRequest;

final class ApiPathMatcher {
    private static final String API_PREFIX = "/api";

    boolean matches(HttpServletRequest request, String uri) {
        if (uri == null) return false;
        String contextPath = request.getContextPath();
        String path = contextPath != null && !contextPath.isEmpty() && uri.startsWith(contextPath)
                ? uri.substring(contextPath.length())
                : uri;
        return path.equals(API_PREFIX) || path.startsWith(API_PREFIX + "/");
    }
}
