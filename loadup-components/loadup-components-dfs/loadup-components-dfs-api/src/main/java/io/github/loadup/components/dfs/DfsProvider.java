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

/** Storage SPI implemented by one binder selected at build and deployment time. */
public interface DfsProvider {

    FileMetadata upload(FileUploadRequest request);

    FileDownloadResponse download(String fileId);

    boolean delete(String fileId);

    boolean exists(String fileId);

    FileMetadata getMetadata(String fileId);

    String getBinderType();

    default URI generatePresignedDownloadUrl(String fileId, Duration expiration) {
        throw unsupported("presigned download URL");
    }

    default MultipartUpload initiateMultipartUpload(MultipartUploadRequest request) {
        throw unsupported("multipart upload");
    }

    default MultipartPart uploadPart(
            String fileId, String uploadId, int partNumber, InputStream content, long contentLength) {
        throw unsupported("multipart upload");
    }

    default FileMetadata completeMultipartUpload(String fileId, String uploadId, List<MultipartPart> parts) {
        throw unsupported("multipart upload");
    }

    default void abortMultipartUpload(String fileId, String uploadId) {
        throw unsupported("multipart upload");
    }

    private UnsupportedOperationException unsupported(String capability) {
        return new UnsupportedOperationException(getBinderType() + " DFS binder does not support " + capability);
    }
}
