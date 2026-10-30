package io.github.loadup.modules.dictionary.infrastructure.repository;

import io.github.loadup.components.database.tenant.TenantContext;
import io.github.loadup.modules.dictionary.infrastructure.dataobject.DictionaryItemDO;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.ListCrudRepository;

/** Spring Data JDBC reads for dictionary items. Tenant identity comes from the execution context. */
public interface DictionaryItemRepository extends ListCrudRepository<DictionaryItemDO, String> {

    default Optional<DictionaryItemDO> findItemById(String id) {
        return findByTenantIdAndIdAndDeletedFalse(TenantContext.tenantId(), id);
    }

    default Page<DictionaryItemDO> listItems(String typeId, Pageable pageable) {
        return findByTenantIdAndTypeIdAndDeletedFalse(TenantContext.tenantId(), typeId, pageable);
    }

    default List<DictionaryItemDO> listEnabledItems(String typeId, Sort sort) {
        return findByTenantIdAndTypeIdAndEnabledTrueAndDeletedFalse(TenantContext.tenantId(), typeId, sort);
    }

    default long countItems(String typeId) {
        return countByTenantIdAndTypeIdAndDeletedFalse(TenantContext.tenantId(), typeId);
    }

    Optional<DictionaryItemDO> findByTenantIdAndIdAndDeletedFalse(String tenantId, String id);

    Page<DictionaryItemDO> findByTenantIdAndTypeIdAndDeletedFalse(String tenantId, String typeId, Pageable pageable);

    List<DictionaryItemDO> findByTenantIdAndTypeIdAndEnabledTrueAndDeletedFalse(
            String tenantId, String typeId, Sort sort);

    long countByTenantIdAndTypeIdAndDeletedFalse(String tenantId, String typeId);
}
