package io.github.loadup.modules.log.infrastructure.async;

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

import io.github.loadup.modules.log.domain.gateway.AuditLogGateway;
import io.github.loadup.modules.log.domain.gateway.ErrorLogGateway;
import io.github.loadup.modules.log.domain.gateway.OperationLogGateway;
import io.github.loadup.modules.log.domain.model.AuditLog;
import io.github.loadup.modules.log.domain.model.ErrorLog;
import io.github.loadup.modules.log.domain.model.OperationLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Async log writer.
 *
 * <p>All write methods run in the dedicated {@code logExecutor} thread pool
 * so that log persistence never blocks the business call thread.
 */
@Service
public class LogAsyncWriter {
    private static final Logger log = LoggerFactory.getLogger(LogAsyncWriter.class);

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

    public LogAsyncWriter(
            OperationLogGateway operationLogGateway, AuditLogGateway auditLogGateway, ErrorLogGateway errorLogGateway) {
        this.operationLogGateway = operationLogGateway;
        this.auditLogGateway = auditLogGateway;
        this.errorLogGateway = errorLogGateway;
    }
}
