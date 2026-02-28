package io.github.loadup.modules.log.domain.gateway;

import io.github.loadup.modules.log.domain.model.AuditLog;
import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogGateway {

    void save(AuditLog log);

    List<AuditLog> findByCondition(String userId, String dataType, String dataId, String action,
            LocalDateTime startTime, LocalDateTime endTime, int pageNum, int pageSize);

    long countByCondition(String userId, String dataType, String dataId, String action,
            LocalDateTime startTime, LocalDateTime endTime);
}

