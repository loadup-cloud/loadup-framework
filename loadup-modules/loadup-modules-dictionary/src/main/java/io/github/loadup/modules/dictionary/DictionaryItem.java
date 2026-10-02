package io.github.loadup.modules.dictionary;

import java.time.LocalDateTime;

/** Value and display label within a dictionary type. */
public record DictionaryItem(
        String id,
        String tenantId,
        String typeId,
        String value,
        String label,
        String description,
        int sortOrder,
        boolean enabled,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {}
