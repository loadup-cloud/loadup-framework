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

/** Business-facing facade for the selected file storage binder. */
public interface DfsService {

    FileMetadata upload(FileUploadRequest request);

    FileDownloadResponse download(String fileId);

    boolean delete(String fileId);

    boolean exists(String fileId);

    FileMetadata getMetadata(String fileId);

    URI generatePresignedDownloadUrl(String fileId, Duration expiration);

    MultipartUpload initiateMultipartUpload(MultipartUploadRequest request);

    MultipartPart uploadPart(String fileId, String uploadId, int partNumber, InputStream content, long contentLength);

    FileMetadata completeMultipartUpload(String fileId, String uploadId, List<MultipartPart> parts);

    void abortMultipartUpload(String fileId, String uploadId);
}
