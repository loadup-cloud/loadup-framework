package io.github.loadup.modules.contract.infrastructure.dataobject;

import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/** Row model for merchant contract headers, read through Spring Data JDBC. */
@Table("merchant_contract")
public record MerchantContractRow(
        @Id @Column("id") String id,
        @Column("tenant_id") String tenantId,
        @Column("merchant_id") String merchantId,
        @Column("scope_key") String scopeKey,
        @Column("plan_version_id") String planVersionId,
        @Column("status") String status,
        @Column("generation") long generation,
        @Column("request_key") String requestKey,
        @Column("request_digest") String requestDigest,
        @Column("created_by") String createdBy,
        @Column("created_at") LocalDateTime createdAt,
        @Column("updated_at") LocalDateTime updatedAt,
        @Column("deleted") boolean deleted) {}
