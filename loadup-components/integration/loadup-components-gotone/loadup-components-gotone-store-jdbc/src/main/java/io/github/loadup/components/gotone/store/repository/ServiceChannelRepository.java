package io.github.loadup.components.gotone.store.repository;

import io.github.loadup.components.database.tenant.TenantContext;
import io.github.loadup.components.gotone.store.dataobject.ServiceChannelRow;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.ListCrudRepository;

/** Spring Data JDBC reads for Gotone channel configuration. */
public interface ServiceChannelRepository extends ListCrudRepository<ServiceChannelRow, String> {

    default List<ServiceChannelRow> findEnabledByServiceCode(String serviceCode, Sort sort) {
        return findByTenantIdAndServiceCodeAndEnabledTrueAndDeletedFalse(TenantContext.tenantId(), serviceCode, sort);
    }

    List<ServiceChannelRow> findByTenantIdAndServiceCodeAndEnabledTrueAndDeletedFalse(
            String tenantId, String serviceCode, Sort sort);
}
