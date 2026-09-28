package io.github.loadup.components.dfs;

import io.github.loadup.components.dfs.model.FileDownloadResponse;
import io.github.loadup.components.dfs.model.FileMetadata;
import io.github.loadup.components.dfs.model.FileUploadRequest;
import io.github.loadup.components.dfs.model.MultipartPart;
import io.github.loadup.components.dfs.model.MultipartUpload;
import io.github.loadup.components.dfs.model.MultipartUploadRequest;
import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.util.List;

/** Default thin facade that delegates to the single configured {@link DfsProvider}. */
public final class DefaultDfsService implements DfsService {
    private final DfsProvider provider;

    public DefaultDfsService(DfsProvider provider) {
        this.provider = provider;
    }

    @Override
    public FileMetadata upload(FileUploadRequest request) {
        return provider.upload(request);
    }

    @Override
    public FileDownloadResponse download(String fileId) {
        return provider.download(fileId);
    }

    @Override
    public boolean delete(String fileId) {
        return provider.delete(fileId);
    }

    @Override
    public boolean exists(String fileId) {
        return provider.exists(fileId);
    }

    @Override
    public FileMetadata getMetadata(String fileId) {
        return provider.getMetadata(fileId);
    }

    @Override
    public URI generatePresignedDownloadUrl(String fileId, Duration expiration) {
        return provider.generatePresignedDownloadUrl(fileId, expiration);
    }

    @Override
    public MultipartUpload initiateMultipartUpload(MultipartUploadRequest request) {
        return provider.initiateMultipartUpload(request);
    }

    @Override
    public MultipartPart uploadPart(
            String fileId, String uploadId, int partNumber, InputStream content, long contentLength) {
        return provider.uploadPart(fileId, uploadId, partNumber, content, contentLength);
    }

    @Override
    public FileMetadata completeMultipartUpload(String fileId, String uploadId, List<MultipartPart> parts) {
        return provider.completeMultipartUpload(fileId, uploadId, parts);
    }

    @Override
    public void abortMultipartUpload(String fileId, String uploadId) {
        provider.abortMultipartUpload(fileId, uploadId);
    }
}
