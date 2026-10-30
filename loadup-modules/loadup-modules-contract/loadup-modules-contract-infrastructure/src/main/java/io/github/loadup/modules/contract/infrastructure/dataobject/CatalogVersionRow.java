package io.github.loadup.modules.contract.infrastructure.dataobject;

import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/** Row model for catalog versions, read through Spring Data JDBC. */
@Table("contract_catalog_version")
public record CatalogVersionRow(
        @Id @Column("id") String id,
        @Column("tenant_id") String tenantId,
        @Column("kind") String kind,
        @Column("code") String code,
        @Column("version") int version,
        @Column("status") String status,
        @Column("row_version") long rowVersion,
        @Column("content") String content,
        @Column("updated_by") String updatedBy,
        @Column("created_at") LocalDateTime createdAt,
        @Column("updated_at") LocalDateTime updatedAt,
        @Column("deleted") boolean deleted) {}
