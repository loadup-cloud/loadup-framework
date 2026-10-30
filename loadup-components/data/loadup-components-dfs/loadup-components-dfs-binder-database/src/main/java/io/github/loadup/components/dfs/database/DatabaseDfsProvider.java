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

import io.github.loadup.components.database.tenant.DatabaseTenant;
import io.github.loadup.components.dfs.DfsObjectNotFoundException;
import io.github.loadup.components.dfs.DfsProvider;
import io.github.loadup.components.dfs.DfsStorageException;
import io.github.loadup.components.dfs.database.dataobject.DfsFileInsert;
import io.github.loadup.components.dfs.database.repository.DfsFileRepository;
import io.github.loadup.components.dfs.model.FileDownloadResponse;
import io.github.loadup.components.dfs.model.FileMetadata;
import io.github.loadup.components.dfs.model.FileUploadRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

/** Transitional database binder for small files. S3 is the recommended production binder. */
public class DatabaseDfsProvider implements DfsProvider {
    private final NamedParameterJdbcTemplate jdbc;
    private final JdbcAggregateTemplate aggregates;
    private final DfsFileRepository files;
    private final ObjectMapper objectMapper;
    private final DatabaseTenant tenant;
    private final Clock clock;

    public DatabaseDfsProvider(
            NamedParameterJdbcTemplate jdbc,
            JdbcAggregateTemplate aggregates,
            DfsFileRepository files,
            ObjectMapper objectMapper,
            DatabaseTenant tenant,
            Clock clock) {
        this.jdbc = jdbc;
        this.aggregates = aggregates;
        this.files = files;
        this.objectMapper = objectMapper;
        this.tenant = tenant;
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
                    tenant.current(),
                    request.filename(),
                    content.length,
                    request.contentType(),
                    content,
                    objectMapper.writeValueAsString(request.metadata()),
                    uploadedAt,
                    uploadedAt,
                    false);
            aggregates.insert(file);
            return toMetadata(file);
        } catch (IOException | JacksonException e) {
            throw new DfsStorageException(DfsStorageException.Code.OPERATION_FAILED, e);
        }
    }

    @Override
    public FileDownloadResponse download(String fileId) {
        DfsFileInsert file = find(fileId);
        return new FileDownloadResponse(toMetadata(file), new ByteArrayInputStream(file.content()), file.fileSize());
    }

    @Override
    public boolean delete(String fileId) {
        return jdbc.update(
                        "UPDATE dfs_file SET deleted = TRUE, updated_at = :now WHERE id = :id AND tenant_id IS NOT DISTINCT FROM :tenant AND deleted = FALSE",
                        parameters(fileId).addValue("now", LocalDateTime.now(clock)))
                > 0;
    }

    @Override
    public boolean exists(String fileId) {
        return files.exists(fileId);
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
        return files.find(fileId).orElseThrow(() -> new DfsObjectNotFoundException(fileId));
    }

    private MapSqlParameterSource parameters(String fileId) {
        return new MapSqlParameterSource("id", fileId).addValue("tenant", tenant.current());
    }

    private FileMetadata toMetadata(DfsFileInsert file) {
        Map<String, String> metadata;
        try {
            metadata = file.metadataJson() == null
                    ? Map.of()
                    : objectMapper.readValue(file.metadataJson(), new TypeReference<>() {});
        } catch (JacksonException e) {
            throw new DfsStorageException(DfsStorageException.Code.OPERATION_FAILED, e);
        }
        return new FileMetadata(
                file.id(),
                file.filename(),
                file.fileSize(),
                file.contentType(),
                getBinderType(),
                file.id(),
                metadata,
                file.createdAt().atZone(clock.getZone()).toInstant());
    }
}
