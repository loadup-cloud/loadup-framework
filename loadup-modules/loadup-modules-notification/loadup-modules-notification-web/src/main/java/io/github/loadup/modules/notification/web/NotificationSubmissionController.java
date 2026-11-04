/*
 * Copyright (C) 2026 LoadUp Cloud
 *
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
 */
package io.github.loadup.modules.notification.web;

import io.github.loadup.commons.error.CommonException;
import io.github.loadup.commons.result.SuccessResponse;
import io.github.loadup.modules.notification.client.command.NotificationSubmitCommand;
import io.github.loadup.modules.notification.client.dto.NotificationAcceptedDTO;
import io.github.loadup.modules.notification.client.error.NotificationResultCodeEnum;
import io.github.loadup.modules.notification.client.facade.NotificationFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal/v1/notifications")
@Tag(name = "Notification Submission", description = "Durable service-to-service notification acceptance")
public class NotificationSubmissionController {
    private final NotificationFacade notifications;

    public NotificationSubmissionController(NotificationFacade notifications) {
        this.notifications = notifications;
    }

    @PostMapping("/submit")
    @Operation(summary = "Accept an in-app notification; success means persisted, not delivered")
    @PreAuthorize("hasAuthority('SCOPE_notification:submit:in-app')")
    public SuccessResponse<NotificationAcceptedDTO> submit(
            @RequestBody NotificationSubmitCommand command, Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getCredentials() instanceof Jwt jwt)) {
            throw new CommonException(NotificationResultCodeEnum.SERVICE_IDENTITY_REQUIRED);
        }
        String source = jwt.getClaimAsString("service_id");
        if (!"SERVICE".equals(jwt.getClaimAsString("principal_type"))
                || source == null
                || source.isBlank()
                || !source.equals(jwt.getSubject())) {
            throw new CommonException(NotificationResultCodeEnum.SERVICE_IDENTITY_REQUIRED);
        }
        return SuccessResponse.of(notifications.submit(source, command));
    }
}
