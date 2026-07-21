package io.github.loadup.components.dfs;

import io.github.loadup.components.dfs.model.FileDownloadResponse;
import io.github.loadup.components.dfs.model.FileMetadata;
import io.github.loadup.components.dfs.model.FileUploadRequest;

public interface DfsTemplate {
    FileMetadata upload(FileUploadRequest request);
    FileDownloadResponse download(String fileId);
    boolean delete(String fileId);
    boolean exists(String fileId);
    FileMetadata getMetadata(String fileId);
    default String generatePresignedUrl(String fileId, long expirationSeconds) { throw new UnsupportedOperationException(); }
    default FileMetadata copy(String sourceFileId, String targetPath) { throw new UnsupportedOperationException(); }
}
