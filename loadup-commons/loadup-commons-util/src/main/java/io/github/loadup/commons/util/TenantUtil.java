/*-
 * #%L
 * Loadup Common Utils
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
package io.github.loadup.commons.util;

import io.github.loadup.commons.context.ContextHolder;
import io.github.loadup.commons.context.ContextKeys;

/** Read-only tenant metadata; temporary overrides are confined to callback scopes. */
public final class TenantUtil {
    private TenantUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static String getTenantId() {
        return ContextHolder.get(ContextKeys.TENANT_ID);
    }

    public static boolean hasTenantId() {
        return getTenantId() != null;
    }

    public static void runWithTenant(String tenantId, Runnable action) {
        callWithTenant(tenantId, () -> {
            action.run();
            return null;
        });
    }

    public static <T, X extends Throwable> T callWithTenant(String tenantId, ScopedValue.CallableOp<T, X> action)
            throws X {
        String normalized = tenantId == null || tenantId.isBlank() ? null : tenantId.trim();
        return ContextHolder.callWith(ContextHolder.current().with(ContextKeys.TENANT_ID, normalized), action);
    }
}
