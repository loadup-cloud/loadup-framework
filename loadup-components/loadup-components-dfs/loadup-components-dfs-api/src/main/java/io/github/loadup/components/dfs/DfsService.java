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
