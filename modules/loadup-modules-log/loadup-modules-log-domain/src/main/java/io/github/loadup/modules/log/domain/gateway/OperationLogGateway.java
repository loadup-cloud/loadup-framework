package io.github.loadup.modules.log.domain.gateway;

import io.github.loadup.modules.log.domain.model.OperationLog;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface OperationLogGateway {

    void save(OperationLog log);

    List<OperationLog> findByCondition(String userId, String module, String operationType,
            Boolean success, LocalDateTime startTime, LocalDateTime endTime,
            int pageNum, int pageSize);

    long countByCondition(String userId, String module, String operationType,
            Boolean success, LocalDateTime startTime, LocalDateTime endTime);

    /** Returns all matching logs (no pagination) for export. Max 10000 rows. */
    List<OperationLog> findAllForExport(String userId, String module, String operationType,
            Boolean success, LocalDateTime startTime, LocalDateTime endTime);

    /** Count grouped by a field name ("module" | "operation_type" | "date"). */
    Map<String, Long> countGroupBy(String groupField, String userId, String module,
            String operationType, Boolean success, LocalDateTime startTime, LocalDateTime endTime);

    /** Average and max duration stats. Returns map keys: avgDuration, maxDuration. */
    Map<String, Object> durationStats(String userId, String module, String operationType,
            LocalDateTime startTime, LocalDateTime endTime);
}



