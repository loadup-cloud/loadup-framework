/*
 * #%L
 * LoadUp Components Global Unique
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

import io.github.loadup.commons.log.LogUtil;
import io.github.loadup.commons.util.TenantUtil;
import io.github.loadup.components.database.config.DatabaseProperties;
import io.github.loadup.components.database.id.IdGenerator;
import io.github.loadup.components.globalunique.model.GlobalUniqueClaim;
import io.github.loadup.components.globalunique.model.GlobalUniqueRecord;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;

/** PostgreSQL implementation of {@link GlobalUniqueTemplate}. */
public class DefaultGlobalUniqueTemplate implements GlobalUniqueTemplate {

    private static final String GLOBAL_TENANT = "__loadup_global__";

    private final JdbcTemplate jdbc;
    private final DatabaseProperties databaseProperties;
    private final IdGenerator idGenerator;
    private final Clock clock;

    public DefaultGlobalUniqueTemplate(
            JdbcTemplate jdbc, DatabaseProperties databaseProperties, IdGenerator idGenerator, Clock clock) {
        this.jdbc = jdbc;
        this.databaseProperties = databaseProperties;
        this.idGenerator = idGenerator;
        this.clock = clock;
    }

    @Override
    public boolean claim(String bizType, String uniqueKey) {
        return claim(new GlobalUniqueClaim(bizType, uniqueKey, null, null));
    }

    @Override
    public boolean claim(GlobalUniqueClaim claim) {
        Objects.requireNonNull(claim, "claim must not be null");
        String tenant = resolveTenantId();
        LocalDateTime now = LocalDateTime.now(clock);
        int inserted = jdbc.update(
                """
                INSERT INTO global_unique
                    (id, tenant_id, biz_type, unique_key, biz_id, request_data, created_at, updated_at, deleted)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, FALSE)
                ON CONFLICT (tenant_id, biz_type, unique_key) DO NOTHING
                """,
                idGenerator.generate(),
                tenant,
                claim.bizType(),
                claim.uniqueKey(),
                claim.bizId(),
                claim.requestData(),
                now,
                now);
        if (inserted == 0) {
            if (find(tenant, claim.bizType(), claim.uniqueKey()).isEmpty()) {
                throw new DataIntegrityViolationException("Global unique key exists but is not active");
            }
            LogUtil.debug(
                    DefaultGlobalUniqueTemplate.class,
                    "Global unique claim already exists: tenantId={}, bizType={}, uniqueKey={}",
                    tenant,
                    claim.bizType(),
                    claim.uniqueKey());
            return false;
        }
        if (inserted != 1) throw new IllegalStateException("Unexpected global unique insert count: " + inserted);
        return true;
    }

    @Override
    public Optional<GlobalUniqueRecord> find(String bizType, String uniqueKey) {
        GlobalUniqueClaim lookup = new GlobalUniqueClaim(bizType, uniqueKey, null, null);
        return find(resolveTenantId(), lookup.bizType(), lookup.uniqueKey());
    }

    private Optional<GlobalUniqueRecord> find(String tenant, String bizType, String uniqueKey) {
        return jdbc
                .query(
                        """
                SELECT id, tenant_id, biz_type, unique_key, biz_id, request_data, created_at, updated_at
                FROM global_unique
                WHERE tenant_id = ? AND biz_type = ? AND unique_key = ? AND deleted = FALSE
                """,
                        (rs, row) -> new GlobalUniqueRecord(
                                rs.getString("id"),
                                rs.getString("tenant_id"),
                                rs.getString("biz_type"),
                                rs.getString("unique_key"),
                                rs.getString("biz_id"),
                                rs.getString("request_data"),
                                rs.getObject("created_at", LocalDateTime.class),
                                rs.getObject("updated_at", LocalDateTime.class)),
                        tenant,
                        bizType,
                        uniqueKey)
                .stream()
                .findFirst();
    }

    private String resolveTenantId() {
        DatabaseProperties.MultiTenant multiTenant = databaseProperties.getMultiTenant();
        if (!multiTenant.isEnabled()) {
            return multiTenant.getDefaultTenantId();
        }

        String tenantId = TenantUtil.getTenantId();
        if (StringUtils.hasText(tenantId)) {
            return tenantId;
        }
        if (multiTenant.isRequired()) {
            throw new IllegalStateException("Tenant context is required for global unique operations");
        }
        return GLOBAL_TENANT;
    }
}
