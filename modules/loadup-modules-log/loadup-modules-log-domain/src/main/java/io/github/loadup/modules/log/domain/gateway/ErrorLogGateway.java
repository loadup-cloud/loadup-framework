package io.github.loadup.modules.log.domain.gateway;

import io.github.loadup.modules.log.domain.model.ErrorLog;
import java.time.LocalDateTime;
import java.util.List;

public interface ErrorLogGateway {

    void save(ErrorLog log);

    List<ErrorLog> findByCondition(String userId, String errorType, String errorCode,
            LocalDateTime startTime, LocalDateTime endTime, int pageNum, int pageSize);

    long countByCondition(String userId, String errorType, String errorCode,
            LocalDateTime startTime, LocalDateTime endTime);
}

