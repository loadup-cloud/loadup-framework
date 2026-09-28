package io.github.loadup.components.globalunique.model;

import java.time.LocalDateTime;

/** Read-only view of a claimed business key. */
public record GlobalUniqueRecord(
        String id,
        String tenantId,
        String bizType,
        String uniqueKey,
        String bizId,
        String requestData,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {}
