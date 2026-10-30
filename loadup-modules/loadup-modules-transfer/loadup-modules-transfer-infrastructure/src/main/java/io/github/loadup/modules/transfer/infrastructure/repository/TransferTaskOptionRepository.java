package io.github.loadup.modules.transfer.infrastructure.repository;

import io.github.loadup.components.database.tenant.TenantContext;
import io.github.loadup.modules.transfer.infrastructure.dataobject.TransferTaskOptionRow;
import java.util.List;
import org.springframework.data.repository.ListCrudRepository;

/** Spring Data JDBC reads for transfer task options. Tenant identity comes from the execution context. */
public interface TransferTaskOptionRepository extends ListCrudRepository<TransferTaskOptionRow, String> {

    default List<TransferTaskOptionRow> options(String taskId) {
        return findByTenantIdAndTaskIdAndDeletedFalseOrderByOptionNameAsc(TenantContext.tenantId(), taskId);
    }

    List<TransferTaskOptionRow> findByTenantIdAndTaskIdAndDeletedFalseOrderByOptionNameAsc(
            String tenantId, String taskId);
}
