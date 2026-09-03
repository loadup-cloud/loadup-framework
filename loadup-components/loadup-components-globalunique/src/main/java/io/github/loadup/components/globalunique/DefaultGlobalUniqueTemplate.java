/*-
 * #%L
 * LoadUp Components :: Global Unique
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

package io.github.loadup.components.globalunique;

import static io.github.loadup.components.globalunique.dataobject.table.Tables.GLOBAL_UNIQUE_DO;

import com.mybatisflex.core.query.QueryWrapper;
import io.github.loadup.commons.util.TenantUtil;
import io.github.loadup.components.database.config.DatabaseProperties;
import io.github.loadup.components.globalunique.dataobject.GlobalUniqueDO;
import io.github.loadup.components.globalunique.mapper.GlobalUniqueMapper;
import io.github.loadup.components.globalunique.model.GlobalUniqueClaim;
import io.github.loadup.components.globalunique.model.GlobalUniqueRecord;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.util.StringUtils;

/** Default MyBatis-Flex implementation of {@link GlobalUniqueTemplate}. */
public class DefaultGlobalUniqueTemplate implements GlobalUniqueTemplate {
    private static final Logger log = LoggerFactory.getLogger(DefaultGlobalUniqueTemplate.class);
    private static final String GLOBAL_TENANT = "__loadup_global__";

    private final GlobalUniqueMapper mapper;
    private final DatabaseProperties databaseProperties;

    public DefaultGlobalUniqueTemplate(GlobalUniqueMapper mapper, DatabaseProperties databaseProperties) {
        this.mapper = mapper;
        this.databaseProperties = databaseProperties;
    }

    @Override
    public boolean claim(String bizType, String uniqueKey) {
        return claim(new GlobalUniqueClaim(bizType, uniqueKey, null, null));
    }

    @Override
    public boolean claim(GlobalUniqueClaim claim) {
        Objects.requireNonNull(claim, "claim must not be null");
        GlobalUniqueDO dataObject = new GlobalUniqueDO();
        dataObject.setTenantId(resolveTenantId());
        dataObject.setBizType(claim.bizType());
        dataObject.setUniqueKey(claim.uniqueKey());
        dataObject.setBizId(claim.bizId());
        dataObject.setRequestData(claim.requestData());

        try {
            return mapper.insert(dataObject) == 1;
        } catch (DuplicateKeyException exception) {
            if (find(claim.bizType(), claim.uniqueKey()).isEmpty()) {
                throw exception;
            }
            log.debug(
                    "Global unique claim already exists: tenantId={}, bizType={}, uniqueKey={}",
                    dataObject.getTenantId(),
                    claim.bizType(),
                    claim.uniqueKey());
            return false;
        }
    }

    @Override
    public Optional<GlobalUniqueRecord> find(String bizType, String uniqueKey) {
        GlobalUniqueClaim lookup = new GlobalUniqueClaim(bizType, uniqueKey, null, null);
        QueryWrapper query = QueryWrapper.create()
                .where(GLOBAL_UNIQUE_DO.TENANT_ID.eq(resolveTenantId()))
                .and(GLOBAL_UNIQUE_DO.BIZ_TYPE.eq(lookup.bizType()))
                .and(GLOBAL_UNIQUE_DO.UNIQUE_KEY.eq(lookup.uniqueKey()));
        return Optional.ofNullable(mapper.selectOneByQuery(query)).map(DefaultGlobalUniqueTemplate::toRecord);
    }

    private String resolveTenantId() {
        DatabaseProperties.MultiTenant multiTenant = databaseProperties.getMultiTenant();
        if (!multiTenant.isEnabled()) {
            return GLOBAL_TENANT;
        }

        String tenantId = TenantUtil.getTenantId();
        if (StringUtils.hasText(tenantId)) {
            return tenantId;
        }
        if (StringUtils.hasText(multiTenant.getDefaultTenantId())) {
            return multiTenant.getDefaultTenantId();
        }
        if (multiTenant.isRequired()) {
            throw new IllegalStateException("Tenant context is required for global unique operations");
        }
        return GLOBAL_TENANT;
    }

    private static GlobalUniqueRecord toRecord(GlobalUniqueDO dataObject) {
        return new GlobalUniqueRecord(
                dataObject.getId(),
                dataObject.getTenantId(),
                dataObject.getBizType(),
                dataObject.getUniqueKey(),
                dataObject.getBizId(),
                dataObject.getRequestData(),
                dataObject.getCreatedAt(),
                dataObject.getUpdatedAt());
    }
}
