package io.github.loadup.modules.log.infrastructure.repository;

/*-
 * #%L
 * Loadup Modules Log Infrastructure
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

import static io.github.loadup.modules.log.infrastructure.dataobject.table.Tables.AUDIT_LOG_DO;

import com.mybatisflex.core.query.QueryWrapper;
import io.github.loadup.modules.log.domain.gateway.AuditLogGateway;
import io.github.loadup.modules.log.domain.model.AuditLog;
import io.github.loadup.modules.log.infrastructure.dataobject.AuditLogDO;
import io.github.loadup.modules.log.infrastructure.mapper.AuditLogDOMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class AuditLogGatewayImpl implements AuditLogGateway {

    private final AuditLogDOMapper mapper;

    @Override
    public void save(AuditLog log) {
        var entity = toEntity(log);
        if (entity.getId() == null) {
            entity.setId(UUID.randomUUID().toString().replace("-", ""));
        }
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(LocalDateTime.now());
        }
        mapper.insert(entity);
    }

    @Override
    public List<AuditLog> findByCondition(
            String userId,
            String dataType,
            String dataId,
            String action,
            LocalDateTime startTime,
            LocalDateTime endTime,
            int pageNum,
            int pageSize) {
        QueryWrapper qw = buildQuery(userId, dataType, dataId, action, startTime, endTime);
        qw.orderBy(AUDIT_LOG_DO.OPERATION_TIME.desc());
        qw.limit((long) (pageNum - 1) * pageSize, pageSize);
        List<AuditLogDO> entities = mapper.selectListByQuery(qw);
        if (entities == null) {
            return List.of();
        }
        return entities.stream().map(this::toModel).toList();
    }

    @Override
    public long countByCondition(
            String userId,
            String dataType,
            String dataId,
            String action,
            LocalDateTime startTime,
            LocalDateTime endTime) {
        return mapper.selectCountByQuery(buildQuery(userId, dataType, dataId, action, startTime, endTime));
    }

    private QueryWrapper buildQuery(
            String userId,
            String dataType,
            String dataId,
            String action,
            LocalDateTime startTime,
            LocalDateTime endTime) {
        QueryWrapper qw = QueryWrapper.create();
        if (userId != null) {
            qw.and(AUDIT_LOG_DO.USER_ID.eq(userId));
        }
        if (dataType != null) {
            qw.and(AUDIT_LOG_DO.DATA_TYPE.eq(dataType));
        }
        if (dataId != null) {
            qw.and(AUDIT_LOG_DO.DATA_ID.eq(dataId));
        }
        if (action != null) {
            qw.and(AUDIT_LOG_DO.ACTION.eq(action));
        }
        if (startTime != null) {
            qw.and(AUDIT_LOG_DO.OPERATION_TIME.ge(startTime));
        }
        if (endTime != null) {
            qw.and(AUDIT_LOG_DO.OPERATION_TIME.le(endTime));
        }
        return qw;
    }

    private io.github.loadup.modules.log.infrastructure.dataobject.AuditLogDO toEntity(AuditLog m) {
        var e = new io.github.loadup.modules.log.infrastructure.dataobject.AuditLogDO();
        e.setId(m.getId());
        e.setUserId(m.getUserId());
        e.setUsername(m.getUsername());
        e.setDataType(m.getDataType());
        e.setDataId(m.getDataId());
        e.setAction(m.getAction());
        e.setBeforeData(m.getBeforeData());
        e.setAfterData(m.getAfterData());
        e.setDiffData(m.getDiffData());
        e.setReason(m.getReason());
        e.setIp(m.getIp());
        e.setOperationTime(m.getOperationTime());
        e.setCreatedAt(m.getCreatedAt());
        return e;
    }

    private AuditLog toModel(io.github.loadup.modules.log.infrastructure.dataobject.AuditLogDO e) {
        return AuditLog.builder()
                .id(e.getId())
                .userId(e.getUserId())
                .username(e.getUsername())
                .dataType(e.getDataType())
                .dataId(e.getDataId())
                .action(e.getAction())
                .beforeData(e.getBeforeData())
                .afterData(e.getAfterData())
                .diffData(e.getDiffData())
                .reason(e.getReason())
                .ip(e.getIp())
                .operationTime(e.getOperationTime())
                .createdAt(e.getCreatedAt())
                .build();
    }

    public AuditLogGatewayImpl(AuditLogDOMapper mapper) {
        this.mapper = mapper;
    }
}
