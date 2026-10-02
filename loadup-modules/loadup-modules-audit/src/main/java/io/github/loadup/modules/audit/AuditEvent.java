package io.github.loadup.modules.audit;

import java.time.LocalDateTime;

/** Stored audit event. */
public record AuditEvent(
        String id,
        String tenantId,
        String actorId,
        String action,
        String method,
        String path,
        String outcome,
        String traceId,
        LocalDateTime occurredAt) {}
