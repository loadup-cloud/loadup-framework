package io.github.loadup.modules.contract.infrastructure.dataobject;

import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/** Row model for immutable contract revisions, read through Spring Data JDBC. */
@Table("merchant_contract_revision")
public record ContractRevisionRow(
        @Id @Column("id") String id,
        @Column("tenant_id") String tenantId,
        @Column("contract_id") String contractId,
        @Column("revision") int revision,
        @Column("snapshot") String snapshot,
        @Column("snapshot_hash") String snapshotHash,
        @Column("created_at") LocalDateTime createdAt,
        @Column("updated_at") LocalDateTime updatedAt,
        @Column("deleted") boolean deleted) {}
