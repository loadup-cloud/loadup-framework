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
import io.github.loadup.modules.file.infrastructure.dataobject.FileResourceInsert;
import io.github.loadup.modules.file.infrastructure.dataobject.FileReferenceRow;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.data.domain.Sort;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class FileResourceGatewayImpl implements FileResourceGateway {
    private final JdbcTemplate jdbc;
    private final JdbcAggregateTemplate aggregates;
    private final Clock clock;

    public FileResourceGatewayImpl(JdbcTemplate jdbc, JdbcAggregateTemplate aggregates, Clock clock) {
        this.jdbc = jdbc;
        this.aggregates = aggregates;
        this.clock = clock;
    }

    @Override
    public void insert(FileResource file) {
        LocalDateTime now = LocalDateTime.now(clock);
        aggregates.insert(new FileResourceInsert(
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
                now,
                false));
    }

    @Override
    public Optional<FileResource> find(String tenant, String id) {
        var query = Query.query(Criteria.where("tenantId").is(tenant)
                .and("id").is(id).and("deleted").is(false));
        return aggregates.findOne(query, FileResourceInsert.class).map(FileResourceGatewayImpl::file);
    }

    @Override
    public Optional<FileResource> lock(String tenant, String id) {
        var dataSource = jdbc.getDataSource();
        if (!TransactionSynchronizationManager.isActualTransactionActive()
                || TransactionSynchronizationManager.isCurrentTransactionReadOnly()
                || dataSource == null
                || !TransactionSynchronizationManager.hasResource(dataSource))
            throw new IllegalStateException("File lock requires a writable transaction on its data source");
        return jdbc.query(
                        "SELECT * FROM file_resource WHERE tenant_id = ? AND id = ? AND deleted = FALSE FOR UPDATE",
                        FileResourceGatewayImpl::file, tenant, id)
                .stream().findFirst();
    }

    @Override
    public PageResult<FileResource> list(String tenant, String owner, int page, int size) {
        var query = Query.query(Criteria.where("tenantId").is(tenant).and("ownerId").is(owner)
                .and("state").is("ACTIVE").and("deleted").is(false));
        long count = aggregates.count(query, FileResourceInsert.class);
        var rows = aggregates.findAll(query.sort(Sort.by(Sort.Direction.DESC, "createdAt", "id"))
                        .limit(size).offset((long) (page - 1) * size), FileResourceInsert.class)
                .stream().map(FileResourceGatewayImpl::file).toList();
        return new PageResult<>(rows, count, page, size);
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
        var query = Query.query(Criteria.where("state").is("PENDING_DELETE").and("deleted").is(false))
                .sort(Sort.by("updatedAt", "id")).limit(limit);
        return aggregates.findAll(query, FileResourceInsert.class).stream()
                .map(FileResourceGatewayImpl::file).toList();
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void addReference(FileReference ref) {
        FileResource parent = lock(ref.tenantId(), ref.fileId())
                .filter(file -> "ACTIVE".equals(file.state()))
                .orElseThrow(() -> new CommonException(FileResultCodeEnum.FILE_NOT_FOUND));
        LocalDateTime now = LocalDateTime.now(clock);
        aggregates.insert(new FileReferenceRow(
                ref.id(),
                parent.tenantId(),
                parent.id(),
                ref.referenceType(),
                ref.referenceId(),
                ref.createdAt() == null ? now : ref.createdAt(),
                now,
                false));
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void removeReference(String tenant, String file, String type, String id) {
        lock(tenant, file)
                .filter(parent -> "ACTIVE".equals(parent.state()))
                .orElseThrow(() -> new CommonException(FileResultCodeEnum.FILE_NOT_FOUND));
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
        return aggregates.count(referenceQuery(tenant, file), FileReferenceRow.class);
    }

    @Override
    public List<FileReference> references(String tenant, String file) {
        return aggregates.findAll(referenceQuery(tenant, file).sort(Sort.by("createdAt", "id")), FileReferenceRow.class)
                .stream()
                .map(row -> new FileReference(row.id(), row.tenantId(), row.fileId(), row.referenceType(),
                        row.referenceId(), row.createdAt()))
                .toList();
    }

    private static Query referenceQuery(String tenant, String file) {
        return Query.query(Criteria.where("tenantId").is(tenant).and("fileId").is(file).and("deleted").is(false));
    }

    private static FileResource file(FileResourceInsert row) {
        return new FileResource(row.id(), row.tenantId(), row.ownerId(), row.storageId(), row.filename(),
                row.contentType(), row.contentLength(), row.provider(), row.state(), row.createdAt(), row.updatedAt());
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

}
