package io.github.loadup.modules.contract.app.service;

import io.github.loadup.modules.contract.client.dto.ContractPersistenceHeader;
import io.github.loadup.modules.contract.client.dto.ContractPersistenceRevision;
import io.github.loadup.modules.contract.client.facade.ContractPersistenceFacade;
import io.github.loadup.modules.contract.domain.gateway.MerchantContractGateway;
import java.util.Optional;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public class ContractPersistenceService implements ContractPersistenceFacade {
    private final MerchantContractGateway gateway;

    public ContractPersistenceService(MerchantContractGateway gateway) {
        this.gateway = gateway;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public Optional<ContractPersistenceHeader> lockByScope(String merchant, String scope) {
        return gateway.lockByScope(merchant, scope)
                .map(row -> new ContractPersistenceHeader(
                        row.id(),
                        row.merchantId(),
                        row.scopeKey(),
                        row.planVersionId(),
                        row.status().name(),
                        row.generation(),
                        row.requestKey(),
                        row.requestDigest(),
                        row.createdBy(),
                        row.createdAt(),
                        row.updatedAt()));
    }

    @Override
    public Optional<ContractPersistenceRevision> revision(String id, int revision) {
        return gateway.revision(id, revision)
                .map(row -> new ContractPersistenceRevision(row.revision(), row.snapshot(), row.snapshotHash()));
    }
}
