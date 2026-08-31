package io.github.loadup.components.database.tenant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*-
 * #%L
 * loadup-components-database
 * %%
 * Copyright (C) 2022 - 2026 loadup_cloud
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

/**
 * Tenant Context Holder
 *
 * <p>Thread-local storage for current tenant ID. Used by multi-tenant feature to automatically
 * filter queries and set tenant_id on insert/update operations.
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
public final class TenantContextHolder {
    private static final Logger log = LoggerFactory.getLogger(TenantContextHolder.class);

    private static final ThreadLocal<String> TENANT_ID = new InheritableThreadLocal<>();

    private TenantContextHolder() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Set current tenant ID
     *
     * @param tenantId tenant ID
     */
    public static void setTenantId(String tenantId) {
        if (tenantId == null) {
            log.warn("Attempting to set null tenant ID, clearing context instead");
            clear();
            return;
        }
        TENANT_ID.set(tenantId);
        log.debug("Set tenant context: {}", tenantId);
    }

    /**
     * Get current tenant ID
     *
     * @return tenant ID, or null if not set
     */
    public static String getTenantId() {
        return TENANT_ID.get();
    }

    /**
     * Check if tenant context is set
     *
     * @return true if tenant context exists
     */
    public static boolean hasTenantId() {
        return TENANT_ID.get() != null;
    }

    /**
     * Clear tenant context
     */
    public static void clear() {
        TENANT_ID.remove();
        log.debug("Cleared tenant context");
    }

    /**
     * Execute code with specific tenant context
     *
     * @param tenantId tenant ID
     * @param runnable code to execute
     */
    public static void runWithTenant(String tenantId, Runnable runnable) {
        String previousTenantId = getTenantId();
        try {
            setTenantId(tenantId);
            runnable.run();
        } finally {
            if (previousTenantId != null) {
                setTenantId(previousTenantId);
            } else {
                clear();
            }
        }
    }
}
