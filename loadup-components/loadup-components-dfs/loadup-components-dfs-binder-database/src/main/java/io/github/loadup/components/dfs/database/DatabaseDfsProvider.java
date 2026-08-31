package io.github.loadup.components.dfs.database;

/*-
 * #%L
 * Loadup Dfs Binder Database
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

import com.mybatisflex.core.query.QueryWrapper;
import io.github.loadup.commons.util.IdUtils;
import io.github.loadup.components.dfs.DfsProvider;
import io.github.loadup.components.dfs.database.dataobject.FileStorageEntity;
import io.github.loadup.components.dfs.database.mapper.FileStorageMapper;
import io.github.loadup.components.dfs.model.FileDownloadResponse;
import io.github.loadup.components.dfs.model.FileMetadata;
import io.github.loadup.components.dfs.model.FileUploadRequest;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class DatabaseDfsProvider implements DfsProvider {
    private final FileStorageMapper mapper;

    public DatabaseDfsProvider(FileStorageMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public FileMetadata upload(FileUploadRequest request) {
        String fileId = IdUtils.uuid2();
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            request.getInputStream().transferTo(baos);
            byte[] bytes = baos.toByteArray();

            FileStorageEntity entity = new FileStorageEntity();
            entity.setId(fileId);
            entity.setFilename(request.getFilename());
            entity.setFileSize((long) bytes.length);
            entity.setContentType(request.getContentType());
            entity.setContent(bytes);
            entity.setBizType(request.getBizType());
            entity.setBizId(request.getBizId());
            mapper.insert(entity);

            FileMetadata fileMetadata = new FileMetadata();
            fileMetadata.setFileId(fileId);
            fileMetadata.setFilename(request.getFilename());
            fileMetadata.setSize((long) bytes.length);
            fileMetadata.setContentType(request.getContentType());
            return fileMetadata;
        } catch (IOException e) {
            throw new RuntimeException("Database upload failed", e);
        }
    }

    @Override
    public FileDownloadResponse download(String fileId) {
        FileStorageEntity entity = mapper.selectOneByQuery(QueryWrapper.create().eq("id", fileId));
        if (entity == null) return null;
        FileDownloadResponse fileDownloadResponse = new FileDownloadResponse();
        fileDownloadResponse.setInputStream(new ByteArrayInputStream(entity.getContent()));

        FileMetadata fileMetadata = new FileMetadata();
        fileMetadata.setFileId(fileId);
        fileMetadata.setFilename(entity.getFilename());
        fileMetadata.setSize(entity.getFileSize());
        fileMetadata.setContentType(entity.getContentType());
        fileDownloadResponse.setMetadata(fileMetadata);
        return fileDownloadResponse;
    }

    @Override
    public boolean delete(String fileId) {
        return mapper.deleteByQuery(QueryWrapper.create().eq("id", fileId)) > 0;
    }

    @Override
    public boolean exists(String fileId) {
        return mapper.selectCountByQuery(QueryWrapper.create().eq("id", fileId)) > 0;
    }

    @Override
    public FileMetadata getMetadata(String fileId) {
        FileStorageEntity entity = mapper.selectOneByQuery(QueryWrapper.create().eq("id", fileId));
        if (entity == null) return null;

        FileMetadata fileMetadata = new FileMetadata();
        fileMetadata.setFileId(fileId);
        fileMetadata.setFilename(entity.getFilename());
        fileMetadata.setSize(entity.getFileSize());
        fileMetadata.setContentType(entity.getContentType());
        return fileMetadata;
    }

    @Override
    public String getBinderType() {
        return "database";
    }
}
