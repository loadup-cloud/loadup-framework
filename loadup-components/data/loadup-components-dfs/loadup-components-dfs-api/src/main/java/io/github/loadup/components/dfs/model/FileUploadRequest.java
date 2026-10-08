package io.github.loadup.components.dfs.model;

import java.io.InputStream;
import java.util.Map;
import java.util.Objects;

/** Request for uploading one object. The caller retains ownership of {@code content}. */
public record FileUploadRequest(
        String filename,
        InputStream content,
        long contentLength,
        String contentType,
        String path,
        Map<String, String> metadata) {

    public FileUploadRequest {
        Objects.requireNonNull(filename, "filename must not be null");
        Objects.requireNonNull(content, "content must not be null");
        if (filename.isBlank()) {
            throw new IllegalArgumentException("filename must not be blank");
        }
        if (contentLength < 0) {
            throw new IllegalArgumentException("contentLength must not be negative");
        }
        contentType = contentType == null || contentType.isBlank() ? "application/octet-stream" : contentType;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public static FileUploadRequest of(String filename, InputStream content, long contentLength, String contentType) {
        return new FileUploadRequest(filename, content, contentLength, contentType, null, Map.of());
    }
}
