package io.github.loadup.components.dfs;

import io.github.loadup.components.dfs.model.FileDownloadResponse;
import io.github.loadup.components.dfs.model.FileMetadata;
import io.github.loadup.components.dfs.model.FileUploadRequest;

public class DefaultDfsTemplate implements DfsTemplate {
    private final DfsProvider provider;

    public DefaultDfsTemplate(DfsProvider provider) { this.provider = provider; }

    @Override public FileMetadata upload(FileUploadRequest request) { return provider.upload(request); }
    @Override public FileDownloadResponse download(String fileId) { return provider.download(fileId); }
    @Override public boolean delete(String fileId) { return provider.delete(fileId); }
    @Override public boolean exists(String fileId) { return provider.exists(fileId); }
    @Override public FileMetadata getMetadata(String fileId) { return provider.getMetadata(fileId); }
    @Override public String generatePresignedUrl(String fileId, long expirationSeconds) { return provider.generatePresignedUrl(fileId, expirationSeconds); }
    @Override public FileMetadata copy(String sourceFileId, String targetPath) { return provider.copy(sourceFileId, targetPath); }
}
