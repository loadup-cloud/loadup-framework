package io.github.loadup.components.authorization.aspect;

/*-
 * #%L
 * LoadUp Components Authorization
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

import io.github.loadup.components.authorization.annotation.RequirePermission;
import io.github.loadup.components.authorization.annotation.RequireRole;
import io.github.loadup.components.authorization.context.UserContext;
import io.github.loadup.components.authorization.exception.ForbiddenException;
import io.github.loadup.components.authorization.exception.UnauthorizedException;
import io.github.loadup.components.authorization.model.LoadUpUser;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * AOP aspect for authorization checks.
 *
 * <p>Intercepts methods annotated with {@link RequireRole} or {@link RequirePermission}
 * and validates user permissions before method execution.</p>
 */
@Aspect
@Component
@Order(100) // Execute before transaction management
public class AuthorizationAspect {
    private static final Logger log = LoggerFactory.getLogger(AuthorizationAspect.class);

    /**
     * Intercept methods with {@link RequireRole} annotation
     */
    @Around("@annotation(io.github.loadup.components.authorization.annotation.RequireRole) || "
            + "@within(io.github.loadup.components.authorization.annotation.RequireRole)")
    public Object checkRole(ProceedingJoinPoint pjp) throws Throwable {
        RequireRole requireRole = getAnnotation(pjp, RequireRole.class);
        if (requireRole == null) {
            return pjp.proceed();
        }

        LoadUpUser user = UserContext.get();
        if (user == null) {
            log.warn("User not authenticated, denying access to method: {}", pjp.getSignature());
            throw new UnauthorizedException("User not authenticated");
        }

        String[] requiredRoles = requireRole.value();
        List<String> userRoles = user.getRoles();

        if (userRoles == null || userRoles.isEmpty()) {
            log.warn("User {} has no roles, denying access to method: {}", user.getUserId(), pjp.getSignature());
            throw new ForbiddenException("Insufficient permissions");
        }

        boolean hasAccess =
                switch (requireRole.logical()) {
                    case OR -> hasAnyRole(userRoles, requiredRoles);
                    case AND -> hasAllRoles(userRoles, requiredRoles);
                };

        if (!hasAccess) {
            log.warn(
                    "Access denied for user: {}, required roles: {}, user roles: {}",
                    user.getUserId(),
                    Arrays.toString(requiredRoles),
                    userRoles);
            throw new ForbiddenException("Insufficient permissions: required roles " + Arrays.toString(requiredRoles));
        }

        log.debug("Authorization passed for user: {}, method: {}", user.getUserId(), pjp.getSignature());

        return pjp.proceed();
    }

    /**
     * Intercept methods with {@link RequirePermission} annotation
     */
    @Around("@annotation(io.github.loadup.components.authorization.annotation.RequirePermission) || "
            + "@within(io.github.loadup.components.authorization.annotation.RequirePermission)")
    public Object checkPermission(ProceedingJoinPoint pjp) throws Throwable {
        RequirePermission requirePermission = getAnnotation(pjp, RequirePermission.class);
        if (requirePermission == null) {
            return pjp.proceed();
        }

        LoadUpUser user = UserContext.get();
        if (user == null) {
            log.warn("User not authenticated, denying access to method: {}", pjp.getSignature());
            throw new UnauthorizedException("User not authenticated");
        }

        String[] requiredPermissions = requirePermission.value();
        List<String> userPermissions = user.getPermissions();

        if (userPermissions == null || userPermissions.isEmpty()) {
            log.warn("User {} has no permissions, denying access to method: {}", user.getUserId(), pjp.getSignature());
            throw new ForbiddenException("Insufficient permissions");
        }

        boolean hasAccess =
                switch (requirePermission.logical()) {
                    case OR -> hasAnyPermission(userPermissions, requiredPermissions);
                    case AND -> hasAllPermissions(userPermissions, requiredPermissions);
                };

        if (!hasAccess) {
            log.warn(
                    "Access denied for user: {}, required permissions: {}, user permissions: {}",
                    user.getUserId(),
                    Arrays.toString(requiredPermissions),
                    userPermissions);
            throw new ForbiddenException(
                    "Insufficient permissions: required permissions " + Arrays.toString(requiredPermissions));
        }

        log.debug("Authorization passed for user: {}, method: {}", user.getUserId(), pjp.getSignature());

        return pjp.proceed();
    }

    /**
     * Get annotation from method or class
     */
    private <T extends Annotation> T getAnnotation(ProceedingJoinPoint pjp, Class<T> annotationClass) {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        T annotation = signature.getMethod().getAnnotation(annotationClass);
        if (annotation == null) {
            annotation = pjp.getTarget().getClass().getAnnotation(annotationClass);
        }
        return annotation;
    }

    /**
     * Check if user has any of the required roles
     */
    private boolean hasAnyRole(List<String> userRoles, String... requiredRoles) {
        return Arrays.stream(requiredRoles)
                .anyMatch(role -> userRoles.contains(role) || userRoles.contains("ROLE_" + role));
    }

    /**
     * Check if user has all of the required roles
     */
    private boolean hasAllRoles(List<String> userRoles, String... requiredRoles) {
        return Arrays.stream(requiredRoles)
                .allMatch(role -> userRoles.contains(role) || userRoles.contains("ROLE_" + role));
    }

    /**
     * Check if user has any of the required permissions
     */
    private boolean hasAnyPermission(List<String> userPermissions, String... requiredPermissions) {
        return Arrays.stream(requiredPermissions).anyMatch(userPermissions::contains);
    }

    /**
     * Check if user has all of the required permissions
     */
    private boolean hasAllPermissions(List<String> userPermissions, String... requiredPermissions) {
        return Arrays.stream(requiredPermissions).allMatch(userPermissions::contains);
    }
}
