package io.github.loadup.modules.contract.infrastructure.repository;

import io.github.loadup.components.database.tenant.TenantContext;
import io.github.loadup.modules.contract.infrastructure.dataobject.CatalogVersionRow;
import java.util.Optional;
import org.springframework.data.repository.ListCrudRepository;

/** Spring Data JDBC reads for catalog versions. Row locks stay on explicit SQL. */
public interface CatalogVersionRepository extends ListCrudRepository<CatalogVersionRow, String> {

    default Optional<CatalogVersionRow> find(String id) {
        return findByTenantIdAndIdAndDeletedFalse(TenantContext.tenantId(), id);
    }

    Optional<CatalogVersionRow> findByTenantIdAndIdAndDeletedFalse(String tenantId, String id);
}
