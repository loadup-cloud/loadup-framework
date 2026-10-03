package io.github.loadup.modules.notification;

import java.time.LocalDateTime;

/** One recipient's copy of an in-app notification. */
public record InboxMessage(String id, String tenantId, String recipientId, String senderId,
        String category, String title, String body, String actionUrl, LocalDateTime readAt,
        LocalDateTime createdAt) {}
