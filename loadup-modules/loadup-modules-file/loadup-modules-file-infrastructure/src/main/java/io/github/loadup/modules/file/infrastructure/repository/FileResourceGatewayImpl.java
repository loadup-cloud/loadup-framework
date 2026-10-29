/*
 * #%L
 * LoadUp File Infrastructure
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
package io.github.loadup.modules.file.infrastructure.repository;

import io.github.loadup.commons.domain.PageResult;
import io.github.loadup.commons.error.CommonException;
import io.github.loadup.modules.file.client.error.FileResultCodeEnum;
import io.github.loadup.modules.file.domain.gateway.FileResourceGateway;
import io.github.loadup.modules.file.domain.model.FileReference;
import io.github.loadup.modules.file.domain.model.FileResource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public class FileResourceGatewayImpl implements FileResourceGateway {
    private final JdbcTemplate jdbc;
    private final Clock clock;

    public FileResourceGatewayImpl(JdbcTemplate jdbc, Clock clock) {
        this.jdbc = jdbc;
        this.clock = clock;
    }

    @Override
    public void insert(FileResource file) {
        LocalDateTime now = LocalDateTime.now(clock);
        int changed = jdbc.update(
                """
                INSERT INTO file_resource (id, tenant_id, owner_id, storage_id, filename, content_type,
                    content_length, provider, state, created_at, updated_at, deleted)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, FALSE)
                """,
                file.id(),
                file.tenantId(),
                file.ownerId(),
                file.storageId(),
                file.filename(),
                file.contentType(),
                file.size(),
                file.provider(),
                file.state(),
                file.createdAt() == null ? now : file.createdAt(),
                now);
        if (changed != 1) throw new IllegalStateException("Unexpected file insert count");
    }

    @Override
    public Optional<FileResource> find(String tenant, String id) {
        return read(tenant, id, false);
    }

    @Override
    public Optional<FileResource> lock(String tenant, String id) {
        return read(tenant, id, true);
    }

    private Optional<FileResource> read(String tenant, String id, boolean lock) {
        return jdbc
                .query(
                        "SELECT * FROM file_resource WHERE tenant_id = ? AND id = ? AND deleted = FALSE"
                                + (lock ? " FOR UPDATE" : ""),
                        FileResourceGatewayImpl::file,
                        tenant,
                        id)
                .stream()
                .findFirst();
    }

    @Override
    public PageResult<FileResource> list(String tenant, String owner, int page, int size) {
        Long count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM file_resource WHERE tenant_id = ? AND owner_id = ? "
                        + "AND state = 'ACTIVE' AND deleted = FALSE",
                Long.class,
                tenant,
                owner);
        var rows = jdbc.query(
                "SELECT * FROM file_resource WHERE tenant_id = ? AND owner_id = ? "
                        + "AND state = 'ACTIVE' AND deleted = FALSE ORDER BY created_at DESC, id DESC LIMIT ? OFFSET ?",
                FileResourceGatewayImpl::file,
                tenant,
                owner,
                size,
                (long) (page - 1) * size);
        return new PageResult<>(rows, count == null ? 0 : count, page, size);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void markPending(String tenant, String id) {
        lock(tenant, id)
                .filter(file -> "ACTIVE".equals(file.state()))
                .orElseThrow(() -> new CommonException(FileResultCodeEnum.FILE_NOT_FOUND));
        if (countReferences(tenant, id) > 0)
            throw new CommonException(FileResultCodeEnum.FILE_IS_REFERENCED_BY_A_BUSINESS_OBJECT);
        int changed = jdbc.update(
                "UPDATE file_resource SET state = 'PENDING_DELETE', updated_at = ? "
                        + "WHERE tenant_id = ? AND id = ? AND state = 'ACTIVE' AND deleted = FALSE",
                LocalDateTime.now(clock),
                tenant,
                id);
        if (changed != 1) throw new CommonException(FileResultCodeEnum.FILE_NOT_FOUND);
    }

    @Override
    public void markDeleted(String tenant, String id) {
        int changed = jdbc.update(
                "UPDATE file_resource SET state = 'DELETED', updated_at = ? "
                        + "WHERE tenant_id = ? AND id = ? AND state = 'PENDING_DELETE' AND deleted = FALSE",
                LocalDateTime.now(clock),
                tenant,
                id);
        if (changed == 0
                && find(tenant, id)
                        .filter(file -> "DELETED".equals(file.state()))
                        .isPresent()) return;
        if (changed != 1) throw new CommonException(FileResultCodeEnum.FILE_IS_NOT_PENDING_DELETION);
    }

    /** Authorized system maintenance scan across tenants. */
    @Override
    public List<FileResource> pending(int limit) {
        return jdbc.query(
                "SELECT * FROM file_resource WHERE state = 'PENDING_DELETE' AND deleted = FALSE "
                        + "ORDER BY updated_at ASC, id ASC LIMIT ?",
                FileResourceGatewayImpl::file,
                limit);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void addReference(FileReference ref) {
        FileResource parent = lock(ref.tenantId(), ref.fileId())
                .filter(file -> "ACTIVE".equals(file.state()))
                .orElseThrow(() -> new CommonException(FileResultCodeEnum.FILE_NOT_FOUND));
        LocalDateTime now = LocalDateTime.now(clock);
        int changed = jdbc.update(
                """
                INSERT INTO file_resource_reference (id, tenant_id, file_id, reference_type, reference_id,
                    created_at, updated_at, deleted) VALUES (?, ?, ?, ?, ?, ?, ?, FALSE)
                """,
                ref.id(),
                parent.tenantId(),
                parent.id(),
                ref.referenceType(),
                ref.referenceId(),
                ref.createdAt() == null ? now : ref.createdAt(),
                now);
        if (changed != 1) throw new IllegalStateException("Unexpected file reference insert count");
    }

    @Override
    public void removeReference(String tenant, String file, String type, String id) {
        jdbc.update(
                "DELETE FROM file_resource_reference WHERE tenant_id = ? AND file_id = ? "
                        + "AND reference_type = ? AND reference_id = ?",
                tenant,
                file,
                type,
                id);
    }

    @Override
    public long countReferences(String tenant, String file) {
        Long count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM file_resource_reference "
                        + "WHERE tenant_id = ? AND file_id = ? AND deleted = FALSE",
                Long.class,
                tenant,
                file);
        return count == null ? 0 : count;
    }

    @Override
    public List<FileReference> references(String tenant, String file) {
        return jdbc.query(
                "SELECT * FROM file_resource_reference WHERE tenant_id = ? AND file_id = ? "
                        + "AND deleted = FALSE ORDER BY created_at ASC, id ASC",
                FileResourceGatewayImpl::reference,
                tenant,
                file);
    }

    private static FileResource file(ResultSet rs, int index) throws SQLException {
        return new FileResource(
                rs.getString("id"),
                rs.getString("tenant_id"),
                rs.getString("owner_id"),
                rs.getString("storage_id"),
                rs.getString("filename"),
                rs.getString("content_type"),
                rs.getLong("content_length"),
                rs.getString("provider"),
                rs.getString("state"),
                rs.getObject("created_at", LocalDateTime.class),
                rs.getObject("updated_at", LocalDateTime.class));
    }

    private static FileReference reference(ResultSet rs, int index) throws SQLException {
        return new FileReference(
                rs.getString("id"),
                rs.getString("tenant_id"),
                rs.getString("file_id"),
                rs.getString("reference_type"),
                rs.getString("reference_id"),
                rs.getObject("created_at", LocalDateTime.class));
    }
}
