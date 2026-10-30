/* Copyright 2026 LoadUp Cloud. Licensed under the Apache License, Version 2.0. */
package io.github.loadup.modules.file.infrastructure.dataobject;

import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/** Single file row with an application-assigned identifier, used for insertion and lookup. */
@Table("file_resource")
public record FileResourceInsert(
        @Id @Column("id") String id,
        @Column("tenant_id") String tenantId,
        @Column("owner_id") String ownerId,
        @Column("storage_id") String storageId,
        @Column("filename") String filename,
        @Column("content_type") String contentType,
        @Column("content_length") long contentLength,
        @Column("provider") String provider,
        @Column("state") String state,
        @Column("created_at") LocalDateTime createdAt,
        @Column("updated_at") LocalDateTime updatedAt,
        @Column("deleted") boolean deleted) {}
