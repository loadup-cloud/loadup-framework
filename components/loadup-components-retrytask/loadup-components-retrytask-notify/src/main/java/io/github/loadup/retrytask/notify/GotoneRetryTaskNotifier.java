package io.github.loadup.retrytask.notify;

/*-
 * #%L
 * Loadup Components Retrytask Notify
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import io.github.loadup.components.gotone.api.NotificationService;
import io.github.loadup.components.gotone.model.NotificationRequest;
import io.github.loadup.retrytask.facade.model.RetryTask;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * Gotone integration for retry task notifications
 * Sends notifications through Gotone component (email/sms/webhook)
 */
@Slf4j
public class GotoneRetryTaskNotifier implements RetryTaskNotifier {

    public static final String TYPE = "gotone";

    private final NotificationService notificationService;
    private final String serviceCode;

    /**
     * @param notificationService Gotone notification service
     * @param serviceCode         Service code for notifications (default: RETRY_TASK_FAILURE)
     */
    public GotoneRetryTaskNotifier(NotificationService notificationService, String serviceCode) {
        this.notificationService = notificationService;
        this.serviceCode = serviceCode != null ? serviceCode : "RETRY_TASK_FAILURE";
    }

    public GotoneRetryTaskNotifier(NotificationService notificationService) {
        this(notificationService, "RETRY_TASK_FAILURE");
    }

    @Override
    public void notify(RetryTask task) {
        try {
            NotificationRequest request = NotificationRequest.builder()
                    .serviceCode(serviceCode)
                    .receivers(List.of("admin@example.com")) // TODO: make configurable
                    .templateParams(Map.of(
                            "bizType", task.getBizType(),
                            "bizId", task.getBizId(),
                            "retryCount", String.valueOf(task.getRetryCount()),
                            "maxRetryCount", String.valueOf(task.getMaxRetryCount()),
                            "failureReason",
                                    task.getLastFailureReason() != null ? task.getLastFailureReason() : "Unknown"))
                    .build();

            notificationService.send(request);

            log.info(
                    ">>> [RETRY-TASK-GOTONE] Sent notification for task: bizType={}, bizId={}",
                    task.getBizType(),
                    task.getBizId());

        } catch (Exception e) {
            log.error(
                    ">>> [RETRY-TASK-GOTONE] Failed to send notification for task: bizType={}, bizId={}",
                    task.getBizType(),
                    task.getBizId(),
                    e);
        }
    }

    @Override
    public String getType() {
        return TYPE;
    }
}
