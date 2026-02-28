package io.github.loadup.modules.log.infrastructure.repository;

import static io.github.loadup.modules.log.infrastructure.dataobject.table.Tables.AUDIT_LOG_DO;

import com.mybatisflex.core.query.QueryWrapper;
import io.github.loadup.modules.log.domain.gateway.AuditLogGateway;
import io.github.loadup.modules.log.domain.model.AuditLog;
import io.github.loadup.modules.log.infrastructure.mapper.AuditLogDOMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
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
    public List<AuditLog> findByCondition(String userId, String dataType, String dataId,
            String action, LocalDateTime startTime, LocalDateTime endTime, int pageNum, int pageSize) {
        QueryWrapper qw = buildQuery(userId, dataType, dataId, action, startTime, endTime);
        qw.orderBy(AUDIT_LOG_DO.OPERATION_TIME.desc());
        qw.limit((long) (pageNum - 1) * pageSize, pageSize);
        return mapper.selectListByQuery(qw).stream().map(this::toModel).toList();
    }

    @Override
    public long countByCondition(String userId, String dataType, String dataId,
            String action, LocalDateTime startTime, LocalDateTime endTime) {
        return mapper.selectCountByQuery(buildQuery(userId, dataType, dataId, action, startTime, endTime));
    }

    private QueryWrapper buildQuery(String userId, String dataType, String dataId,
            String action, LocalDateTime startTime, LocalDateTime endTime) {
        QueryWrapper qw = QueryWrapper.create();
        if (userId != null) qw.and(AUDIT_LOG_DO.USER_ID.eq(userId));
        if (dataType != null) qw.and(AUDIT_LOG_DO.DATA_TYPE.eq(dataType));
        if (dataId != null) qw.and(AUDIT_LOG_DO.DATA_ID.eq(dataId));
        if (action != null) qw.and(AUDIT_LOG_DO.ACTION.eq(action));
        if (startTime != null) qw.and(AUDIT_LOG_DO.OPERATION_TIME.ge(startTime));
        if (endTime != null) qw.and(AUDIT_LOG_DO.OPERATION_TIME.le(endTime));
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
}

