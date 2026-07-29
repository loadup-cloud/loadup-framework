package io.github.loadup.components.dfs;

/*-
 * #%L
 * Loadup Dfs Components Api
 * %%
 * Copyright (C) 2025 - 2026 loadup_cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
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
