package io.github.loadup.components.dfs.model;

import java.time.Instant;
import java.util.Map;

/** Immutable metadata returned by every DFS binder. */
public record FileMetadata(
        String fileId,
        String filename,
        long size,
        String contentType,
        String provider,
        String path,
        Map<String, String> metadata,
        Instant uploadedAt) {

    public FileMetadata {
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
