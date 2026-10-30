package io.github.loadup.components.gotone.store.repository;

import io.github.loadup.components.database.tenant.TenantContext;
import io.github.loadup.components.gotone.store.dataobject.NotificationServiceRow;
import java.util.Optional;
import org.springframework.data.repository.ListCrudRepository;

/** Spring Data JDBC reads for Gotone notification service configuration. */
public interface NotificationServiceRepository extends ListCrudRepository<NotificationServiceRow, String> {

    default Optional<NotificationServiceRow> findByServiceCode(String serviceCode) {
        return findByTenantIdAndServiceCodeAndEnabledTrueAndDeletedFalse(TenantContext.tenantId(), serviceCode);
    }

    Optional<NotificationServiceRow> findByTenantIdAndServiceCodeAndEnabledTrueAndDeletedFalse(
            String tenantId, String serviceCode);
}
