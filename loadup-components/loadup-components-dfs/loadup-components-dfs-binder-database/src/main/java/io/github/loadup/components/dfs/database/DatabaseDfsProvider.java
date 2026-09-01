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
package io.github.loadup.components.dfs.database;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybatisflex.core.query.QueryWrapper;
import io.github.loadup.components.dfs.DfsObjectNotFoundException;
import io.github.loadup.components.dfs.DfsProvider;
import io.github.loadup.components.dfs.DfsStorageException;
import io.github.loadup.components.dfs.database.dataobject.FileStorageDO;
import io.github.loadup.components.dfs.database.mapper.FileStorageMapper;
import io.github.loadup.components.dfs.model.FileDownloadResponse;
import io.github.loadup.components.dfs.model.FileMetadata;
import io.github.loadup.components.dfs.model.FileUploadRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;

/** Transitional database binder for small files. S3 is the recommended production binder. */
public class DatabaseDfsProvider implements DfsProvider {
    private final FileStorageMapper mapper;
    private final ObjectMapper objectMapper;

    public DatabaseDfsProvider(FileStorageMapper mapper, ObjectMapper objectMapper) {
        this.mapper = mapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public FileMetadata upload(FileUploadRequest request) {
        String fileId = UUID.randomUUID().toString().replace("-", "");
        try {
            byte[] content = request.content().readAllBytes();
            if (content.length != request.contentLength()) {
                throw new DfsStorageException("Uploaded content length mismatch: expected=" + request.contentLength()
                        + ", actual=" + content.length);
            }
            LocalDateTime uploadedAt = LocalDateTime.now(ZoneOffset.UTC);
            FileStorageDO file = new FileStorageDO();
            file.setId(fileId);
            file.setFilename(request.filename());
            file.setFileSize((long) content.length);
            file.setContentType(request.contentType());
            file.setContent(content);
            file.setMetadataJson(objectMapper.writeValueAsString(request.metadata()));
            file.setCreatedAt(uploadedAt);
            file.setUpdatedAt(uploadedAt);
            file.setDeleted(false);
            mapper.insert(file);
            return toMetadata(file);
        } catch (IOException e) {
            throw new DfsStorageException("Database DFS upload failed for " + fileId, e);
        }
    }

    @Override
    public FileDownloadResponse download(String fileId) {
        FileStorageDO file = find(fileId);
        return new FileDownloadResponse(
                toMetadata(file), new ByteArrayInputStream(file.getContent()), file.getFileSize());
    }

    @Override
    public boolean delete(String fileId) {
        return mapper.deleteById(fileId) > 0;
    }

    @Override
    public boolean exists(String fileId) {
        return mapper.selectOneById(fileId) != null;
    }

    @Override
    public FileMetadata getMetadata(String fileId) {
        return toMetadata(find(fileId));
    }

    @Override
    public String getBinderType() {
        return "database";
    }

    private FileStorageDO find(String fileId) {
        FileStorageDO file = mapper.selectOneByQuery(QueryWrapper.create().eq("id", fileId));
        if (file == null) {
            throw new DfsObjectNotFoundException(fileId);
        }
        return file;
    }

    private FileMetadata toMetadata(FileStorageDO file) {
        Map<String, String> metadata;
        try {
            metadata = file.getMetadataJson() == null
                    ? Map.of()
                    : objectMapper.readValue(file.getMetadataJson(), new TypeReference<>() {});
        } catch (IOException e) {
            throw new DfsStorageException("Failed to parse database DFS metadata for " + file.getId(), e);
        }
        return new FileMetadata(
                file.getId(),
                file.getFilename(),
                file.getFileSize(),
                file.getContentType(),
                getBinderType(),
                file.getId(),
                metadata,
                file.getCreatedAt().toInstant(ZoneOffset.UTC));
    }
}
