/* Copyright 2026 LoadUp Cloud. Licensed under the Apache License, Version 2.0. */
package io.github.loadup.modules.audit.infrastructure.dataobject;

import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/** Append-only audit row; its application-assigned ID is always inserted explicitly. */
@Table("audit_event")
public record AuditEventInsert(
        @Id @Column("id") String id,
        @Column("tenant_id") String tenantId,
        @Column("actor_id") String actorId,
        @Column("action") String action,
        @Column("http_method") String method,
        @Column("request_path") String path,
        @Column("outcome") String outcome,
        @Column("trace_id") String traceId,
        @Column("occurred_at") LocalDateTime occurredAt,
        @Column("created_at") LocalDateTime createdAt,
        @Column("updated_at") LocalDateTime updatedAt,
        @Column("deleted") boolean deleted) {}
