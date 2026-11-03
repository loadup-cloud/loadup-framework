/*
 * Copyright (C) 2025-2026 LoadUp Cloud
 *
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
 */
package io.github.loadup.components.database.tenant;

import io.github.loadup.commons.error.CommonException;
import io.github.loadup.commons.util.TenantUtil;
import io.github.loadup.components.database.config.DatabaseProperties;
import java.util.regex.Pattern;

/**
 * Resolves the trusted tenant and its database schema.
 *
 * <p>Schema-per-tenant isolation keeps tenant identity out of business tables. The tenant id is the
 * routing key; until a tenant registry is introduced, the tenant id is also the schema name. The
 * default tenant always uses the configured default schema.
 */
public final class DatabaseTenant {
    private static final Pattern SCHEMA_NAME = Pattern.compile("[a-z_][a-z0-9_]{0,62}");

    private final DatabaseProperties databaseProperties;
    private final DatabaseProperties.MultiTenant properties;

    public DatabaseTenant(DatabaseProperties properties) {
        this.databaseProperties = properties;
        this.properties = properties.getMultiTenant();
    }

    private static String requireSchema(String schema) {
        if (schema == null || !SCHEMA_NAME.matcher(schema).matches()) {
            throw new CommonException(DatabaseTenantResultCodeEnum.INVALID_SCHEMA);
        }
        return schema;
    }

    public String currentTenantId() {
        if (!properties.isEnabled()) {
            return properties.getDefaultTenantId();
        }
        String tenant = TenantUtil.getTenantId();
        if (tenant != null && !tenant.isBlank()) {
            return tenant.trim();
        }
        if (properties.isRequired()) {
            throw new CommonException(DatabaseTenantResultCodeEnum.TENANT_REQUIRED);
        }
        return properties.getDefaultTenantId();
    }

    /**
     * Returns the physical schema for the current request or task. Schema names are validated
     * before they can reach a connection, so tenant input can never alter SQL structure.
     */
    public String currentSchema() {
        if (!properties.isEnabled()) {
            return requireSchema(databaseProperties.getDefaultSchema());
        }
        String tenant = TenantUtil.getTenantId();
        if (tenant == null || tenant.isBlank()) {
            if (properties.isRequired()) {
                throw new CommonException(DatabaseTenantResultCodeEnum.TENANT_REQUIRED);
            }
            return requireSchema(databaseProperties.getDefaultSchema());
        }
        return requireSchema(tenant.trim());
    }
}
