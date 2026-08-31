package io.github.loadup.modules.log.app.service;

/*-
 * #%L
 * Loadup Modules Log App
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

import io.github.loadup.modules.log.client.dto.LogStatisticsDTO;
import io.github.loadup.modules.log.client.dto.LogStatisticsDTO.StatItem;
import io.github.loadup.modules.log.client.dto.OperationLogDTO;
import io.github.loadup.modules.log.client.query.LogStatisticsQuery;
import io.github.loadup.modules.log.client.query.OperationLogQuery;
import io.github.loadup.modules.log.domain.gateway.OperationLogGateway;
import io.github.loadup.modules.log.domain.model.OperationLog;
import io.github.loadup.modules.log.infrastructure.async.LogAsyncWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * Application service for operation log management.
 *
 * <p>Exposed to Gateway via {@code bean://operationLogService:method}.
 */
@Service
public class OperationLogService {
    private static final Logger log = LoggerFactory.getLogger(OperationLogService.class);

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final OperationLogGateway gateway;
    private final LogAsyncWriter logAsyncWriter;

    /**
     * Query operation logs by condition (paginated).
     */
    public List<OperationLogDTO> listByCondition(OperationLogQuery query) {
        Assert.notNull(query, "query must not be null");
        int pageNum = query.getPageNum() == null ? 1 : query.getPageNum();
        int pageSize = query.getPageSize() == null ? 20 : Math.min(query.getPageSize(), 200);
        return gateway
                .findByCondition(
                        query.getUserId(),
                        query.getModule(),
                        query.getOperationType(),
                        query.isSuccess(),
                        query.getStartTime(),
                        query.getEndTime(),
                        pageNum,
                        pageSize)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    /**
     * Count operation logs by condition.
     */
    public long countByCondition(OperationLogQuery query) {
        Assert.notNull(query, "query must not be null");
        return gateway.countByCondition(
                query.getUserId(),
                query.getModule(),
                query.getOperationType(),
                query.isSuccess(),
                query.getStartTime(),
                query.getEndTime());
    }

    /**
     * Manually record an operation log (for scenarios without AOP).
     */
    public void record(
            String userId,
            String username,
            String module,
            String operationType,
            String description,
            Boolean success,
            String errorMessage,
            Long duration) {
        OperationLog record = OperationLog.builder()
                .id(UUID.randomUUID().toString().replace("-", ""))
                .userId(userId)
                .username(username)
                .module(module)
                .operationType(operationType)
                .description(description)
                .success(success)
                .errorMessage(errorMessage)
                .duration(duration)
                .operationTime(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();
        logAsyncWriter.saveOperationLog(record);
    }

    /**
     * Statistics: total / success / failure / rates, grouped counts and duration stats.
     */
    public LogStatisticsDTO statistics(LogStatisticsQuery query) {
        Assert.notNull(query, "query must not be null");
        String userId = query.getUserId();
        String module = query.getModule();
        String opType = query.getOperationType();
        LocalDateTime start = query.getStartTime();
        LocalDateTime end = query.getEndTime();

        long total = gateway.countByCondition(userId, module, opType, null, start, end);
        long success = gateway.countByCondition(userId, module, opType, true, start, end);
        long failure = total - success;
        double rate = total == 0 ? 0.0 : Math.round(success * 10000.0 / total) / 100.0;

        Map<String, Long> byModule = gateway.countGroupBy("module", userId, module, opType, null, start, end);
        Map<String, Long> byType = gateway.countGroupBy("operation_type", userId, module, opType, null, start, end);
        Map<String, Long> byDate = gateway.countGroupBy("date", userId, module, opType, null, start, end);
        Map<String, Object> duration = gateway.durationStats(userId, module, opType, start, end);

        LogStatisticsDTO dto = new LogStatisticsDTO();
        dto.setTotal(total);
        dto.setSuccessCount(success);
        dto.setFailureCount(failure);
        dto.setSuccessRate(rate);
        dto.setAvgDuration(
                duration.get("avgDuration") == null ? null : ((Number) duration.get("avgDuration")).doubleValue());
        dto.setMaxDuration(
                duration.get("maxDuration") == null ? null : ((Number) duration.get("maxDuration")).longValue());
        dto.setByModule(byModule.entrySet().stream()
                .map(e -> new StatItem(e.getKey(), e.getValue()))
                .toList());
        dto.setByOperationType(byType.entrySet().stream()
                .map(e -> new StatItem(e.getKey(), e.getValue()))
                .toList());
        dto.setByDate(byDate.entrySet().stream()
                .map(e -> new StatItem(e.getKey(), e.getValue()))
                .toList());
        return dto;
    }

    /**
     * Export operation logs as CSV, writing directly to the provided writer.
     *
     * <p>Caller is responsible for setting response headers:
     * <pre>
     *   Content-Type: text/csv; charset=UTF-8
     *   Content-Disposition: attachment; filename="operation_log.csv"
     * </pre>
     */
    public void exportCsv(OperationLogQuery query, PrintWriter writer) {
        Assert.notNull(query, "query must not be null");
        List<OperationLog> logs = gateway.findAllForExport(
                query.getUserId(),
                query.getModule(),
                query.getOperationType(),
                query.isSuccess(),
                query.getStartTime(),
                query.getEndTime());

        writer.println("ID,用户ID,用户名,模块,操作类型,描述,方法,执行时长(ms),是否成功,IP,操作时间");
        for (OperationLog l : logs) {
            writer.println(String.join(
                    ",",
                    safe(l.getId()),
                    safe(l.getUserId()),
                    safe(l.getUsername()),
                    safe(l.getModule()),
                    safe(l.getOperationType()),
                    safe(l.getDescription()),
                    safe(l.getMethod()),
                    l.getDuration() == null ? "" : l.getDuration().toString(),
                    l.isSuccess() == null ? "" : l.isSuccess().toString(),
                    safe(l.getIp()),
                    l.getOperationTime() == null ? "" : l.getOperationTime().format(FORMATTER)));
        }
        writer.flush();
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private String safe(String s) {
        if (s == null) {
            return "";
        }
        // escape CSV: wrap in quotes if contains comma/newline/quote
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    private OperationLogDTO toDTO(OperationLog m) {
        OperationLogDTO dto = new OperationLogDTO();
        dto.setId(m.getId());
        dto.setUserId(m.getUserId());
        dto.setUsername(m.getUsername());
        dto.setModule(m.getModule());
        dto.setOperationType(m.getOperationType());
        dto.setDescription(m.getDescription());
        dto.setMethod(m.getMethod());
        dto.setRequestParams(m.getRequestParams());
        dto.setResponseResult(m.getResponseResult());
        dto.setDuration(m.getDuration());
        dto.setSuccess(m.isSuccess());
        dto.setErrorMessage(m.getErrorMessage());
        dto.setIp(m.getIp());
        dto.setUserAgent(m.getUserAgent());
        dto.setOperationTime(m.getOperationTime());
        return dto;
    }

    public OperationLogService(OperationLogGateway gateway, LogAsyncWriter logAsyncWriter) {
        this.gateway = gateway;
        this.logAsyncWriter = logAsyncWriter;
    }
}
