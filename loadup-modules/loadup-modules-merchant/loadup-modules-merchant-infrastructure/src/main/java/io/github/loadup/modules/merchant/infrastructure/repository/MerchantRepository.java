package io.github.loadup.modules.merchant.infrastructure.repository;

import io.github.loadup.components.database.tenant.TenantContext;
import io.github.loadup.modules.merchant.infrastructure.dataobject.MerchantDO;
import java.util.Optional;
import org.springframework.data.repository.ListCrudRepository;

/** Spring Data JDBC reads for merchant profiles. Row locks and conditional writes stay on SQL. */
public interface MerchantRepository extends ListCrudRepository<MerchantDO, String> {

    default Optional<MerchantDO> find(String id) {
        return findByTenantIdAndIdAndDeletedFalse(TenantContext.tenantId(), id);
    }

    Optional<MerchantDO> findByTenantIdAndIdAndDeletedFalse(String tenantId, String id);
}
