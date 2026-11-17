/*
 * Copyright (C) 2026 LoadUp Cloud
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

import io.github.loadup.commons.result.FailureResponse;
import io.github.loadup.components.runtime.deployment.DeploymentIdentity;
import io.github.loadup.components.runtime.deployment.DeploymentProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.http.MediaType;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

/**
 * Verifies that an inbound request targets this deployment.
 *
 * <p>The filter only compares the configured deployment code with a single request header. It
 * creates no tenant context, resolves no schema, grants no permission and forwards nothing: a
 * mismatch is rejected before authentication or business access. The validation decision is kept
 * per request so an asynchronous or error dispatch never re-reads the header.
 */
public final class DeploymentCodeFilter extends OncePerRequestFilter {

    static final String VALIDATED_ATTRIBUTE = DeploymentCodeFilter.class.getName() + ".validated";

    private static final Pattern CODE = Pattern.compile(DeploymentProperties.CODE_PATTERN);

    private final DeploymentIdentity identity;
    private final DeploymentCodeProperties properties;
    private final ObjectMapper objectMapper;
    private final AntPathMatcher matcher = new AntPathMatcher();
    private final List<String> excludedPaths;

    public DeploymentCodeFilter(
            DeploymentIdentity identity, DeploymentCodeProperties properties, ObjectMapper objectMapper) {
        this.identity = identity;
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.excludedPaths = List.copyOf(properties.getExcludedPaths());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (request.getAttribute(VALIDATED_ATTRIBUTE) != null) {
            chain.doFilter(request, response);
            return;
        }
        if (!requiresCode(request)) {
            request.setAttribute(VALIDATED_ATTRIBUTE, Boolean.FALSE);
            chain.doFilter(request, response);
            return;
        }
        DeploymentResultCodeEnum failure = evaluate(request);
        if (failure != null) {
            writeFailure(response, failure);
            return;
        }
        request.setAttribute(VALIDATED_ATTRIBUTE, Boolean.TRUE);
        chain.doFilter(request, response);
    }

    private boolean requiresCode(HttpServletRequest request) {
        if (CorsUtils.isPreFlightRequest(request)) {
            return false;
        }
        String path = pathWithinApplication(request);
        for (String excluded : excludedPaths) {
            if (matcher.match(excluded, path)) {
                return false;
            }
        }
        return true;
    }

    private String pathWithinApplication(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (uri == null) {
            return "/";
        }
        if (contextPath != null && !contextPath.isEmpty() && uri.startsWith(contextPath)) {
            uri = uri.substring(contextPath.length());
        }
        return uri.isEmpty() ? "/" : uri;
    }

    private DeploymentResultCodeEnum evaluate(HttpServletRequest request) {
        List<String> values = Collections.list(request.getHeaders(properties.getHeaderName()));
        if (values.isEmpty()) {
            return DeploymentResultCodeEnum.DEPLOYMENT_CODE_REQUIRED;
        }
        if (values.size() != 1) {
            return DeploymentResultCodeEnum.DEPLOYMENT_CODE_INVALID;
        }
        String value = values.getFirst();
        if (value == null || !CODE.matcher(value).matches()) {
            return DeploymentResultCodeEnum.DEPLOYMENT_CODE_INVALID;
        }
        return identity.matches(value) ? null : DeploymentResultCodeEnum.DEPLOYMENT_CODE_MISMATCH;
    }

    private void writeFailure(HttpServletResponse response, DeploymentResultCodeEnum failure) throws IOException {
        response.setStatus(failure.httpStatus());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(FailureResponse.of(failure)));
    }

    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return false;
    }

    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return false;
    }
}
