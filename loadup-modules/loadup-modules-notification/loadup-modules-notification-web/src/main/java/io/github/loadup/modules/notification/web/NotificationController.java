/*
 * #%L
 * LoadUp In-App Notifications Web Adapter
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.github.loadup.modules.notification.web;

import io.github.loadup.commons.result.PageResponse;
import io.github.loadup.commons.result.SuccessResponse;
import io.github.loadup.commons.util.TenantUtil;
import io.github.loadup.components.authorization.model.LoadUpUser;
import io.github.loadup.modules.notification.client.command.NotificationArchiveCommand;
import io.github.loadup.modules.notification.client.command.NotificationReadAllCommand;
import io.github.loadup.modules.notification.client.command.NotificationReadCommand;
import io.github.loadup.modules.notification.client.command.NotificationSendCommand;
import io.github.loadup.modules.notification.client.dto.NotificationViewDTO;
import io.github.loadup.modules.notification.client.dto.ReadResultDTO;
import io.github.loadup.modules.notification.client.dto.SendResultDTO;
import io.github.loadup.modules.notification.client.dto.UnreadCountDTO;
import io.github.loadup.modules.notification.client.facade.InboxFacade;
import io.github.loadup.modules.notification.client.query.NotificationPageQuery;
import io.github.loadup.modules.notification.client.query.NotificationUnreadCountQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** A user-owned inbox with a separate administrator publishing entry point. */
@RestController
@RequestMapping("/notifications")
@Tag(name = "In-App Notifications", description = "Private notification inbox")
public class NotificationController {
    private final NotificationWebConverter converter;
    private final InboxFacade inbox;

    public NotificationController(InboxFacade inbox, NotificationWebConverter converter) {
        this.converter = converter;
        this.inbox = inbox;
    }

    @PostMapping("/publish")
    @Operation(summary = "Publish a notification to selected users")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public SuccessResponse<SendResultDTO> publish(
            @RequestBody NotificationSendCommand request, Authentication authentication) {
        int delivered = inbox.publish(
                TenantUtil.getTenantId(),
                actor(authentication),
                request.recipients(),
                request.category(),
                request.title(),
                request.body(),
                request.actionUrl(),
                request.requestKey());
        return SuccessResponse.of(new SendResultDTO(delivered));
    }

    @PostMapping("/list")
    @Operation(summary = "List my notifications")
    @PreAuthorize("isAuthenticated()")
    public PageResponse<NotificationViewDTO> list(
            @RequestBody NotificationPageQuery request, Authentication authentication) {
        var result = inbox.list(
                TenantUtil.getTenantId(),
                actor(authentication),
                Boolean.TRUE.equals(request.unreadOnly()),
                request.page() == null ? 1 : request.page(),
                request.size() == null ? 20 : request.size());
        return PageResponse.of(result.map(converter::toView));
    }

    @PostMapping("/unread-count")
    @Operation(summary = "Count my unread notifications")
    @PreAuthorize("isAuthenticated()")
    public SuccessResponse<UnreadCountDTO> unreadCount(
            @RequestBody NotificationUnreadCountQuery request, Authentication authentication) {
        return SuccessResponse.of(
                new UnreadCountDTO(inbox.unreadCount(TenantUtil.getTenantId(), actor(authentication))));
    }

    @PostMapping("/read")
    @Operation(summary = "Mark one notification as read")
    @PreAuthorize("isAuthenticated()")
    public SuccessResponse<Void> markRead(@RequestBody NotificationReadCommand request, Authentication authentication) {
        inbox.markRead(TenantUtil.getTenantId(), actor(authentication), request.id());
        return SuccessResponse.success();
    }

    @PostMapping("/read-all")
    @Operation(summary = "Mark all my notifications as read")
    @PreAuthorize("isAuthenticated()")
    public SuccessResponse<ReadResultDTO> markAllRead(
            @RequestBody NotificationReadAllCommand request, Authentication authentication) {
        return SuccessResponse.of(
                new ReadResultDTO(inbox.markAllRead(TenantUtil.getTenantId(), actor(authentication))));
    }

    @PostMapping("/archive")
    @Operation(summary = "Archive one notification")
    @PreAuthorize("isAuthenticated()")
    public SuccessResponse<Void> archive(
            @RequestBody NotificationArchiveCommand request, Authentication authentication) {
        inbox.archive(TenantUtil.getTenantId(), actor(authentication), request.id());
        return SuccessResponse.success();
    }

    private static String actor(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof LoadUpUser user)
                || user.getUserId() == null
                || user.getUserId().isBlank()) {
            throw new IllegalArgumentException("authenticated user is required");
        }
        return user.getUserId();
    }
}
