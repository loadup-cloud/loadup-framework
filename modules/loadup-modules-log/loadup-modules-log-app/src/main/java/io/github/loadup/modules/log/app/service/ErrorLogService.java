package io.github.loadup.modules.log.app.service;

/*-
 * #%L
 * Loadup Modules Log App
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

import io.github.loadup.modules.log.client.dto.ErrorLogDTO;
import io.github.loadup.modules.log.client.query.ErrorLogQuery;
import io.github.loadup.modules.log.domain.gateway.ErrorLogGateway;
import io.github.loadup.modules.log.domain.model.ErrorLog;
import io.github.loadup.modules.log.infrastructure.async.LogAsyncWriter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * Application service for error log management.
 *
 * <p>Exposed to Gateway via {@code bean://errorLogService:method}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ErrorLogService {

    private final ErrorLogGateway gateway;
    private final LogAsyncWriter logAsyncWriter;

    /** Query error logs by condition (paginated). */
    public List<ErrorLogDTO> listByCondition(ErrorLogQuery query) {
        Assert.notNull(query, "query must not be null");
        int pageNum = query.getPageNum() == null ? 1 : query.getPageNum();
        int pageSize = query.getPageSize() == null ? 20 : Math.min(query.getPageSize(), 200);
        return gateway
                .findByCondition(
                        query.getUserId(),
                        query.getErrorType(),
                        query.getErrorCode(),
                        query.getStartTime(),
                        query.getEndTime(),
                        pageNum,
                        pageSize)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    /** Count error logs by condition. */
    public long countByCondition(ErrorLogQuery query) {
        Assert.notNull(query, "query must not be null");
        return gateway.countByCondition(
                query.getUserId(),
                query.getErrorType(),
                query.getErrorCode(),
                query.getStartTime(),
                query.getEndTime());
    }

    /**
     * Record an error log entry asynchronously.
     *
     * @param errorType BUSINESS / SYSTEM / THIRD_PARTY
     * @param errorCode optional application error code
     * @param errorMessage short error message (required)
     * @param stackTrace full stack trace (nullable)
     * @param requestUrl the URL that triggered the error (nullable)
     * @param requestMethod HTTP method (nullable)
     * @param ip client IP (nullable)
     */
    public void record(
            String userId,
            String errorType,
            String errorCode,
            String errorMessage,
            String stackTrace,
            String requestUrl,
            String requestMethod,
            String ip) {
        Assert.hasText(errorMessage, "errorMessage must not be blank");
        ErrorLog record = ErrorLog.builder()
                .id(UUID.randomUUID().toString().replace("-", ""))
                .userId(userId)
                .errorType(errorType != null ? errorType : "SYSTEM")
                .errorCode(errorCode)
                .errorMessage(truncate(errorMessage, 2000))
                .stackTrace(truncate(stackTrace, 8000))
                .requestUrl(requestUrl)
                .requestMethod(requestMethod)
                .ip(ip)
                .errorTime(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();
        logAsyncWriter.saveErrorLog(record);
    }

    /**
     * Record an exception directly, extracting type, message and stack trace automatically.
     */
    public void recordException(String userId, Throwable ex, String requestUrl, String requestMethod, String ip) {
        String stackTrace = buildStackTrace(ex);
        record(
                userId,
                "SYSTEM",
                ex.getClass().getSimpleName(),
                ex.getMessage(),
                stackTrace,
                requestUrl,
                requestMethod,
                ip);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }

    private String buildStackTrace(Throwable ex) {
        if (ex == null) return null;
        StringBuilder sb = new StringBuilder();
        sb.append(ex.getClass().getName()).append(": ").append(ex.getMessage()).append("\n");
        for (StackTraceElement el : ex.getStackTrace()) {
            sb.append("\tat ").append(el).append("\n");
            if (sb.length() > 6000) {
                sb.append("\t... (truncated)");
                break;
            }
        }
        return sb.toString();
    }

    private ErrorLogDTO toDTO(ErrorLog m) {
        ErrorLogDTO dto = new ErrorLogDTO();
        dto.setId(m.getId());
        dto.setUserId(m.getUserId());
        dto.setErrorType(m.getErrorType());
        dto.setErrorCode(m.getErrorCode());
        dto.setErrorMessage(m.getErrorMessage());
        dto.setStackTrace(m.getStackTrace());
        dto.setRequestUrl(m.getRequestUrl());
        dto.setRequestMethod(m.getRequestMethod());
        dto.setRequestParams(m.getRequestParams());
        dto.setIp(m.getIp());
        dto.setErrorTime(m.getErrorTime());
        return dto;
    }
}
