package io.github.loadup.modules.upms.infrastructure.repository;

import io.github.loadup.components.database.tenant.TenantContext;
import io.github.loadup.modules.upms.infrastructure.dataobject.RoleDO;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.ListPagingAndSortingRepository;

/** Spring Data JDBC reads for roles. Tenant identity always comes from the execution context. */
public interface RoleRepository
        extends ListCrudRepository<RoleDO, String>, ListPagingAndSortingRepository<RoleDO, String> {

    @Override
    default Optional<RoleDO> findById(String id) {
        return findByTenantIdAndIdAndDeletedFalse(TenantContext.tenantId(), id);
    }

    default Optional<RoleDO> findByRoleCode(String roleCode) {
        return findByTenantIdAndRoleCodeAndDeletedFalse(TenantContext.tenantId(), roleCode);
    }

    default List<RoleDO> findByParentId(String parentId) {
        return findByTenantIdAndParentIdAndDeletedFalse(TenantContext.tenantId(), parentId);
    }

    @Override
    default List<RoleDO> findAll() {
        return findByTenantIdAndDeletedFalse(TenantContext.tenantId(), Sort.unsorted());
    }

    @Override
    default List<RoleDO> findAll(Sort sort) {
        return findByTenantIdAndDeletedFalse(TenantContext.tenantId(), sort);
    }

    @Override
    default Page<RoleDO> findAll(Pageable pageable) {
        return findByTenantIdAndDeletedFalse(TenantContext.tenantId(), pageable);
    }

    default List<RoleDO> findAllEnabled() {
        return findByTenantIdAndStatusAndDeletedFalse(TenantContext.tenantId(), (short) 1, Sort.unsorted());
    }

    default boolean existsByRoleCode(String roleCode) {
        return existsByTenantIdAndRoleCodeAndDeletedFalse(TenantContext.tenantId(), roleCode);
    }

    Optional<RoleDO> findByTenantIdAndIdAndDeletedFalse(String tenantId, String id);

    Optional<RoleDO> findByTenantIdAndRoleCodeAndDeletedFalse(String tenantId, String roleCode);

    List<RoleDO> findByTenantIdAndParentIdAndDeletedFalse(String tenantId, String parentId);

    List<RoleDO> findByTenantIdAndDeletedFalse(String tenantId, Sort sort);

    List<RoleDO> findByTenantIdAndStatusAndDeletedFalse(String tenantId, Short status, Sort sort);

    Page<RoleDO> findByTenantIdAndDeletedFalse(String tenantId, Pageable pageable);

    boolean existsByTenantIdAndRoleCodeAndDeletedFalse(String tenantId, String roleCode);
}
