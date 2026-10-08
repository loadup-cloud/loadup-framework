package io.github.loadup.components.dfs.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/** Download response whose content stream must be closed by the caller. */
public record FileDownloadResponse(FileMetadata metadata, InputStream content, long contentLength)
        implements AutoCloseable {

    public FileDownloadResponse {
        Objects.requireNonNull(metadata, "metadata must not be null");
        Objects.requireNonNull(content, "content must not be null");
        if (contentLength < 0) {
            throw new IllegalArgumentException("contentLength must not be negative");
        }
    }

    @Override
    public void close() throws IOException {
        content.close();
    }
}
