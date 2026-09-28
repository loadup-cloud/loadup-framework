package io.github.loadup.modules.upms.infrastructure.converter;

/** Converts domain audit values to the database representation. */
public final class AuditMappingSupport {
    private AuditMappingSupport() {}

    public static Integer toDeletedFlag(Boolean deleted) {
        return deleted == null ? null : (deleted ? 1 : 0);
    }

    public static Boolean toDeleted(Integer deleted) {
        return deleted != null && deleted != 0;
    }
}
