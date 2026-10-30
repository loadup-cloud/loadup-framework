/*
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

import com.mybatisflex.core.query.QueryWrapper;
import io.github.loadup.components.dfs.DfsObjectNotFoundException;
import io.github.loadup.components.dfs.DfsProvider;
import io.github.loadup.components.dfs.DfsStorageException;
import io.github.loadup.components.dfs.database.dataobject.DfsFileInsert;
import io.github.loadup.components.dfs.database.mapper.DfsFileInsertMapper;
import io.github.loadup.components.dfs.database.table.Tables;
import io.github.loadup.components.dfs.model.FileDownloadResponse;
import io.github.loadup.components.dfs.model.FileMetadata;
import io.github.loadup.components.dfs.model.FileUploadRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

/**
 * Transitional database binder for small files. S3 is the recommended production binder.
 */
public class DatabaseDfsProvider implements DfsProvider {
    private final NamedParameterJdbcTemplate jdbc;
    private final DfsFileInsertMapper files;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public DatabaseDfsProvider(
            NamedParameterJdbcTemplate jdbc, DfsFileInsertMapper files, ObjectMapper objectMapper, Clock clock) {
        this.jdbc = jdbc;
        this.files = files;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Override
    public FileMetadata upload(FileUploadRequest request) {
        String fileId = UUID.randomUUID().toString().replace("-", "");
        try {
            byte[] content = request.content().readAllBytes();
            if (content.length != request.contentLength()) {
                throw new DfsStorageException(DfsStorageException.Code.OPERATION_FAILED);
            }
            LocalDateTime uploadedAt = LocalDateTime.now(clock);
            var file = new DfsFileInsert(
                    fileId,
                    request.filename(),
                    content.length,
                    request.contentType(),
                    content,
                    objectMapper.writeValueAsString(request.metadata()),
                    uploadedAt,
                    uploadedAt,
                    false);
            files.insert(file);
            return toMetadata(file);
        } catch (IOException | JacksonException e) {
            throw new DfsStorageException(DfsStorageException.Code.OPERATION_FAILED, e);
        }
    }

    @Override
    public FileDownloadResponse download(String fileId) {
        DfsFileInsert file = find(fileId);
        return new FileDownloadResponse(
                toMetadata(file), new ByteArrayInputStream(file.getContent()), file.getFileSize());
    }

    @Override
    public boolean delete(String fileId) {
        return jdbc.update(
                        "UPDATE dfs_file SET deleted = TRUE, updated_at = :now WHERE id = :id AND deleted = FALSE",
                        new MapSqlParameterSource("id", fileId).addValue("now", LocalDateTime.now(clock)))
                > 0;
    }

    @Override
    public boolean exists(String fileId) {
        return files.selectCountByQuery(QueryWrapper.create().where(Tables.DFS_FILE_INSERT.ID.eq(fileId))) > 0;
    }

    @Override
    public FileMetadata getMetadata(String fileId) {
        return toMetadata(find(fileId));
    }

    @Override
    public String getBinderType() {
        return "database";
    }

    private DfsFileInsert find(String fileId) {
        return Optional.ofNullable(files.selectOneById(fileId))
                .orElseThrow(() -> new DfsObjectNotFoundException(fileId));
    }

    private FileMetadata toMetadata(DfsFileInsert file) {
        Map<String, String> metadata;
        try {
            metadata = file.getMetadataJson() == null
                    ? Map.of()
                    : objectMapper.readValue(file.getMetadataJson(), new TypeReference<>() {});
        } catch (JacksonException e) {
            throw new DfsStorageException(DfsStorageException.Code.OPERATION_FAILED, e);
        }
        return new FileMetadata(
                file.getId(),
                file.getFilename(),
                file.getFileSize(),
                file.getContentType(),
                getBinderType(),
                file.getId(),
                metadata,
                file.getCreatedAt().atZone(clock.getZone()).toInstant());
    }
}
