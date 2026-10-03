package io.github.loadup.modules.notification.web;

import io.github.loadup.commons.result.PageDTO;
import io.github.loadup.commons.result.SuccessResponse;
import io.github.loadup.commons.util.TenantUtil;
import io.github.loadup.components.authorization.model.LoadUpUser;
import io.github.loadup.modules.notification.InboxMessage;
import io.github.loadup.modules.notification.InboxService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** A user-owned inbox with a separate administrator publishing entry point. */
@RestController
@RequestMapping("/api/notifications")
@Tag(name = "In-App Notifications", description = "Private notification inbox")
public class NotificationController {
    private final InboxService inbox;

    public NotificationController(InboxService inbox) { this.inbox = inbox; }

    @PostMapping("/publish")
    @Operation(summary = "Publish a notification to selected users")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public SendResult publish(@RequestBody SendRequest request, Authentication authentication) {
        int delivered = inbox.publish(TenantUtil.getTenantId(), actor(authentication), request.recipients(),
                request.category(), request.title(), request.body(), request.actionUrl(), request.requestKey());
        return new SendResult(delivered);
    }

    @PostMapping("/list")
    @Operation(summary = "List my notifications")
    @PreAuthorize("isAuthenticated()")
    public PageDTO<NotificationView> list(@RequestBody ListRequest request, Authentication authentication) {
        var result = inbox.list(TenantUtil.getTenantId(), actor(authentication),
                Boolean.TRUE.equals(request.unreadOnly()), request.page() == null ? 1 : request.page(),
                request.size() == null ? 20 : request.size());
        return PageDTO.of(result.records().stream().map(NotificationView::of).toList(),
                result.total(), result.page(), result.size());
    }

    @PostMapping("/unread-count")
    @Operation(summary = "Count my unread notifications")
    @PreAuthorize("isAuthenticated()")
    public UnreadCount unreadCount(@RequestBody EmptyRequest request, Authentication authentication) {
        return new UnreadCount(inbox.unreadCount(TenantUtil.getTenantId(), actor(authentication)));
    }

    @PostMapping("/read")
    @Operation(summary = "Mark one notification as read")
    @PreAuthorize("isAuthenticated()")
    public SuccessResponse<Void> markRead(@RequestBody IdRequest request, Authentication authentication) {
        inbox.markRead(TenantUtil.getTenantId(), actor(authentication), request.id());
        return SuccessResponse.success();
    }

    @PostMapping("/read-all")
    @Operation(summary = "Mark all my notifications as read")
    @PreAuthorize("isAuthenticated()")
    public ReadResult markAllRead(@RequestBody EmptyRequest request, Authentication authentication) {
        return new ReadResult(inbox.markAllRead(TenantUtil.getTenantId(), actor(authentication)));
    }

    @PostMapping("/archive")
    @Operation(summary = "Archive one notification")
    @PreAuthorize("isAuthenticated()")
    public SuccessResponse<Void> archive(@RequestBody IdRequest request, Authentication authentication) {
        inbox.archive(TenantUtil.getTenantId(), actor(authentication), request.id());
        return SuccessResponse.success();
    }

    private static String actor(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof LoadUpUser user)
                || user.getUserId() == null || user.getUserId().isBlank()) {
            throw new IllegalArgumentException("authenticated user is required");
        }
        return user.getUserId();
    }

    public record SendRequest(List<String> recipients, String category, String title, String body,
            String actionUrl, String requestKey) {}
    public record EmptyRequest() {}
    public record IdRequest(String id) {}
    public record ListRequest(Boolean unreadOnly, Integer page, Integer size) {}
    public record SendResult(int delivered) {}
    public record UnreadCount(long count) {}
    public record ReadResult(int updated) {}
    public record NotificationView(String id, String senderId, String category, String title, String body,
            String actionUrl, LocalDateTime readAt, LocalDateTime createdAt) {
        static NotificationView of(InboxMessage message) {
            return new NotificationView(message.id(), message.senderId(), message.category(), message.title(),
                    message.body(), message.actionUrl(), message.readAt(), message.createdAt());
        }
    }
}
