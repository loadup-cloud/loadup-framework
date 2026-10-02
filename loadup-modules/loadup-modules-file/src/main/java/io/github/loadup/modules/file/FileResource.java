package io.github.loadup.modules.file;

import java.time.LocalDateTime;

/** Business record for one object stored by DFS. */
public record FileResource(String id, String tenantId, String ownerId, String storageId, String filename,
        String contentType, long size, String provider, String state, LocalDateTime createdAt,
        LocalDateTime updatedAt) {}
