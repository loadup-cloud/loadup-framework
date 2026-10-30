package io.github.loadup.components.dfs.database.repository;

import io.github.loadup.components.database.tenant.TenantContext;
import io.github.loadup.components.dfs.database.dataobject.DfsFileInsert;
import java.util.Optional;
import org.springframework.data.repository.ListCrudRepository;

/** Spring Data JDBC reads for database-backed DFS objects. Tenant identity comes from the execution context. */
public interface DfsFileRepository extends ListCrudRepository<DfsFileInsert, String> {

    default Optional<DfsFileInsert> find(String fileId) {
        return findByTenantIdAndIdAndDeletedFalse(TenantContext.tenantId(), fileId);
    }

    default boolean exists(String fileId) {
        return existsByTenantIdAndIdAndDeletedFalse(TenantContext.tenantId(), fileId);
    }

    Optional<DfsFileInsert> findByTenantIdAndIdAndDeletedFalse(String tenantId, String id);

    boolean existsByTenantIdAndIdAndDeletedFalse(String tenantId, String id);
}
