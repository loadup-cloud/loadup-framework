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

import static io.github.loadup.modules.contract.infrastructure.dataobject.table.Tables.MERCHANT_CONTRACT_DO;

import com.mybatisflex.core.query.QueryWrapper;
import io.github.loadup.commons.error.CommonException;
import io.github.loadup.modules.contract.client.dto.ContractError;
import io.github.loadup.modules.contract.domain.gateway.MerchantContractGateway;
import io.github.loadup.modules.contract.domain.model.*;
import io.github.loadup.modules.contract.infrastructure.converter.ContractStorageConverter;
import io.github.loadup.modules.contract.infrastructure.dataobject.*;
import io.github.loadup.modules.contract.infrastructure.mapper.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class MerchantContractGatewayImpl implements MerchantContractGateway {
    private final MerchantContractDOMapper contracts;
    private final ContractRevisionDOMapper revisions;
    private final ContractStorageConverter converter;

    public MerchantContractGatewayImpl(
            MerchantContractDOMapper contracts,
            ContractRevisionDOMapper revisions,
            ContractStorageConverter converter) {
        this.contracts = contracts;
        this.revisions = revisions;
        this.converter = converter;
    }

    private static String required(String value) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("Tenant and lookup identifiers are required");
        return value;
    }

    private QueryWrapper scope(String tenant) {
        return QueryWrapper.create()
                .where(MerchantContractDO::getTenantId)
                .eq(required(tenant))
                .and(MerchantContractDO::getDeleted)
                .eq(0);
    }

    public Optional<ContractRecord> find(String tenant, String id) {
        return Optional.ofNullable(contracts.selectOneByQuery(
                        scope(tenant).and(MerchantContractDO::getId).eq(required(id))))
                .map(converter::toContract);
    }

    public Optional<ContractRecord> findByScope(String tenant, String merchant, String key) {
        return Optional.ofNullable(contracts.selectOneByQuery(scope(tenant)
                        .and(MerchantContractDO::getMerchantId)
                        .eq(required(merchant))
                        .and(MerchantContractDO::getScopeKey)
                        .eq(required(key))))
                .map(converter::toContract);
    }

    public Optional<ContractRecord> findByRequest(String tenant, String key) {
        return Optional.ofNullable(contracts.selectOneByQuery(
                        scope(tenant).and(MerchantContractDO::getRequestKey).eq(required(key))))
                .map(converter::toContract);
    }

    public Optional<ContractRevisionRecord> revision(String tenant, String id, int version) {
        var query = QueryWrapper.create()
                .where(ContractRevisionDO::getTenantId)
                .eq(required(tenant))
                .and(ContractRevisionDO::getDeleted)
                .eq(0)
                .and(ContractRevisionDO::getContractId)
                .eq(required(id))
                .and(ContractRevisionDO::getRevision)
                .eq(version);
        return Optional.ofNullable(revisions.selectOneByQuery(query)).map(converter::toRevision);
    }

    public void insert(ContractRecord contract, ContractRevisionRecord revision) {
        contracts.insert(converter.toContractDO(contract));
        revisions.insert(converter.toRevisionDO(revision));
    }

    public void updateStatus(String tenant, String id, long generation, ContractStatus status) {
        MerchantContractDO patch = new MerchantContractDO();
        patch.setStatus(status.name());
        patch.setGeneration(Math.addExact(generation, 1));
        patch.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));
        int changed = contracts.updateByQuery(
                patch,
                true,
                scope(tenant)
                        .and(MerchantContractDO::getId)
                        .eq(required(id))
                        .and(MerchantContractDO::getGeneration)
                        .eq(generation)
                        .and(MerchantContractDO::getStatus)
                        .ne(ContractStatus.TERMINATED.name()));
        if (changed != 1) throw new CommonException(ContractError.CONFLICT, "Contract state changed or terminated");
    }

    public ContractPage<ContractRecord> page(String tenant, String merchant, int page, int size) {
        QueryWrapper query = scope(tenant);
        if (merchant != null && !merchant.isBlank())
            query.and(MerchantContractDO::getMerchantId).eq(required(merchant));
        long total = contracts.selectCountByQuery(query);
        query.orderBy(MERCHANT_CONTRACT_DO.CREATED_AT.desc(), MERCHANT_CONTRACT_DO.ID.asc())
                .limit((long) (page - 1) * size, size);
        return new ContractPage<>(
                contracts.selectListByQuery(query).stream()
                        .map(converter::toContract)
                        .toList(),
                total);
    }
}
