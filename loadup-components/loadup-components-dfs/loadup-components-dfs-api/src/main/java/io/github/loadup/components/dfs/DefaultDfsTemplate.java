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

public class DefaultDfsTemplate implements DfsTemplate {
    private final DfsProvider provider;

    public DefaultDfsTemplate(DfsProvider provider) {
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
    public String generatePresignedUrl(String fileId, long expirationSeconds) {
        return provider.generatePresignedUrl(fileId, expirationSeconds);
    }

    @Override
    public FileMetadata copy(String sourceFileId, String targetPath) {
        return provider.copy(sourceFileId, targetPath);
    }
}
