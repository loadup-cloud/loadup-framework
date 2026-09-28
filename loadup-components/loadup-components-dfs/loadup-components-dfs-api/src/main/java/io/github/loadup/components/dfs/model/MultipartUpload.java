package io.github.loadup.components.dfs.model;

/** Identifier returned when a multipart upload is initiated. */
public record MultipartUpload(String fileId, String uploadId) {}
