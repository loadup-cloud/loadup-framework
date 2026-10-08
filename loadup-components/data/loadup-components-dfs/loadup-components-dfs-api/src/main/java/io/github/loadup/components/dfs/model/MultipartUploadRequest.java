package io.github.loadup.components.dfs.model;

import java.util.Map;
import java.util.Objects;

/** Metadata used to initiate an S3-compatible multipart upload. */
public record MultipartUploadRequest(String filename, String contentType, String path, Map<String, String> metadata) {

    public MultipartUploadRequest {
        Objects.requireNonNull(filename, "filename must not be null");
        if (filename.isBlank()) {
            throw new IllegalArgumentException("filename must not be blank");
        }
        contentType = contentType == null || contentType.isBlank() ? "application/octet-stream" : contentType;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
