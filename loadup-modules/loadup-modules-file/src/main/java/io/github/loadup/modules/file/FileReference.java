package io.github.loadup.modules.file;

import java.time.LocalDateTime;

/** A business object's claim on a file; claims prevent deletion. */
public record FileReference(String id, String tenantId, String fileId, String referenceType,
        String referenceId, LocalDateTime createdAt) {}
