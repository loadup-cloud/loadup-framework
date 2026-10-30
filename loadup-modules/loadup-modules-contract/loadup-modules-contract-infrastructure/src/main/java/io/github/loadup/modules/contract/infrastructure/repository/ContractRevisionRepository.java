package io.github.loadup.modules.contract.infrastructure.repository;

import io.github.loadup.components.database.tenant.TenantContext;
import io.github.loadup.modules.contract.infrastructure.dataobject.ContractRevisionRow;
import java.util.Optional;
import org.springframework.data.repository.ListCrudRepository;

/** Spring Data JDBC reads for contract revisions. */
public interface ContractRevisionRepository extends ListCrudRepository<ContractRevisionRow, String> {

    default Optional<ContractRevisionRow> revision(String contractId, int revision) {
        return findByTenantIdAndContractIdAndRevisionAndDeletedFalse(TenantContext.tenantId(), contractId, revision);
    }

    Optional<ContractRevisionRow> findByTenantIdAndContractIdAndRevisionAndDeletedFalse(
            String tenantId, String contractId, int revision);
}
