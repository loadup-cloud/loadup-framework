package io.github.loadup.modules.dictionary;

import java.time.LocalDateTime;

/** Named collection of business dictionary items. */
public record DictionaryType(
        String id,
        String tenantId,
        String code,
        String name,
        String description,
        boolean enabled,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {}
