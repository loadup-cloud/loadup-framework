/* Copyright 2026 LoadUp Cloud. Licensed under the Apache License, Version 2.0. */
package io.github.loadup.components.database.tenant;

import io.github.loadup.commons.util.TenantUtil;
import io.github.loadup.components.database.config.DatabaseProperties;

/** Resolves the trusted context for explicit SQL and enforces the configured missing-tenant policy. */
public final class DatabaseTenant {
    private final DatabaseProperties.MultiTenant properties;

    public DatabaseTenant(DatabaseProperties properties) {
        this.properties = properties.getMultiTenant();
    }

    public String current() {
        if (!properties.isEnabled()) return properties.getDefaultTenantId();
        String tenant = TenantUtil.getTenantId();
        if (tenant != null && !tenant.isBlank()) return tenant.trim();
        if (properties.isRequired()) throw new IllegalStateException("Tenant context is required for database access");
        return null;
    }
}
