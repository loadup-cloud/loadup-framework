package io.github.loadup.modules.audit;

import java.time.LocalDateTime;

/** Tenant-scoped audit search. Tenant ID must come from trusted request context. */
public record AuditQuery(
        String tenantId,
        String actorId,
        String action,
        String outcome,
        LocalDateTime from,
        LocalDateTime to,
        int page,
        int size) {
    public AuditQuery {
        if (page < 1 || size < 1 || size > 100) {
            throw new IllegalArgumentException("page must be positive and size must be between 1 and 100");
        }
        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException("from must not be after to");
        }
    }
}
