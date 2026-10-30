/* Copyright 2026 LoadUp Cloud. Licensed under the Apache License, Version 2.0. */
package io.github.loadup.components.dfs.database.dataobject;

import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/** A database DFS object with an application-assigned ID. */
@Table("dfs_file")
public record DfsFileInsert(
        @Id @Column("id") String id,
        @Column("tenant_id") String tenantId,
        @Column("filename") String filename,
        @Column("file_size") long fileSize,
        @Column("content_type") String contentType,
        @Column("content") byte[] content,
        @Column("metadata_json") String metadataJson,
        @Column("created_at") LocalDateTime createdAt,
        @Column("updated_at") LocalDateTime updatedAt,
        @Column("deleted") boolean deleted) {}
