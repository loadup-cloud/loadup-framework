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

package io.github.loadup.components.database.tenant;

/** Holds the tenant ID for the current execution. */
public final class TenantContextHolder {
    private static final ThreadLocal<String> TENANT_ID = new ThreadLocal<>();

    private TenantContextHolder() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void setTenantId(String tenantId) {
        if (tenantId == null) {
            clear();
            return;
        }
        String value = tenantId.trim();
        if (value.isEmpty()) {
            clear();
            return;
        }
        TENANT_ID.set(value);
    }

    public static String getTenantId() {
        return TENANT_ID.get();
    }

    public static boolean hasTenantId() {
        return TENANT_ID.get() != null;
    }

    public static void clear() {
        TENANT_ID.remove();
    }

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
