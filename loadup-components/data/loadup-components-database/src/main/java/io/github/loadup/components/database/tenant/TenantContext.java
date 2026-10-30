/* Copyright 2026 LoadUp Cloud. Licensed under the Apache License, Version 2.0. */
package io.github.loadup.components.database.tenant;

import io.github.loadup.commons.util.TenantUtil;

/**
 * Read-only access to the trusted tenant for repository queries.
 *
 * <p>The database component registers the configured {@link DatabaseTenant} resolver at startup.
 * Repository default methods use this accessor so callers cannot supply an arbitrary tenant
 * identifier to a query.
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
        return current == null ? TenantUtil.getTenantId() : current.current();
    }
}
