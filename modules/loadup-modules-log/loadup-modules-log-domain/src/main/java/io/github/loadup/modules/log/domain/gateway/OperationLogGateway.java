package io.github.loadup.modules.log.domain.gateway;

import io.github.loadup.modules.log.domain.model.OperationLog;
import java.time.LocalDateTime;
import java.util.List;

public interface OperationLogGateway {

    void save(OperationLog log);

    List<OperationLog> findByCondition(String userId, String module, String operationType,
            Boolean success, LocalDateTime startTime, LocalDateTime endTime,
            int pageNum, int pageSize);

    long countByCondition(String userId, String module, String operationType,
            Boolean success, LocalDateTime startTime, LocalDateTime endTime);
}

