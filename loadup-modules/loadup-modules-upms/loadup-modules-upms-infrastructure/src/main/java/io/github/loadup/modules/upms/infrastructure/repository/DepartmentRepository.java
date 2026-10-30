package io.github.loadup.modules.upms.infrastructure.repository;

import io.github.loadup.components.database.tenant.TenantContext;
import io.github.loadup.modules.upms.infrastructure.dataobject.DepartmentDO;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.ListCrudRepository;

/** Spring Data JDBC reads for department nodes. Tenant identity always comes from the execution context. */
public interface DepartmentRepository extends ListCrudRepository<DepartmentDO, String> {

    @Override
    default Optional<DepartmentDO> findById(String id) {
        return findByTenantIdAndIdAndDeletedFalse(TenantContext.tenantId(), id);
    }

    default Optional<DepartmentDO> findByDeptCode(String deptCode) {
        return findByTenantIdAndDeptCodeAndDeletedFalse(TenantContext.tenantId(), deptCode);
    }

    default List<DepartmentDO> findByParentId(String parentId) {
        return findByTenantIdAndParentIdAndDeletedFalse(TenantContext.tenantId(), parentId);
    }

    @Override
    default List<DepartmentDO> findAll() {
        return findByTenantIdAndDeletedFalse(TenantContext.tenantId());
    }

    default List<DepartmentDO> findAllEnabled() {
        return findByTenantIdAndStatusAndDeletedFalse(TenantContext.tenantId(), (short) 1);
    }

    default boolean existsByDeptCode(String deptCode) {
        return existsByTenantIdAndDeptCodeAndDeletedFalse(TenantContext.tenantId(), deptCode);
    }

    Optional<DepartmentDO> findByTenantIdAndIdAndDeletedFalse(String tenantId, String id);

    Optional<DepartmentDO> findByTenantIdAndDeptCodeAndDeletedFalse(String tenantId, String deptCode);

    List<DepartmentDO> findByTenantIdAndParentIdAndDeletedFalse(String tenantId, String parentId);

    List<DepartmentDO> findByTenantIdAndDeletedFalse(String tenantId);

    List<DepartmentDO> findByTenantIdAndStatusAndDeletedFalse(String tenantId, Short status);

    boolean existsByTenantIdAndDeptCodeAndDeletedFalse(String tenantId, String deptCode);
}
