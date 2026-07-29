package io.github.loadup.components.dfs.database;

/*-
 * #%L
 * Loadup Dfs Binder Database
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
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
