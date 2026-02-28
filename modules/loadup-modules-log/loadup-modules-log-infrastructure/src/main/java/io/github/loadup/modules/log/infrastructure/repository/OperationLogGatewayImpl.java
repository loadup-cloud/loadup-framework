package io.github.loadup.modules.log.infrastructure.repository;

import static io.github.loadup.modules.log.infrastructure.dataobject.table.Tables.OPERATION_LOG_DO;

import com.mybatisflex.core.query.QueryWrapper;
import io.github.loadup.modules.log.domain.gateway.OperationLogGateway;
import io.github.loadup.modules.log.domain.model.OperationLog;
import io.github.loadup.modules.log.infrastructure.mapper.OperationLogDOMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OperationLogGatewayImpl implements OperationLogGateway {

    private final OperationLogDOMapper mapper;

    @Override
    public void save(OperationLog log) {
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
    public List<OperationLog> findByCondition(String userId, String module, String operationType,
            Boolean success, LocalDateTime startTime, LocalDateTime endTime, int pageNum, int pageSize) {
        QueryWrapper qw = buildQuery(userId, module, operationType, success, startTime, endTime);
        qw.orderBy(OPERATION_LOG_DO.OPERATION_TIME.desc());
        qw.limit((long) (pageNum - 1) * pageSize, pageSize);
        return mapper.selectListByQuery(qw).stream().map(this::toModel).toList();
    }

    @Override
    public long countByCondition(String userId, String module, String operationType,
            Boolean success, LocalDateTime startTime, LocalDateTime endTime) {
        return mapper.selectCountByQuery(buildQuery(userId, module, operationType, success, startTime, endTime));
    }

    private QueryWrapper buildQuery(String userId, String module, String operationType,
            Boolean success, LocalDateTime startTime, LocalDateTime endTime) {
        QueryWrapper qw = QueryWrapper.create();
        if (userId != null) qw.and(OPERATION_LOG_DO.USER_ID.eq(userId));
        if (module != null) qw.and(OPERATION_LOG_DO.MODULE.eq(module));
        if (operationType != null) qw.and(OPERATION_LOG_DO.OPERATION_TYPE.eq(operationType));
        if (success != null) qw.and(OPERATION_LOG_DO.SUCCESS.eq(success));
        if (startTime != null) qw.and(OPERATION_LOG_DO.OPERATION_TIME.ge(startTime));
        if (endTime != null) qw.and(OPERATION_LOG_DO.OPERATION_TIME.le(endTime));
        return qw;
    }

    private io.github.loadup.modules.log.infrastructure.dataobject.OperationLogDO toEntity(OperationLog m) {
        var e = new io.github.loadup.modules.log.infrastructure.dataobject.OperationLogDO();
        e.setId(m.getId());
        e.setUserId(m.getUserId());
        e.setUsername(m.getUsername());
        e.setModule(m.getModule());
        e.setOperationType(m.getOperationType());
        e.setDescription(m.getDescription());
        e.setMethod(m.getMethod());
        e.setRequestParams(m.getRequestParams());
        e.setResponseResult(m.getResponseResult());
        e.setDuration(m.getDuration());
        e.setSuccess(m.getSuccess());
        e.setErrorMessage(m.getErrorMessage());
        e.setIp(m.getIp());
        e.setUserAgent(m.getUserAgent());
        e.setOperationTime(m.getOperationTime());
        e.setCreatedAt(m.getCreatedAt());
        return e;
    }

    private OperationLog toModel(io.github.loadup.modules.log.infrastructure.dataobject.OperationLogDO e) {
        return OperationLog.builder()
                .id(e.getId())
                .userId(e.getUserId())
                .username(e.getUsername())
                .module(e.getModule())
                .operationType(e.getOperationType())
                .description(e.getDescription())
                .method(e.getMethod())
                .requestParams(e.getRequestParams())
                .responseResult(e.getResponseResult())
                .duration(e.getDuration())
                .success(e.getSuccess())
                .errorMessage(e.getErrorMessage())
                .ip(e.getIp())
                .userAgent(e.getUserAgent())
                .operationTime(e.getOperationTime())
                .createdAt(e.getCreatedAt())
                .build();
    }
}

