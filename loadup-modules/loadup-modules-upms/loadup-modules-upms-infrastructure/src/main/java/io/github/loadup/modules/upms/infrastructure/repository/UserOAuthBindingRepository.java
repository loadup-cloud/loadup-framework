package io.github.loadup.modules.upms.infrastructure.repository;

import io.github.loadup.components.database.tenant.TenantContext;
import io.github.loadup.modules.upms.infrastructure.dataobject.UserOAuthBindingDO;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.ListCrudRepository;

/** Spring Data JDBC reads for OAuth bindings. Tenant identity always comes from the execution context. */
public interface UserOAuthBindingRepository extends ListCrudRepository<UserOAuthBindingDO, String> {

    default Optional<UserOAuthBindingDO> findByProviderAndOpenId(String provider, String openId) {
        return findByTenantIdAndProviderAndOpenIdAndDeletedFalse(TenantContext.tenantId(), provider, openId);
    }

    default List<UserOAuthBindingDO> findByUserId(String userId) {
        return findByTenantIdAndUserIdAndDeletedFalse(TenantContext.tenantId(), userId);
    }

    default Optional<UserOAuthBindingDO> findByUserIdAndProvider(String userId, String provider) {
        return findByTenantIdAndUserIdAndProviderAndDeletedFalse(TenantContext.tenantId(), userId, provider);
    }

    Optional<UserOAuthBindingDO> findByTenantIdAndProviderAndOpenIdAndDeletedFalse(
            String tenantId, String provider, String openId);

    List<UserOAuthBindingDO> findByTenantIdAndUserIdAndDeletedFalse(String tenantId, String userId);

    Optional<UserOAuthBindingDO> findByTenantIdAndUserIdAndProviderAndDeletedFalse(
            String tenantId, String userId, String provider);
}
