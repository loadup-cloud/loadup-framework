package io.github.loadup.modules.dictionary.infrastructure.repository;

import io.github.loadup.components.database.tenant.TenantContext;
import io.github.loadup.modules.dictionary.infrastructure.dataobject.DictionaryTypeDO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.ListCrudRepository;

/** Spring Data JDBC reads for dictionary types. Tenant identity comes from the execution context. */
public interface DictionaryTypeRepository extends ListCrudRepository<DictionaryTypeDO, String> {

    default Optional<DictionaryTypeDO> findTypeById(String id) {
        return findByTenantIdAndIdAndDeletedFalse(TenantContext.tenantId(), id);
    }

    default Optional<DictionaryTypeDO> findTypeByCode(String code) {
        return findByTenantIdAndCodeAndDeletedFalse(TenantContext.tenantId(), code);
    }

    default Page<DictionaryTypeDO> listTypes(Pageable pageable) {
        return findByTenantIdAndDeletedFalse(TenantContext.tenantId(), pageable);
    }

    Optional<DictionaryTypeDO> findByTenantIdAndIdAndDeletedFalse(String tenantId, String id);

    Optional<DictionaryTypeDO> findByTenantIdAndCodeAndDeletedFalse(String tenantId, String code);

    Page<DictionaryTypeDO> findByTenantIdAndDeletedFalse(String tenantId, Pageable pageable);
}
