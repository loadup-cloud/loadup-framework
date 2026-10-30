package io.github.loadup.modules.notification.infrastructure.repository;

import io.github.loadup.components.database.tenant.TenantContext;
import io.github.loadup.modules.notification.infrastructure.dataobject.InboxMessageDO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.ListCrudRepository;

/** Spring Data JDBC reads for inbox messages. State transitions stay on conditional SQL. */
public interface InboxMessageRepository extends ListCrudRepository<InboxMessageDO, String> {

    default Page<InboxMessageDO> list(String recipientId, boolean unread, Pageable pageable) {
        return unread
                ? findByTenantIdAndRecipientIdAndArchivedAtIsNullAndReadAtIsNullAndDeletedFalse(
                        TenantContext.tenantId(), recipientId, pageable)
                : findByTenantIdAndRecipientIdAndArchivedAtIsNullAndDeletedFalse(
                        TenantContext.tenantId(), recipientId, pageable);
    }

    default long unreadCount(String recipientId) {
        return countByTenantIdAndRecipientIdAndArchivedAtIsNullAndReadAtIsNullAndDeletedFalse(
                TenantContext.tenantId(), recipientId);
    }

    Page<InboxMessageDO> findByTenantIdAndRecipientIdAndArchivedAtIsNullAndDeletedFalse(
            String tenantId, String recipientId, Pageable pageable);

    Page<InboxMessageDO> findByTenantIdAndRecipientIdAndArchivedAtIsNullAndReadAtIsNullAndDeletedFalse(
            String tenantId, String recipientId, Pageable pageable);

    long countByTenantIdAndRecipientIdAndArchivedAtIsNullAndReadAtIsNullAndDeletedFalse(
            String tenantId, String recipientId);
}
