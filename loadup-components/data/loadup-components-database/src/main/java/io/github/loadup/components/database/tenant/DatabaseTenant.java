/* Copyright 2026 LoadUp Cloud. Licensed under the Apache License, Version 2.0. */
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

    private final DatabaseProperties.MultiTenant properties;

    public DatabaseTenant(DatabaseProperties properties) {
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
            return requireSchema(properties.getDefaultSchema());
        }
        String tenant = TenantUtil.getTenantId();
        if (tenant == null || tenant.isBlank()) {
            if (properties.isRequired()) {
                throw new CommonException(DatabaseTenantResultCodeEnum.TENANT_REQUIRED);
            }
            return requireSchema(properties.getDefaultSchema());
        }
        return requireSchema(tenant.trim());
    }
}
