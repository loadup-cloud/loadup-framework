/*
 * Copyright (C) 2025-2026 LoadUp Cloud
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
package io.github.loadup.components.webmvc;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;

final class ApiPathMatcher {
    private static final String API_PREFIX = "/api";
    private static final String PROTOCOL_ATTRIBUTE = ProtocolResponse.class.getName();

    boolean matches(HttpServletRequest request, String uri) {
        if (Boolean.TRUE.equals(request.getAttribute(PROTOCOL_ATTRIBUTE))) {
            return false;
        }
        rememberProtocol(request, request.getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE));
        if (Boolean.TRUE.equals(request.getAttribute(PROTOCOL_ATTRIBUTE))) {
            return false;
        }
        if (uri == null) {
            return false;
        }
        String contextPath = request.getContextPath();
        String path = contextPath != null && !contextPath.isEmpty() && uri.startsWith(contextPath)
                ? uri.substring(contextPath.length())
                : uri;
        return path.equals(API_PREFIX) || path.startsWith(API_PREFIX + "/");
    }

    void rememberProtocol(HttpServletRequest request, Object candidate) {
        if (candidate instanceof HandlerMethod handler
                && (handler.hasMethodAnnotation(ProtocolResponse.class)
                        || AnnotatedElementUtils.hasAnnotation(handler.getBeanType(), ProtocolResponse.class))) {
            // Keep the decision for a subsequent error dispatch after MVC replaces the matched handler.
            request.setAttribute(PROTOCOL_ATTRIBUTE, Boolean.TRUE);
        }
    }
}
