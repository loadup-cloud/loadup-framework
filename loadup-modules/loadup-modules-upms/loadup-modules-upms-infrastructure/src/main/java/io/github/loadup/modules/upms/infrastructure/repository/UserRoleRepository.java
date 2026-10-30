package io.github.loadup.modules.upms.infrastructure.repository;

import io.github.loadup.components.database.tenant.TenantContext;
import io.github.loadup.modules.upms.infrastructure.dataobject.UserRoleDO;
import java.util.List;
import org.springframework.data.repository.ListCrudRepository;

/** Spring Data JDBC reads for user-role links. Tenant identity always comes from the execution context. */
public interface UserRoleRepository extends ListCrudRepository<UserRoleDO, String> {

    default List<UserRoleDO> findByUserId(String userId) {
        return findByTenantIdAndUserIdAndDeletedFalse(TenantContext.tenantId(), userId);
    }

    default long countByRoleId(String roleId) {
        return countByTenantIdAndRoleIdAndDeletedFalse(TenantContext.tenantId(), roleId);
    }

    List<UserRoleDO> findByTenantIdAndUserIdAndDeletedFalse(String tenantId, String userId);

    long countByTenantIdAndRoleIdAndDeletedFalse(String tenantId, String roleId);
}
