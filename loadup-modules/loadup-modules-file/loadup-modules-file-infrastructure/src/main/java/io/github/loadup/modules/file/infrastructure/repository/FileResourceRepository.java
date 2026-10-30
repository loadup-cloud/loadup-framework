package io.github.loadup.modules.file.infrastructure.repository;

import io.github.loadup.components.database.tenant.TenantContext;
import io.github.loadup.modules.file.infrastructure.dataobject.FileResourceInsert;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.ListCrudRepository;

/** Spring Data JDBC reads for file resources. Row locks and state changes stay on explicit SQL. */
public interface FileResourceRepository extends ListCrudRepository<FileResourceInsert, String> {

    default Optional<FileResourceInsert> find(String id) {
        return findByTenantIdAndIdAndDeletedFalse(TenantContext.tenantId(), id);
    }

    default Page<FileResourceInsert> listOwned(String ownerId, Pageable pageable) {
        return findByTenantIdAndOwnerIdAndStateAndDeletedFalse(TenantContext.tenantId(), ownerId, "ACTIVE", pageable);
    }

    default List<FileResourceInsert> pending(Sort sort) {
        return findByStateAndDeletedFalse("PENDING_DELETE", sort);
    }

    Optional<FileResourceInsert> findByTenantIdAndIdAndDeletedFalse(String tenantId, String id);

    Page<FileResourceInsert> findByTenantIdAndOwnerIdAndStateAndDeletedFalse(
            String tenantId, String ownerId, String state, Pageable pageable);

    List<FileResourceInsert> findByStateAndDeletedFalse(String state, Sort sort);
}
