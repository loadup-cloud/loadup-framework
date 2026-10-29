package io.github.loadup.modules.contract.client.facade;

import io.github.loadup.modules.contract.client.dto.ContractPersistenceHeader;
import io.github.loadup.modules.contract.client.dto.ContractPersistenceRevision;
import java.util.Optional;

/** Local-only contract facts; locking requires a caller transaction on the same data source. */
public interface ContractPersistenceFacade {
    Optional<ContractPersistenceHeader> lockByScope(String tenantId, String merchantId, String scopeKey);

    Optional<ContractPersistenceRevision> revision(String tenantId, String contractId, int revision);
}
