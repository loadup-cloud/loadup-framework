package io.github.loadup.modules.audit.web;

import io.github.loadup.commons.log.LogUtil;
import io.github.loadup.commons.util.TenantUtil;
import io.github.loadup.modules.audit.AuditService;
import io.github.loadup.modules.audit.AuditWrite;
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

    private final AuditService service;
    private final AuditWebProperties properties;

    public AuditCaptureInterceptor(AuditService service, AuditWebProperties properties) {
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
            service.record(new AuditWrite(
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
