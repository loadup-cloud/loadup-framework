package io.github.loadup.modules.contract.app.service;

import io.github.loadup.commons.error.CommonException;
import io.github.loadup.modules.contract.app.support.ContractIdentity;
import io.github.loadup.modules.contract.client.dto.ContractPersistenceHeader;
import io.github.loadup.modules.contract.client.dto.ContractPersistenceRevision;
import io.github.loadup.modules.contract.client.error.ContractResultCodeEnum;
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
    public Optional<ContractPersistenceHeader> lockByScope(String tenant, String merchant, String scope) {
        checkTenant(tenant);
        return gateway.lockByScope(tenant, merchant, scope)
                .map(row -> new ContractPersistenceHeader(
                        row.id(),
                        row.tenantId(),
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
    public Optional<ContractPersistenceRevision> revision(String tenant, String id, int revision) {
        checkTenant(tenant);
        return gateway.revision(tenant, id, revision)
                .map(row -> new ContractPersistenceRevision(row.revision(), row.snapshot(), row.snapshotHash()));
    }

    private static void checkTenant(String tenant) {
        if (!ContractIdentity.tenant().equals(tenant))
            throw new CommonException(ContractResultCodeEnum.CONTRACT_IDENTITY_MISMATCH);
    }
}
