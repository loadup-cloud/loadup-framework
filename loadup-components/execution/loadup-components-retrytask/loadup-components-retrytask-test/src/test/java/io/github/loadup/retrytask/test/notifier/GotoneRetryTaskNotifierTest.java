package io.github.loadup.retrytask.test.notifier;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.loadup.components.gotone.NotificationService;
import io.github.loadup.components.gotone.model.NotificationRequest;
import io.github.loadup.components.gotone.model.NotificationResponse;
import io.github.loadup.retrytask.facade.model.RetryTaskFailure;
import io.github.loadup.retrytask.notifier.gotone.GotoneRetryTaskNotifier;
import io.github.loadup.retrytask.notifier.gotone.RetryTaskNotifyProperties;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class GotoneRetryTaskNotifierTest {

    @Test
    void sendsFailureViaGotoneWithTemplateParams() {
        CapturingNotificationService notificationService = new CapturingNotificationService();
        RetryTaskNotifyProperties properties = new RetryTaskNotifyProperties();
        properties.setServiceCode("RETRY_TASK_FAILED");
        properties.setReceivers(List.of("ops@example.com"));
        GotoneRetryTaskNotifier notifier = new GotoneRetryTaskNotifier(notificationService, properties);
        RetryTaskFailure failure = new RetryTaskFailure("order-sync", "order-1", UUID.randomUUID(), 3, "boom");

        notifier.notifyFailed(failure);

        assertThat(notificationService.captured).isNotNull();
        assertThat(notificationService.captured.serviceCode()).isEqualTo("RETRY_TASK_FAILED");
        assertThat(notificationService.captured.receivers()).containsExactly("ops@example.com");
        assertThat(notificationService.captured.templateParams())
                .containsEntry("bizType", "order-sync")
                .containsEntry("bizId", "order-1")
                .containsEntry("attempts", 3)
                .containsEntry("errorMessage", "boom");
    }

    @Test
    void skipsWhenServiceCodeIsNotConfigured() {
        CapturingNotificationService notificationService = new CapturingNotificationService();
        RetryTaskNotifyProperties properties = new RetryTaskNotifyProperties();
        properties.setReceivers(List.of("ops@example.com"));
        GotoneRetryTaskNotifier notifier = new GotoneRetryTaskNotifier(notificationService, properties);

        notifier.notifyFailed(new RetryTaskFailure("order-sync", "order-1", UUID.randomUUID(), 1, "boom"));

        assertThat(notificationService.captured).isNull();
    }

    @Test
    void skipsWhenDisabled() {
        CapturingNotificationService notificationService = new CapturingNotificationService();
        RetryTaskNotifyProperties properties = new RetryTaskNotifyProperties();
        properties.setEnabled(false);
        properties.setServiceCode("RETRY_TASK_FAILED");
        properties.setReceivers(List.of("ops@example.com"));
        GotoneRetryTaskNotifier notifier = new GotoneRetryTaskNotifier(notificationService, properties);

        notifier.notifyFailed(new RetryTaskFailure("order-sync", "order-1", UUID.randomUUID(), 1, "boom"));

        assertThat(notificationService.captured).isNull();
    }

    private static final class CapturingNotificationService implements NotificationService {
        private NotificationRequest captured;

        @Override
        public NotificationResponse send(NotificationRequest request) {
            this.captured = request;
            return new NotificationResponse(
                    "trace", request.serviceCode(), request.receivers().size(), List.of(), true, null);
        }

        @Override
        public void sendAsync(NotificationRequest request) {
            this.captured = request;
        }
    }
}
