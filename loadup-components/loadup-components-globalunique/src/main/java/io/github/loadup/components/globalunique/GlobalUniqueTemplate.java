package io.github.loadup.components.globalunique;

import io.github.loadup.components.globalunique.model.GlobalUniqueClaim;
import io.github.loadup.components.globalunique.model.GlobalUniqueRecord;
import java.util.Optional;

/** Claims tenant-scoped idempotency keys through a database unique constraint. */
public interface GlobalUniqueTemplate {

    /**
     * Claims a business key in the current tenant.
     *
     * @param bizType business namespace
     * @param uniqueKey unique key inside the namespace
     * @return {@code true} for the first claim, otherwise {@code false}
     */
    boolean claim(String bizType, String uniqueKey);

    /**
     * Claims a business key and stores optional diagnostic data.
     *
     * @param claim claim data
     * @return {@code true} for the first claim, otherwise {@code false}
     */
    boolean claim(GlobalUniqueClaim claim);

    /**
     * Finds a claim in the current tenant.
     *
     * @param bizType business namespace
     * @param uniqueKey unique key inside the namespace
     * @return the matching record when present
     */
    Optional<GlobalUniqueRecord> find(String bizType, String uniqueKey);
}
