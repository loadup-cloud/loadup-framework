package io.github.loadup.retrytask.notify;

import io.github.loadup.components.gotone.api.NotificationService;
import io.github.loadup.components.gotone.model.NotificationRequest;
import io.github.loadup.retrytask.facade.model.RetryTask;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

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
                    "failureReason", task.getLastFailureReason() != null ? task.getLastFailureReason() : "Unknown"
                ))
                .build();

            notificationService.send(request);

            log.info(">>> [RETRY-TASK-GOTONE] Sent notification for task: bizType={}, bizId={}",
                task.getBizType(), task.getBizId());

        } catch (Exception e) {
            log.error(">>> [RETRY-TASK-GOTONE] Failed to send notification for task: bizType={}, bizId={}",
                task.getBizType(), task.getBizId(), e);
        }
    }

    @Override
    public String getType() {
        return TYPE;
    }
}

