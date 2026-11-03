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
