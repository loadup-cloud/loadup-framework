package io.github.loadup.modules.notification;

public interface InboxRepository {
    int insert(InboxMessage message, String requestKey);
    InboxPage list(String tenantId, String recipientId, boolean unreadOnly, int page, int size);
    long unreadCount(String tenantId, String recipientId);
    int markRead(String tenantId, String recipientId, String id);
    int markAllRead(String tenantId, String recipientId);
    int archive(String tenantId, String recipientId, String id);
}
