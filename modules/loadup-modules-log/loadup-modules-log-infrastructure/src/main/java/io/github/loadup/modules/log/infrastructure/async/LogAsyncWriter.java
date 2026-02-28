package io.github.loadup.modules.log.infrastructure.async;

import io.github.loadup.modules.log.domain.gateway.AuditLogGateway;
import io.github.loadup.modules.log.domain.gateway.OperationLogGateway;
import io.github.loadup.modules.log.domain.model.AuditLog;
import io.github.loadup.modules.log.domain.model.OperationLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Async log writer.
 *
 * <p>All methods run in the dedicated {@code logExecutor} thread pool so that
 * log persistence never blocks the business call thread.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LogAsyncWriter {

    private final OperationLogGateway operationLogGateway;
    private final AuditLogGateway auditLogGateway;

    @Async("logExecutor")
    public void saveOperationLog(OperationLog operationLog) {
        try {
            operationLogGateway.save(operationLog);
        } catch (Exception e) {
            log.error("Failed to save operation log: method={}, error={}",
                    operationLog.getMethod(), e.getMessage(), e);
        }
    }

    @Async("logExecutor")
    public void saveAuditLog(AuditLog auditLog) {
        try {
            auditLogGateway.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to save audit log: dataType={}, action={}, error={}",
                    auditLog.getDataType(), auditLog.getAction(), e.getMessage(), e);
        }
    }
}

