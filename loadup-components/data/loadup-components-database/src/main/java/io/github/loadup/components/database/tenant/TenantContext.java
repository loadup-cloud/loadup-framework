/* Copyright 2026 LoadUp Cloud. Licensed under the Apache License, Version 2.0. */
package io.github.loadup.components.database.tenant;

import io.github.loadup.commons.constant.CommonConstants;
import io.github.loadup.commons.util.TenantUtil;

/**
 * Static view of the trusted tenant and its schema.
 *
 * <p>The database component registers its configured {@link DatabaseTenant} resolver at startup.
 * A future {@code TenantRoutingDataSource} will use {@link #tenantId()} as its
 * {@code AbstractRoutingDataSource} lookup key. Until a resolver is registered (for example in
 * plain unit tests), the raw execution-context tenant is used.
 */
public final class TenantContext {
    private static volatile DatabaseTenant resolver;

    private TenantContext() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void bind(DatabaseTenant databaseTenant) {
        resolver = databaseTenant;
    }

    public static String tenantId() {
        DatabaseTenant current = resolver;
        return current == null ? TenantUtil.getTenantId() : current.currentTenantId();
    }

    public static String schema() {
        DatabaseTenant current = resolver;
        return current == null ? CommonConstants.DEFAULT_SCHEMA : current.currentSchema();
    }
}
