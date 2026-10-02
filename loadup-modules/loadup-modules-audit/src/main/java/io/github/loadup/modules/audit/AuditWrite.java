package io.github.loadup.modules.audit;

/** Safe, metadata-only audit input. Never put credentials or request bodies in this record. */
public record AuditWrite(
        String tenantId,
        String actorId,
        String action,
        String method,
        String path,
        String outcome,
        String traceId) {}
