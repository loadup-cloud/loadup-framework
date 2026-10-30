package io.github.loadup.modules.file.infrastructure.repository;

import io.github.loadup.components.database.tenant.TenantContext;
import io.github.loadup.modules.file.infrastructure.dataobject.FileReferenceRow;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.ListCrudRepository;

/** Spring Data JDBC reads for file references. Tenant identity comes from the execution context. */
public interface FileReferenceRepository extends ListCrudRepository<FileReferenceRow, String> {

    default List<FileReferenceRow> references(String fileId, Sort sort) {
        return findByTenantIdAndFileIdAndDeletedFalse(TenantContext.tenantId(), fileId, sort);
    }

    default long countReferences(String fileId) {
        return countByTenantIdAndFileIdAndDeletedFalse(TenantContext.tenantId(), fileId);
    }

    List<FileReferenceRow> findByTenantIdAndFileIdAndDeletedFalse(String tenantId, String fileId, Sort sort);

    long countByTenantIdAndFileIdAndDeletedFalse(String tenantId, String fileId);
}
