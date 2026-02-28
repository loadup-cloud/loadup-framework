package io.github.loadup.modules.log.infrastructure.async;

/*-
 * #%L
 * Loadup Modules Log Infrastructure
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import io.github.loadup.modules.log.domain.gateway.AuditLogGateway;
import io.github.loadup.modules.log.domain.gateway.ErrorLogGateway;
import io.github.loadup.modules.log.domain.gateway.OperationLogGateway;
import io.github.loadup.modules.log.domain.model.AuditLog;
import io.github.loadup.modules.log.domain.model.ErrorLog;
import io.github.loadup.modules.log.domain.model.OperationLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Async log writer.
 *
 * <p>All write methods run in the dedicated {@code logExecutor} thread pool
 * so that log persistence never blocks the business call thread.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LogAsyncWriter {

    private final OperationLogGateway operationLogGateway;
    private final AuditLogGateway auditLogGateway;
    private final ErrorLogGateway errorLogGateway;

    @Async("logExecutor")
    public void saveOperationLog(OperationLog operationLog) {
        try {
            operationLogGateway.save(operationLog);
        } catch (Exception e) {
            log.error("Failed to save operation log: method={}, error={}", operationLog.getMethod(), e.getMessage(), e);
        }
    }

    @Async("logExecutor")
    public void saveAuditLog(AuditLog auditLog) {
        try {
            auditLogGateway.save(auditLog);
        } catch (Exception e) {
            log.error(
                    "Failed to save audit log: dataType={}, action={}, error={}",
                    auditLog.getDataType(),
                    auditLog.getAction(),
                    e.getMessage(),
                    e);
        }
    }

    @Async("logExecutor")
    public void saveErrorLog(ErrorLog errorLog) {
        try {
            errorLogGateway.save(errorLog);
        } catch (Exception e) {
            log.error(
                    "Failed to save error log: errorType={}, message={}, error={}",
                    errorLog.getErrorType(),
                    errorLog.getErrorMessage(),
                    e.getMessage(),
                    e);
        }
    }
}
