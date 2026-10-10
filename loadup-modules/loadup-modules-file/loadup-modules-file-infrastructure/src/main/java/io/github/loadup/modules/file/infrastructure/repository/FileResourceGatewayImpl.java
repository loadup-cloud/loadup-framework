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

import static io.github.loadup.modules.file.infrastructure.dataobject.table.Tables.*;

import com.mybatisflex.core.logicdelete.LogicDeleteManager;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.tenant.TenantManager;
import com.mybatisflex.core.util.UpdateEntity;
import io.github.loadup.modules.file.domain.gateway.FileResourceGateway;
import io.github.loadup.modules.file.domain.model.*;
import io.github.loadup.modules.file.infrastructure.converter.FileStorageConverter;
import io.github.loadup.modules.file.infrastructure.dataobject.*;
import io.github.loadup.modules.file.infrastructure.mapper.*;
import java.util.*;

public class FileResourceGatewayImpl implements FileResourceGateway {

    private final FileResourceDOMapper files;
    private final FileReferenceDOMapper refs;
    private final FileStorageConverter converter;

    public FileResourceGatewayImpl(
            FileResourceDOMapper files, FileReferenceDOMapper refs, FileStorageConverter converter) {
        this.files = files;
        this.refs = refs;
        this.converter = converter;
    }

    private QueryWrapper scope(String tenant, String id) {
        return QueryWrapper.create()
                .from(FILE_RESOURCE_DO)
                .where(FILE_RESOURCE_DO.TENANT_ID.eq(tenant))
                .and(FILE_RESOURCE_DO.ID.eq(id));
    }

    private QueryWrapper refScope(String tenant, String file) {
        return QueryWrapper.create()
                .from(FILE_REFERENCE_DO)
                .where(FILE_REFERENCE_DO.TENANT_ID.eq(tenant))
                .and(FILE_REFERENCE_DO.FILE_ID.eq(file));
    }

    @Override
    public void insert(FileResource file) {
        files.insert(converter.toDO(file));
    }

    @Override
    public Optional<FileResource> find(String tenant, String id) {
        return Optional.ofNullable(files.selectOneByQuery(scope(tenant, id))).map(converter::toDomain);
    }

    @Override
    public Optional<FileResource> lock(String tenant, String id) {
        return Optional.ofNullable(files.selectOneByQuery(scope(tenant, id).forUpdate()))
                .map(converter::toDomain);
    }

    @Override
    public FilePage<FileResource> list(String tenant, String owner, int page, int size) {
        var q = QueryWrapper.create()
                .from(FILE_RESOURCE_DO)
                .where(FILE_RESOURCE_DO.TENANT_ID.eq(tenant))
                .and(FILE_RESOURCE_DO.OWNER_ID.eq(owner))
                .and(FILE_RESOURCE_DO.STATE.eq("ACTIVE"));
        long total = files.selectCountByQuery(q);
        q.orderBy(FILE_RESOURCE_DO.CREATED_AT.desc(), FILE_RESOURCE_DO.ID.desc())
                .limit(size)
                .offset((long) (page - 1) * size);
        return new FilePage<>(
                files.selectListByQuery(q).stream().map(converter::toDomain).toList(), total, page, size);
    }

    @Override
    public void markPending(String tenant, String id) {
        var patch = UpdateEntity.of(FileResourceDO.class);
        patch.setState("PENDING_DELETE");
        files.updateByQuery(patch, scope(tenant, id).and(FILE_RESOURCE_DO.STATE.eq("ACTIVE")));
    }

    @Override
    public void markDeleted(String tenant, String id) {
        var patch = UpdateEntity.of(FileResourceDO.class);
        patch.setState("DELETED");
        files.updateByQuery(patch, scope(tenant, id).and(FILE_RESOURCE_DO.STATE.eq("PENDING_DELETE")));
    }
    /** System maintenance operation across tenant boundaries. */
    @Override
    public List<FileResource> pending(int limit) {
        return TenantManager.withoutTenantCondition(() -> files
                .selectListByQuery(QueryWrapper.create()
                        .from(FILE_RESOURCE_DO)
                        .where(FILE_RESOURCE_DO.STATE.eq("PENDING_DELETE"))
                        .orderBy(FILE_RESOURCE_DO.UPDATED_AT.asc(), FILE_RESOURCE_DO.ID.asc())
                        .limit(limit))
                .stream()
                .map(converter::toDomain)
                .toList());
    }

    @Override
    public void addReference(FileReference ref) {
        refs.insert(converter.toDO(ref));
    }

    @Override
    public void removeReference(String tenant, String file, String type, String id) {
        LogicDeleteManager.execWithoutLogicDelete(() -> refs.deleteByQuery(refScope(tenant, file)
                .and(FILE_REFERENCE_DO.REFERENCE_TYPE.eq(type))
                .and(FILE_REFERENCE_DO.REFERENCE_ID.eq(id))));
    }

    @Override
    public long countReferences(String tenant, String file) {
        return refs.selectCountByQuery(refScope(tenant, file));
    }

    @Override
    public List<FileReference> references(String tenant, String file) {
        return refs
                .selectListByQuery(
                        refScope(tenant, file).orderBy(FILE_REFERENCE_DO.CREATED_AT.asc(), FILE_REFERENCE_DO.ID.asc()))
                .stream()
                .map(converter::toDomain)
                .toList();
    }
}
