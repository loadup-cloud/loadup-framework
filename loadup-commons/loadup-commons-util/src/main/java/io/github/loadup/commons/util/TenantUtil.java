package io.github.loadup.commons.util;

/** Holds the current tenant ID for the execution context. */
public final class TenantUtil {
    private static final ThreadLocal<String> TENANT_ID = new ThreadLocal<>();

    private TenantUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Binds the tenant ID to the current thread. A blank value clears the context.
     *
     * @param tenantId tenant ID, may be null or blank
     */
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

    /**
     * Return the tenant ID bound to the current thread.
     *
     * @return tenant ID or null when no tenant is bound
     */
    public static String getTenantId() {
        return TENANT_ID.get();
    }

    /**
     * Whether a tenant ID is bound to the current thread.
     *
     * @return true when a tenant ID is present
     */
    public static boolean hasTenantId() {
        return TENANT_ID.get() != null;
    }

    /** Remove the tenant binding from the current thread. */
    public static void clear() {
        TENANT_ID.remove();
    }

    /**
     * Run the callback under a temporary tenant context and restore the previous binding.
     *
     * @param tenantId temporary tenant ID
     * @param runnable callback
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
