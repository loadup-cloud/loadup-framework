/*-
 * #%L
 * Loadup Dfs Components Api
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
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
