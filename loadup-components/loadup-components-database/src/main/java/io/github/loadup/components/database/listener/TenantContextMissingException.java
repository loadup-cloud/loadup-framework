package io.github.loadup.components.database.listener;

/** Raised when a required tenant context is missing. */
public class TenantContextMissingException extends IllegalStateException {
    private static final long serialVersionUID = 1L;

    public TenantContextMissingException(Class<?> entityType) {
        super("Tenant context is required for entity " + entityType.getName());
    }

    public TenantContextMissingException(String tableName) {
        super("Tenant context is required for table " + tableName);
    }
}
