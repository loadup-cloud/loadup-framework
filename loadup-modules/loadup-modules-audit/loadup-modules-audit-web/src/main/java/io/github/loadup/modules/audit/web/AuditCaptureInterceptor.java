/*
 * #%L
 * LoadUp Audit Center Web Adapter
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
package io.github.loadup.modules.audit.web;

import io.github.loadup.commons.log.LogUtil;
import io.github.loadup.commons.util.TenantUtil;
import io.github.loadup.modules.audit.client.command.AuditRecordCommand;
import io.github.loadup.modules.audit.client.facade.AuditFacade;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

/** Captures configured MVC operations without collecting bodies, query strings or credentials. */
public class AuditCaptureInterceptor implements HandlerInterceptor {

    private final AuditFacade service;
    private final AuditWebProperties properties;

    public AuditCaptureInterceptor(AuditFacade service, AuditWebProperties properties) {
        this.service = service;
        this.properties = properties;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        if (!properties.getIncludeMethods().contains(request.getMethod())) return;
        String path = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE) instanceof String pattern
                ? pattern
                : request.getRequestURI().substring(request.getContextPath().length());
        String action = handler instanceof HandlerMethod method
                ? method.getBeanType().getSimpleName() + "."
                        + method.getMethod().getName()
                : request.getMethod() + " " + path;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String actorId = authentication != null
                        && authentication.isAuthenticated()
                        && !"anonymousUser".equals(authentication.getPrincipal())
                ? authentication.getName()
                : null;
        String outcome = ex != null
                        || response.getStatus() >= 400
                        || Boolean.TRUE.equals(request.getAttribute(AuditResponseAdvice.FAILURE_ATTRIBUTE))
                ? "FAILURE"
                : "SUCCESS";
        try {
            service.record(new AuditRecordCommand(
                    TenantUtil.getTenantId(), actorId, action, request.getMethod(), path, outcome, MDC.get("traceId")));
        } catch (RuntimeException failure) {
            LogUtil.error(
                    AuditCaptureInterceptor.class,
                    "Failed to persist audit event for {} {}",
                    request.getMethod(),
                    path,
                    failure);
        }
    }
}
