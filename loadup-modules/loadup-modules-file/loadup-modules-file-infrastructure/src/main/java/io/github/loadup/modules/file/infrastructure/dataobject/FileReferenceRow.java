/* Copyright 2026 LoadUp Cloud. Licensed under the Apache License, Version 2.0. */
package io.github.loadup.modules.file.infrastructure.dataobject;

import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/** A reference row; parent-file lifecycle is managed explicitly by the gateway. */
@Table("file_resource_reference")
public record FileReferenceRow(
        @Id @Column("id") String id,
        @Column("tenant_id") String tenantId,
        @Column("file_id") String fileId,
        @Column("reference_type") String referenceType,
        @Column("reference_id") String referenceId,
        @Column("created_at") LocalDateTime createdAt,
        @Column("updated_at") LocalDateTime updatedAt,
        @Column("deleted") boolean deleted) {}
