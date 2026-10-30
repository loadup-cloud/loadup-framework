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

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import io.github.loadup.commons.domain.PageResult;
import io.github.loadup.commons.error.CommonException;
import io.github.loadup.modules.file.client.error.FileResultCodeEnum;
import io.github.loadup.modules.file.domain.gateway.FileResourceGateway;
import io.github.loadup.modules.file.domain.model.FileReference;
import io.github.loadup.modules.file.domain.model.FileResource;
import io.github.loadup.modules.file.infrastructure.dataobject.FileReferenceRow;
import io.github.loadup.modules.file.infrastructure.dataobject.FileResourceInsert;
import io.github.loadup.modules.file.infrastructure.mapper.FileReferenceRowMapper;
import io.github.loadup.modules.file.infrastructure.mapper.FileResourceInsertMapper;
import io.github.loadup.modules.file.infrastructure.table.Tables;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class FileResourceGatewayImpl extends ServiceImpl<FileResourceInsertMapper, FileResourceInsert>
        implements FileResourceGateway {
    private final JdbcTemplate jdbc;
    private final FileReferenceRowMapper referenceMapper;
    private final Clock clock;

    public FileResourceGatewayImpl(
            JdbcTemplate jdbc, FileResourceInsertMapper mapper, FileReferenceRowMapper referenceMapper, Clock clock) {
        this.jdbc = jdbc;
        this.mapper = mapper;
        this.referenceMapper = referenceMapper;
        this.clock = clock;
    }

    private static FileResource toResource(FileResourceInsert row) {
        return new FileResource(
                row.getId(),
                row.getOwnerId(),
                row.getStorageId(),
                row.getFilename(),
                row.getContentType(),
                row.getContentLength(),
                row.getProvider(),
                row.getState(),
                row.getCreatedAt(),
                row.getUpdatedAt());
    }

    private static FileResource file(ResultSet rs, int index) throws SQLException {
        return new FileResource(
                rs.getString("id"),
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

    @Override
    public void insert(FileResource file) {
        LocalDateTime now = LocalDateTime.now(clock);
        save(new FileResourceInsert(
                file.id(),
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
    public Optional<FileResource> find(String id) {
        return getByIdOpt(id).map(FileResourceGatewayImpl::toResource);
    }

    @Override
    public Optional<FileResource> lock(String id) {
        var dataSource = jdbc.getDataSource();
        if (!TransactionSynchronizationManager.isActualTransactionActive()
                || TransactionSynchronizationManager.isCurrentTransactionReadOnly()
                || dataSource == null
                || !TransactionSynchronizationManager.hasResource(dataSource)) {
            throw new IllegalStateException("File lock requires a writable transaction on its data source");
        }
        return jdbc
                .query(
                        "SELECT * FROM file_resource WHERE id = ? AND deleted = FALSE FOR UPDATE",
                        FileResourceGatewayImpl::file,
                        id)
                .stream()
                .findFirst();
    }

    @Override
    public PageResult<FileResource> list(String owner, int page, int size) {
        var result = page(
                new Page<>(page, size),
                QueryWrapper.create()
                        .where(Tables.FILE_RESOURCE_INSERT.OWNER_ID.eq(owner))
                        .and(Tables.FILE_RESOURCE_INSERT.STATE.eq("ACTIVE"))
                        .orderBy(Tables.FILE_RESOURCE_INSERT.CREATED_AT.desc(), Tables.FILE_RESOURCE_INSERT.ID.desc()));
        return new PageResult<>(
                result.getRecords().stream()
                        .map(FileResourceGatewayImpl::toResource)
                        .toList(),
                result.getTotalRow(),
                page,
                size);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void markPending(String id) {
        lock(id).filter(file -> "ACTIVE".equals(file.state()))
                .orElseThrow(() -> new CommonException(FileResultCodeEnum.FILE_NOT_FOUND));
        if (countReferences(id) > 0) {
            throw new CommonException(FileResultCodeEnum.FILE_IS_REFERENCED_BY_A_BUSINESS_OBJECT);
        }
        int changed = jdbc.update(
                "UPDATE file_resource SET state = 'PENDING_DELETE', updated_at = ? "
                        + "WHERE id = ? AND state = 'ACTIVE' AND deleted = FALSE",
                LocalDateTime.now(clock),
                id);
        if (changed != 1) {
            throw new CommonException(FileResultCodeEnum.FILE_NOT_FOUND);
        }
    }

    @Override
    public void markDeleted(String id) {
        int changed = jdbc.update(
                "UPDATE file_resource SET state = 'DELETED', updated_at = ? "
                        + "WHERE id = ? AND state = 'PENDING_DELETE' AND deleted = FALSE",
                LocalDateTime.now(clock),
                id);
        if (changed == 0
                && find(id).filter(file -> "DELETED".equals(file.state())).isPresent()) {
            return;
        }
        if (changed != 1) {
            throw new CommonException(FileResultCodeEnum.FILE_IS_NOT_PENDING_DELETION);
        }
    }

    /**
     * Authorized system maintenance scan across the current tenant schema.
     */
    @Override
    public List<FileResource> pending(int limit) {
        var query = QueryWrapper.create()
                .where(Tables.FILE_RESOURCE_INSERT.STATE.eq("PENDING_DELETE"))
                .orderBy(Tables.FILE_RESOURCE_INSERT.UPDATED_AT.asc(), Tables.FILE_RESOURCE_INSERT.ID.asc());
        return list(query).stream()
                .limit(limit)
                .map(FileResourceGatewayImpl::toResource)
                .toList();
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void addReference(FileReference ref) {
        FileResource parent = lock(ref.fileId())
                .filter(file -> "ACTIVE".equals(file.state()))
                .orElseThrow(() -> new CommonException(FileResultCodeEnum.FILE_NOT_FOUND));
        LocalDateTime now = LocalDateTime.now(clock);
        referenceMapper.insert(new FileReferenceRow(
                ref.id(),
                parent.id(),
                ref.referenceType(),
                ref.referenceId(),
                ref.createdAt() == null ? now : ref.createdAt(),
                now,
                false));
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void removeReference(String file, String type, String id) {
        lock(file)
                .filter(parent -> "ACTIVE".equals(parent.state()))
                .orElseThrow(() -> new CommonException(FileResultCodeEnum.FILE_NOT_FOUND));
        jdbc.update(
                "DELETE FROM file_resource_reference WHERE file_id = ? "
                        + "AND reference_type = ? AND reference_id = ?",
                file,
                type,
                id);
    }

    @Override
    public long countReferences(String file) {
        return referenceMapper.selectCountByQuery(
                QueryWrapper.create().where(Tables.FILE_REFERENCE_ROW.FILE_ID.eq(file)));
    }

    @Override
    public List<FileReference> references(String file) {
        var query = QueryWrapper.create()
                .where(Tables.FILE_REFERENCE_ROW.FILE_ID.eq(file))
                .orderBy(Tables.FILE_REFERENCE_ROW.CREATED_AT.asc(), Tables.FILE_REFERENCE_ROW.ID.asc());
        return referenceMapper.selectListByQuery(query).stream()
                .map(row -> new FileReference(
                        row.getId(), row.getFileId(), row.getReferenceType(), row.getReferenceId(), row.getCreatedAt()))
                .toList();
    }
}
