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
