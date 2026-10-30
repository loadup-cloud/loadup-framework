package io.github.loadup.modules.merchant.app.service;

import io.github.loadup.modules.merchant.client.dto.MerchantPersistenceFacts;
import io.github.loadup.modules.merchant.client.facade.MerchantPersistenceFacade;
import io.github.loadup.modules.merchant.domain.gateway.MerchantGateway;
import io.github.loadup.modules.merchant.domain.model.Merchant;
import io.github.loadup.modules.merchant.domain.model.MerchantStatus;
import java.util.Optional;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Local-only facts adapter; lock is held by the caller's transaction.
 */
public class MerchantPersistenceService implements MerchantPersistenceFacade {
    private final MerchantGateway gateway;

    public MerchantPersistenceService(MerchantGateway gateway) {
        this.gateway = gateway;
    }

    private static MerchantPersistenceFacts facts(Merchant merchant) {
        return new MerchantPersistenceFacts(
                merchant.id(), merchant.status() == MerchantStatus.ACTIVE, merchant.rowVersion());
    }

    @Override
    public Optional<MerchantPersistenceFacts> find(String id) {
        return gateway.find(id).map(MerchantPersistenceService::facts);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public Optional<MerchantPersistenceFacts> lock(String id) {
        return gateway.lock(id).map(MerchantPersistenceService::facts);
    }
}
