package io.github.loadup.modules.contract.client.dto;

import io.github.loadup.commons.json.ToStringAsJson;
import java.time.LocalDateTime;

/** Trusted local contract header used while the caller holds its row lock. */
public record ContractPersistenceHeader(
        String id,
        String tenantId,
        String merchantId,
        String scopeKey,
        String planVersionId,
        String status,
        long generation,
        String requestKey,
        String requestDigest,
        String createdBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
    @Override
    public String toString() {
        return ToStringAsJson.reflectionToString(this);
    }
}
