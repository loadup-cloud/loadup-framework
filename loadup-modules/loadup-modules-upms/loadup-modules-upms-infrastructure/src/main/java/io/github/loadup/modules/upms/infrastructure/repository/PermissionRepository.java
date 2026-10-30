package io.github.loadup.modules.upms.infrastructure.repository;

import io.github.loadup.components.database.tenant.TenantContext;
import io.github.loadup.modules.upms.infrastructure.dataobject.PermissionDO;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.ListCrudRepository;

/** Spring Data JDBC reads for permission nodes. Tenant identity always comes from the execution context. */
public interface PermissionRepository extends ListCrudRepository<PermissionDO, String> {

    @Override
    default Optional<PermissionDO> findById(String id) {
        return findByTenantIdAndIdAndDeletedFalse(TenantContext.tenantId(), id);
    }

    default Optional<PermissionDO> findByPermissionCode(String permissionCode) {
        return findByTenantIdAndPermissionCodeAndDeletedFalse(TenantContext.tenantId(), permissionCode);
    }

    default List<PermissionDO> findByParentId(String parentId) {
        return findByTenantIdAndParentIdAndDeletedFalse(TenantContext.tenantId(), parentId);
    }

    default List<PermissionDO> findByPermissionType(Short permissionType) {
        return findByTenantIdAndPermissionTypeAndDeletedFalse(TenantContext.tenantId(), permissionType);
    }

    @Override
    default List<PermissionDO> findAll() {
        return findByTenantIdAndDeletedFalse(TenantContext.tenantId(), Sort.unsorted());
    }

    default List<PermissionDO> findAllEnabled() {
        return findByTenantIdAndStatusAndDeletedFalse(TenantContext.tenantId(), (short) 1, Sort.unsorted());
    }

    default List<PermissionDO> findMenuPermissions() {
        return findByTenantIdAndPermissionTypeInAndStatusAndDeletedFalse(
                TenantContext.tenantId(), List.of((short) 1, (short) 2), (short) 1, Sort.by("sortOrder"));
    }

    default boolean existsByPermissionCode(String permissionCode) {
        return existsByTenantIdAndPermissionCodeAndDeletedFalse(TenantContext.tenantId(), permissionCode);
    }

    Optional<PermissionDO> findByTenantIdAndIdAndDeletedFalse(String tenantId, String id);

    Optional<PermissionDO> findByTenantIdAndPermissionCodeAndDeletedFalse(String tenantId, String permissionCode);

    List<PermissionDO> findByTenantIdAndParentIdAndDeletedFalse(String tenantId, String parentId);

    List<PermissionDO> findByTenantIdAndPermissionTypeAndDeletedFalse(String tenantId, Short permissionType);

    List<PermissionDO> findByTenantIdAndDeletedFalse(String tenantId, Sort sort);

    List<PermissionDO> findByTenantIdAndStatusAndDeletedFalse(String tenantId, Short status, Sort sort);

    List<PermissionDO> findByTenantIdAndPermissionTypeInAndStatusAndDeletedFalse(
            String tenantId, List<Short> permissionTypes, Short status, Sort sort);

    boolean existsByTenantIdAndPermissionCodeAndDeletedFalse(String tenantId, String permissionCode);
}
