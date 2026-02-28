package io.github.loadup.modules.log.infrastructure.aspect;

/*-
 * #%L
 * Loadup Modules Log Infrastructure
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.loadup.modules.log.client.annotation.OperationLog;
import io.github.loadup.modules.log.infrastructure.async.LogAsyncWriter;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Aspect
@RequiredArgsConstructor
public class OperationLogAspect {

    private final LogAsyncWriter logAsyncWriter;
    private final ObjectMapper objectMapper;

    @Around("@annotation(operationLog)")
    public Object around(ProceedingJoinPoint pjp, OperationLog operationLog) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = null;
        Throwable error = null;

        try {
            result = pjp.proceed();
            return result;
        } catch (Throwable t) {
            error = t;
            throw t;
        } finally {
            long duration = System.currentTimeMillis() - start;
            saveLog(pjp, operationLog, result, error, duration);
        }
    }

    private void saveLog(
            ProceedingJoinPoint pjp, OperationLog annotation, Object result, Throwable error, long duration) {
        try {
            String ip = null;
            String userAgent = null;
            var attrs = RequestContextHolder.getRequestAttributes();
            if (attrs instanceof ServletRequestAttributes sra) {
                HttpServletRequest req = sra.getRequest();
                ip = getClientIp(req);
                userAgent = req.getHeader("User-Agent");
            }

            io.github.loadup.modules.log.domain.model.OperationLog model =
                    io.github.loadup.modules.log.domain.model.OperationLog.builder()
                            .id(UUID.randomUUID().toString().replace("-", ""))
                            .module(annotation.module())
                            .operationType(annotation.type())
                            .description(annotation.description())
                            .method(pjp.getSignature().toShortString())
                            .requestParams(annotation.recordParams() ? toJson(pjp.getArgs()) : null)
                            .responseResult(annotation.recordResponse() && result != null ? toJson(result) : null)
                            .duration(duration)
                            .success(error == null)
                            .errorMessage(error != null ? truncate(error.getMessage(), 500) : null)
                            .ip(ip)
                            .userAgent(userAgent)
                            .operationTime(LocalDateTime.now())
                            .createdAt(LocalDateTime.now())
                            .build();

            logAsyncWriter.saveOperationLog(model);
        } catch (Exception e) {
            log.warn("Failed to build operation log record", e);
        }
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "[serialization failed]";
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    private String truncate(String s, int maxLen) {
        if (s == null) return null;
        return s.length() <= maxLen ? s : s.substring(0, maxLen);
    }
}
