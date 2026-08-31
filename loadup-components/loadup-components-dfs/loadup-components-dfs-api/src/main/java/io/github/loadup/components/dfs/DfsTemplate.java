package io.github.loadup.components.dfs;

/*-
 * #%L
 * Loadup Dfs Components Api
 * %%
 * Copyright (C) 2025 - 2026 loadup_cloud
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

import io.github.loadup.components.dfs.model.FileDownloadResponse;
import io.github.loadup.components.dfs.model.FileMetadata;
import io.github.loadup.components.dfs.model.FileUploadRequest;

public interface DfsTemplate {
    FileMetadata upload(FileUploadRequest request);

    FileDownloadResponse download(String fileId);

    boolean delete(String fileId);

    boolean exists(String fileId);

    FileMetadata getMetadata(String fileId);

    default String generatePresignedUrl(String fileId, long expirationSeconds) {
        throw new UnsupportedOperationException();
    }

    default FileMetadata copy(String sourceFileId, String targetPath) {
        throw new UnsupportedOperationException();
    }
}
