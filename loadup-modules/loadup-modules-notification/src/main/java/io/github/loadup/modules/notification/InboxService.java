package io.github.loadup.modules.notification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

/** Delivery to a private, persistent inbox and recipient-owned read operations. */
public class InboxService {
    private static final String DEFAULT_TENANT = "__default__";
    private final InboxRepository repository;

    public InboxService(InboxRepository repository) { this.repository = repository; }

    @Transactional
    public int publish(String tenantId, String senderId, List<String> recipients, String category,
            String title, String body, String actionUrl, String requestKey) {
        String tenant = tenant(tenantId);
        if (recipients == null || recipients.isEmpty() || recipients.size() > 100) {
            throw new IllegalArgumentException("recipients must contain 1 to 100 users");
        }
        String sender = senderId == null || senderId.isBlank() ? null : required(senderId, "senderId", 64);
        String kind = required(category, "category", 64);
        String heading = required(title, "title", 200);
        String text = required(body, "body", 4000);
        String url = actionUrl == null || actionUrl.isBlank() ? null : required(actionUrl, "actionUrl", 500);
        if (url != null && (!url.startsWith("/") || url.startsWith("//")
                || url.indexOf('\\') >= 0 || url.chars().anyMatch(Character::isISOControl))) {
            throw new IllegalArgumentException("actionUrl must be an application-relative path");
        }
        String key = requestKey == null || requestKey.isBlank() ? null : required(requestKey, "requestKey", 128);
        LocalDateTime now = LocalDateTime.now();
        int inserted = 0;
        for (String recipient : recipients.stream().distinct().toList()) {
            InboxMessage message = new InboxMessage(UUID.randomUUID().toString(), tenant,
                    required(recipient, "recipientId", 64), sender, kind, heading, text, url, null, now);
            inserted += repository.insert(message, key);
        }
        return inserted;
    }

    public InboxPage list(String tenantId, String recipientId, boolean unreadOnly, int page, int size) {
        if (page < 1 || size < 1 || size > 100) throw new IllegalArgumentException("invalid page or size");
        return repository.list(tenant(tenantId), required(recipientId, "recipientId", 64), unreadOnly, page, size);
    }

    public long unreadCount(String tenantId, String recipientId) {
        return repository.unreadCount(tenant(tenantId), required(recipientId, "recipientId", 64));
    }

    public void markRead(String tenantId, String recipientId, String id) {
        if (repository.markRead(tenant(tenantId), required(recipientId, "recipientId", 64),
                required(id, "id", 64)) == 0) throw new IllegalArgumentException("notification not found");
    }

    public int markAllRead(String tenantId, String recipientId) {
        return repository.markAllRead(tenant(tenantId), required(recipientId, "recipientId", 64));
    }

    public void archive(String tenantId, String recipientId, String id) {
        if (repository.archive(tenant(tenantId), required(recipientId, "recipientId", 64),
                required(id, "id", 64)) == 0) throw new IllegalArgumentException("notification not found");
    }

    private static String tenant(String value) {
        return value == null || value.isBlank() ? DEFAULT_TENANT : required(value, "tenantId", 64);
    }

    private static String required(String value, String name, int max) {
        if (value == null || value.isBlank() || value.trim().length() > max) {
            throw new IllegalArgumentException(name + " must contain 1 to " + max + " characters");
        }
        return value.trim();
    }
}
