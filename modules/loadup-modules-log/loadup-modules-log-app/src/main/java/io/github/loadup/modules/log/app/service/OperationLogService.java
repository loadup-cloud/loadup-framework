package io.github.loadup.modules.log.app.service;

import io.github.loadup.modules.log.client.dto.OperationLogDTO;
import io.github.loadup.modules.log.client.query.OperationLogQuery;
import io.github.loadup.modules.log.domain.gateway.OperationLogGateway;
import io.github.loadup.modules.log.domain.model.OperationLog;
import io.github.loadup.modules.log.infrastructure.async.LogAsyncWriter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * Application service for operation log management.
 *
 * <p>Exposed to Gateway via {@code bean://operationLogService:method}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OperationLogService {

    private final OperationLogGateway gateway;
    private final LogAsyncWriter logAsyncWriter;

    /** Query operation logs by condition (paginated). */
    public List<OperationLogDTO> listByCondition(OperationLogQuery query) {
        Assert.notNull(query, "query must not be null");
        int pageNum = query.getPageNum() == null ? 1 : query.getPageNum();
        int pageSize = query.getPageSize() == null ? 20 : Math.min(query.getPageSize(), 200);
        return gateway.findByCondition(
                        query.getUserId(), query.getModule(), query.getOperationType(),
                        query.getSuccess(), query.getStartTime(), query.getEndTime(),
                        pageNum, pageSize)
                .stream().map(this::toDTO).toList();
    }

    /** Count operation logs by condition. */
    public long countByCondition(OperationLogQuery query) {
        Assert.notNull(query, "query must not be null");
        return gateway.countByCondition(
                query.getUserId(), query.getModule(), query.getOperationType(),
                query.getSuccess(), query.getStartTime(), query.getEndTime());
    }

    /** Manually record an operation log (for scenarios without AOP). */
    public void record(String userId, String username, String module, String operationType,
            String description, Boolean success, String errorMessage, Long duration) {
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
        dto.setSuccess(m.getSuccess());
        dto.setErrorMessage(m.getErrorMessage());
        dto.setIp(m.getIp());
        dto.setUserAgent(m.getUserAgent());
        dto.setOperationTime(m.getOperationTime());
        return dto;
    }
}

