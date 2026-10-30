package io.github.loadup.modules.upms.infrastructure.repository;

import io.github.loadup.components.database.tenant.TenantContext;
import io.github.loadup.modules.upms.infrastructure.dataobject.UserDO;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.ListPagingAndSortingRepository;

/** Spring Data JDBC reads for the user aggregate. Writes remain conditional SQL. */
public interface UserRepository
        extends ListCrudRepository<UserDO, String>, ListPagingAndSortingRepository<UserDO, String> {

    @Override
    default Optional<UserDO> findById(String id) {
        return findByTenantIdAndIdAndDeletedFalse(TenantContext.tenantId(), id);
    }

    default Optional<UserDO> findByUsername(String username) {
        return findByTenantIdAndUsernameAndDeletedFalse(TenantContext.tenantId(), username);
    }

    default Optional<UserDO> findByEmail(String email) {
        return findByTenantIdAndEmailAndDeletedFalse(TenantContext.tenantId(), email);
    }

    default Optional<UserDO> findByMobile(String mobile) {
        return findByTenantIdAndMobileAndDeletedFalse(TenantContext.tenantId(), mobile);
    }

    default List<UserDO> findByDeptId(String deptId) {
        return findByTenantIdAndDeptIdAndDeletedFalse(TenantContext.tenantId(), deptId);
    }

    @Override
    default Page<UserDO> findAll(Pageable pageable) {
        return findByTenantIdAndDeletedFalse(TenantContext.tenantId(), pageable);
    }

    default long countByDeptId(String deptId) {
        return countByTenantIdAndDeptIdAndDeletedFalse(TenantContext.tenantId(), deptId);
    }

    default boolean existsByUsername(String username) {
        return existsByTenantIdAndUsernameAndDeletedFalse(TenantContext.tenantId(), username);
    }

    default boolean existsByEmail(String email) {
        return existsByTenantIdAndEmailAndDeletedFalse(TenantContext.tenantId(), email);
    }

    default boolean existsByMobile(String mobile) {
        return existsByTenantIdAndMobileAndDeletedFalse(TenantContext.tenantId(), mobile);
    }

    Optional<UserDO> findByTenantIdAndIdAndDeletedFalse(String tenantId, String id);

    Optional<UserDO> findByTenantIdAndUsernameAndDeletedFalse(String tenantId, String username);

    Optional<UserDO> findByTenantIdAndEmailAndDeletedFalse(String tenantId, String email);

    Optional<UserDO> findByTenantIdAndMobileAndDeletedFalse(String tenantId, String mobile);

    List<UserDO> findByTenantIdAndDeptIdAndDeletedFalse(String tenantId, String deptId);

    Page<UserDO> findByTenantIdAndDeletedFalse(String tenantId, Pageable pageable);

    long countByTenantIdAndDeptIdAndDeletedFalse(String tenantId, String deptId);

    boolean existsByTenantIdAndUsernameAndDeletedFalse(String tenantId, String username);

    boolean existsByTenantIdAndEmailAndDeletedFalse(String tenantId, String email);

    boolean existsByTenantIdAndMobileAndDeletedFalse(String tenantId, String mobile);
}
