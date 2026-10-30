package io.github.loadup.modules.transfer.infrastructure.repository;

import io.github.loadup.components.database.tenant.TenantContext;
import io.github.loadup.modules.transfer.infrastructure.dataobject.TransferTaskRow;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.ListCrudRepository;

/** Spring Data JDBC reads for transfer tasks. Conditional state updates stay on explicit SQL. */
public interface TransferTaskRepository extends ListCrudRepository<TransferTaskRow, String> {

    default Page<TransferTaskRow> listOwned(String ownerId, Pageable pageable) {
        return findByTenantIdAndOwnerIdAndDeletedFalse(TenantContext.tenantId(), ownerId, pageable);
    }

    Optional<TransferTaskRow> findByTenantIdAndIdAndDeletedFalse(String tenantId, String id);

    Optional<TransferTaskRow> findByIdAndDeletedFalse(String id);

    Page<TransferTaskRow> findByTenantIdAndOwnerIdAndDeletedFalse(String tenantId, String ownerId, Pageable pageable);

    Page<TransferTaskRow> findByStatusAndDeletedFalse(String status, Pageable pageable);
}
