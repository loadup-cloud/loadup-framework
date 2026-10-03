package io.github.loadup.modules.transfer;

import java.time.LocalDateTime;

/** Durable state of one import or export job. */
public record TransferTask(String id, String tenantId, String ownerId, TransferKind kind, String handlerKey,
        String sourceFileId, String resultFileId, TransferStatus status, long processedCount, long totalCount,
        String errorMessage, LocalDateTime createdAt, LocalDateTime startedAt, LocalDateTime finishedAt) {}
