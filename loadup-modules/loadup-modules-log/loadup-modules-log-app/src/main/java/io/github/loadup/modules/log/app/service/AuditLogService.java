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

import io.github.loadup.modules.log.client.dto.AuditLogDTO;
import io.github.loadup.modules.log.client.query.AuditLogQuery;
import io.github.loadup.modules.log.domain.gateway.AuditLogGateway;
import io.github.loadup.modules.log.domain.model.AuditLog;
import io.github.loadup.modules.log.infrastructure.async.LogAsyncWriter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * Application service for audit log management.
 *
 * <p>Exposed to Gateway via {@code bean://auditLogService:method}.
 */
@Service
public class AuditLogService {
    private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);

    private final AuditLogGateway gateway;
    private final LogAsyncWriter logAsyncWriter;

    /**
     * Query audit logs by condition (paginated).
     */
    public List<AuditLogDTO> listByCondition(AuditLogQuery query) {
        Assert.notNull(query, "query must not be null");
        int pageNum = query.getPageNum() == null ? 1 : query.getPageNum();
        int pageSize = query.getPageSize() == null ? 20 : Math.min(query.getPageSize(), 200);
        return gateway
                .findByCondition(
                        query.getUserId(),
                        query.getDataType(),
                        query.getDataId(),
                        query.getAction(),
                        query.getStartTime(),
                        query.getEndTime(),
                        pageNum,
                        pageSize)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    /**
     * Count audit logs by condition.
     */
    public long countByCondition(AuditLogQuery query) {
        Assert.notNull(query, "query must not be null");
        return gateway.countByCondition(
                query.getUserId(),
                query.getDataType(),
                query.getDataId(),
                query.getAction(),
                query.getStartTime(),
                query.getEndTime());
    }

    /**
     * Record an audit log entry manually.
     *
     * @param dataType type of entity changed, e.g. "USER"
     * @param dataId   ID of the entity
     * @param action   what happened, e.g. "CREATE", "UPDATE", "DELETE"
     * @param before   JSON of the entity before change (nullable)
     * @param after    JSON of the entity after change (nullable)
     */
    public void record(
            String userId,
            String username,
            String dataType,
            String dataId,
            String action,
            String before,
            String after,
            String reason,
            String ip) {
        AuditLog record = AuditLog.builder()
                .id(UUID.randomUUID().toString().replace("-", ""))
                .userId(userId)
                .username(username)
                .dataType(dataType)
                .dataId(dataId)
                .action(action)
                .beforeData(before)
                .afterData(after)
                .reason(reason)
                .ip(ip)
                .operationTime(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();
        logAsyncWriter.saveAuditLog(record);
    }

    private AuditLogDTO toDTO(AuditLog m) {
        AuditLogDTO dto = new AuditLogDTO();
        dto.setId(m.getId());
        dto.setUserId(m.getUserId());
        dto.setUsername(m.getUsername());
        dto.setDataType(m.getDataType());
        dto.setDataId(m.getDataId());
        dto.setAction(m.getAction());
        dto.setBeforeData(m.getBeforeData());
        dto.setAfterData(m.getAfterData());
        dto.setDiffData(m.getDiffData());
        dto.setReason(m.getReason());
        dto.setIp(m.getIp());
        dto.setOperationTime(m.getOperationTime());
        return dto;
    }

    public AuditLogService(AuditLogGateway gateway, LogAsyncWriter logAsyncWriter) {
        this.gateway = gateway;
        this.logAsyncWriter = logAsyncWriter;
    }
}
