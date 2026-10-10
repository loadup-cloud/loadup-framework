/*
 * #%L
 * LoadUp Merchant
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
package io.github.loadup.modules.merchant.infrastructure.repository;

import com.mybatisflex.core.query.QueryWrapper;
import io.github.loadup.commons.error.CommonException;
import io.github.loadup.modules.merchant.client.dto.MerchantError;
import io.github.loadup.modules.merchant.domain.gateway.MerchantGateway;
import io.github.loadup.modules.merchant.domain.model.*;
import io.github.loadup.modules.merchant.infrastructure.converter.MerchantStorageConverter;
import io.github.loadup.modules.merchant.infrastructure.dataobject.MerchantDO;
import io.github.loadup.modules.merchant.infrastructure.mapper.MerchantMapper;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class MerchantGatewayImpl implements MerchantGateway {
    private final MerchantMapper mapper;
    private final MerchantStorageConverter converter;

    public MerchantGatewayImpl(MerchantMapper mapper, MerchantStorageConverter converter) {
        this.mapper = mapper;
        this.converter = converter;
    }

    private static String required(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Tenant and id required");
        return value;
    }

    private QueryWrapper scope(String tenant) {
        return QueryWrapper.create()
                .where(MerchantDO::getTenantId)
                .eq(required(tenant))
                .and(MerchantDO::getDeleted)
                .eq(0);
    }

    public Optional<Merchant> find(String tenant, String id) {
        return Optional.ofNullable(mapper.selectOneByQuery(
                        scope(tenant).and(MerchantDO::getId).eq(required(id))))
                .map(converter::toMerchant);
    }

    public void insert(Merchant merchant) {
        mapper.insert(converter.toDO(merchant));
    }

    public void update(Merchant merchant, long expected) {
        int changed = mapper.updateByQuery(
                converter.toDO(merchant),
                false,
                scope(merchant.tenantId())
                        .and(MerchantDO::getId)
                        .eq(required(merchant.id()))
                        .and(MerchantDO::getRowVersion)
                        .eq(expected));
        if (changed != 1) throw new CommonException(MerchantError.CONFLICT);
    }

    public MerchantPage page(String tenant, String code, String name, MerchantStatus status, int page, int size) {
        QueryWrapper query = scope(tenant);
        if (code != null && !code.isBlank())
            query.and(MerchantDO::getMerchantCode).eq(code);
        if (name != null && !name.isBlank()) query.and(MerchantDO::getName).like(name);
        if (status != null) query.and(MerchantDO::getStatus).eq(status.name());
        long total = mapper.selectCountByQuery(query);
        query.orderBy("created_at DESC, id ASC").limit((long) (page - 1) * size, size);
        return new MerchantPage(
                mapper.selectListByQuery(query).stream()
                        .map(converter::toMerchant)
                        .toList(),
                total);
    }
}
