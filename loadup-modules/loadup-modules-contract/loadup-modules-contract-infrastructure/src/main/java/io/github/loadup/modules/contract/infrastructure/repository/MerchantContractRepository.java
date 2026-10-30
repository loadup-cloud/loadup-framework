package io.github.loadup.modules.contract.infrastructure.repository;

import io.github.loadup.components.database.tenant.TenantContext;
import io.github.loadup.modules.contract.infrastructure.dataobject.MerchantContractRow;
import java.util.Optional;
import org.springframework.data.repository.ListCrudRepository;

/** Spring Data JDBC reads for merchant contracts. Conditional updates stay on explicit SQL. */
public interface MerchantContractRepository extends ListCrudRepository<MerchantContractRow, String> {

    default Optional<MerchantContractRow> find(String id) {
        return findByTenantIdAndIdAndDeletedFalse(TenantContext.tenantId(), id);
    }

    default Optional<MerchantContractRow> findByScope(String merchantId, String scopeKey) {
        return findByTenantIdAndMerchantIdAndScopeKeyAndDeletedFalse(TenantContext.tenantId(), merchantId, scopeKey);
    }

    default Optional<MerchantContractRow> findByRequest(String requestKey) {
        return findByTenantIdAndRequestKeyAndDeletedFalse(TenantContext.tenantId(), requestKey);
    }

    Optional<MerchantContractRow> findByTenantIdAndIdAndDeletedFalse(String tenantId, String id);

    Optional<MerchantContractRow> findByTenantIdAndMerchantIdAndScopeKeyAndDeletedFalse(
            String tenantId, String merchantId, String scopeKey);

    Optional<MerchantContractRow> findByTenantIdAndRequestKeyAndDeletedFalse(String tenantId, String requestKey);
}
