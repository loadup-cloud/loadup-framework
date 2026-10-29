package io.github.loadup.modules.merchant.client.facade;

import io.github.loadup.modules.merchant.client.dto.MerchantPersistenceFacts;
import java.util.Optional;

/** Local merchant facts; lock requires a transaction on the same data source. */
public interface MerchantPersistenceFacade {
    Optional<MerchantPersistenceFacts> find(String tenantId, String merchantId);

    Optional<MerchantPersistenceFacts> lock(String tenantId, String merchantId);
}
