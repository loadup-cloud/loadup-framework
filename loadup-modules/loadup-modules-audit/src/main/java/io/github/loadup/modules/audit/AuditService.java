package io.github.loadup.modules.audit;

import io.github.loadup.modules.audit.jdbc.JdbcAuditRepository;
import java.time.LocalDateTime;
import java.util.UUID;

/** Records and searches immutable audit events. */
public class AuditService {
    private final JdbcAuditRepository repository;

    public AuditService(JdbcAuditRepository repository) {
        this.repository = repository;
    }

    public void record(AuditWrite write) {
        if (write == null || write.action() == null || write.action().isBlank()) {
            throw new IllegalArgumentException("audit action is required");
        }
        repository.insert(new AuditEvent(
                UUID.randomUUID().toString(),
                write.tenantId(),
                write.actorId(),
                write.action(),
                write.method(),
                write.path(),
                write.outcome(),
                write.traceId(),
                LocalDateTime.now()));
    }

    public AuditPage search(AuditQuery query) {
        return repository.search(query);
    }
}
