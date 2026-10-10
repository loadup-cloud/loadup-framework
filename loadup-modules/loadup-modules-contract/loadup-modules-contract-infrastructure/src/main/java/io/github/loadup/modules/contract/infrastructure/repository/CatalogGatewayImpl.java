/*
 * #%L
 * LoadUp Contract
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
package io.github.loadup.modules.contract.infrastructure.repository;

import static io.github.loadup.modules.contract.infrastructure.dataobject.table.Tables.CATALOG_VERSION_DO;

import com.mybatisflex.core.query.QueryWrapper;
import io.github.loadup.commons.error.CommonException;
import io.github.loadup.modules.contract.client.dto.ContractError;
import io.github.loadup.modules.contract.domain.gateway.CatalogGateway;
import io.github.loadup.modules.contract.domain.model.*;
import io.github.loadup.modules.contract.infrastructure.converter.ContractStorageConverter;
import io.github.loadup.modules.contract.infrastructure.dataobject.CatalogVersionDO;
import io.github.loadup.modules.contract.infrastructure.mapper.CatalogVersionDOMapper;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class CatalogGatewayImpl implements CatalogGateway {
    private final CatalogVersionDOMapper mapper;
    private final ContractStorageConverter converter;

    public CatalogGatewayImpl(CatalogVersionDOMapper mapper, ContractStorageConverter converter) {
        this.mapper = mapper;
        this.converter = converter;
    }

    private static String required(String value) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("Tenant and lookup identifiers are required");
        return value;
    }

    private QueryWrapper scope(String tenant) {
        return QueryWrapper.create()
                .where(CatalogVersionDO::getTenantId)
                .eq(required(tenant))
                .and(CatalogVersionDO::getDeleted)
                .eq(0);
    }

    public Optional<CatalogEntry> find(String tenant, String id) {
        return Optional.ofNullable(mapper.selectOneByQuery(
                        scope(tenant).and(CatalogVersionDO::getId).eq(required(id))))
                .map(converter::toCatalog);
    }

    public Optional<CatalogEntry> lock(String tenant, String id) {
        return Optional.ofNullable(mapper.selectOneByQuery(scope(tenant)
                        .and(CatalogVersionDO::getId)
                        .eq(required(id))
                        .forUpdate()))
                .map(converter::toCatalog);
    }

    public void insert(CatalogEntry entry) {
        mapper.insert(converter.toCatalogDO(entry));
    }

    public void update(CatalogEntry entry, long expected) {
        int changed = mapper.updateByQuery(
                converter.toCatalogDO(entry),
                scope(entry.tenantId())
                        .and(CatalogVersionDO::getId)
                        .eq(entry.id())
                        .and(CatalogVersionDO::getRowVersion)
                        .eq(expected));
        if (changed != 1) throw new CommonException(ContractError.CONFLICT, "Catalog version changed");
    }

    public ContractPage<CatalogEntry> page(
            String tenant, CatalogKind kind, CatalogStatus status, String code, int page, int size) {
        QueryWrapper query = scope(tenant).and(CatalogVersionDO::getKind).eq(kind.name());
        if (status != null) query.and(CatalogVersionDO::getStatus).eq(status.name());
        if (code != null && !code.isBlank())
            query.and(CatalogVersionDO::getCode).eq(code);
        long total = mapper.selectCountByQuery(query);
        query.orderBy(CATALOG_VERSION_DO.UPDATED_AT.desc(), CATALOG_VERSION_DO.ID.asc())
                .limit((long) (page - 1) * size, size);
        return new ContractPage<>(
                mapper.selectListByQuery(query).stream()
                        .map(converter::toCatalog)
                        .toList(),
                total);
    }
}
