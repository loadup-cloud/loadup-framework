package io.github.loadup.modules.merchant.app.service;

import io.github.loadup.commons.error.CommonException;
import io.github.loadup.modules.merchant.client.dto.MerchantPersistenceFacts;
import io.github.loadup.modules.merchant.client.error.MerchantResultCodeEnum;
import io.github.loadup.modules.merchant.client.facade.MerchantPersistenceFacade;
import io.github.loadup.modules.merchant.domain.gateway.MerchantGateway;
import io.github.loadup.modules.merchant.domain.model.Merchant;
import io.github.loadup.modules.merchant.domain.model.MerchantStatus;
import java.util.Optional;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Local-only facts adapter; lock is held by the caller's transaction. */
public class MerchantPersistenceService implements MerchantPersistenceFacade {
    private final MerchantGateway gateway;

    public MerchantPersistenceService(MerchantGateway gateway) {
        this.gateway = gateway;
    }

    @Override
    public Optional<MerchantPersistenceFacts> find(String tenant, String id) {
        checkTenant(tenant);
        return gateway.find(tenant, id).map(MerchantPersistenceService::facts);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public Optional<MerchantPersistenceFacts> lock(String tenant, String id) {
        checkTenant(tenant);
        return gateway.lock(tenant, id).map(MerchantPersistenceService::facts);
    }

    private static void checkTenant(String tenant) {
        if (!MerchantIdentity.tenant().equals(tenant))
            throw new CommonException(MerchantResultCodeEnum.MERCHANT_LOOKUP_TENANT_MISMATCH);
    }

    private static MerchantPersistenceFacts facts(Merchant merchant) {
        return new MerchantPersistenceFacts(
                merchant.id(), merchant.status() == MerchantStatus.ACTIVE, merchant.rowVersion());
    }
}
