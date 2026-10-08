package io.github.loadup.retrytask.notifier.gotone;

import io.github.loadup.commons.log.LogUtil;
import io.github.loadup.components.gotone.NotificationService;
import io.github.loadup.components.gotone.model.NotificationRequest;
import io.github.loadup.retrytask.facade.RetryTaskNotifier;
import io.github.loadup.retrytask.facade.model.RetryTaskFailure;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link RetryTaskNotifier} that reuses the gotone notification component to alert on permanent
 * retry task failures.
 *
 * <p>The notifier only activates when a gotone {@link NotificationService} bean is present and
 * {@code loadup.retrytask.notify.service-code} is configured. Failure details are passed as
 * template params: {@code bizType}, {@code bizId}, {@code jobId}, {@code attempts} and {@code
 * errorMessage}.
 */
public class GotoneRetryTaskNotifier implements RetryTaskNotifier {

    private final NotificationService notificationService;
    private final RetryTaskNotifyProperties properties;

    public GotoneRetryTaskNotifier(NotificationService notificationService, RetryTaskNotifyProperties properties) {
        this.notificationService = notificationService;
        this.properties = properties;
    }

    @Override
    public void notifyFailed(RetryTaskFailure failure) {
        if (!properties.isEnabled()) {
            LogUtil.debug(GotoneRetryTaskNotifier.class, "Gotone retry notifier skipped: disabled");
            return;
        }
        if (properties.getServiceCode() == null || properties.getServiceCode().isBlank()) {
            LogUtil.debug(
                    GotoneRetryTaskNotifier.class,
                    "Gotone retry notifier skipped: loadup.retrytask.notify.service-code is not configured");
            return;
        }
        List<String> receivers = properties.getReceivers();
        if (receivers == null || receivers.isEmpty()) {
            LogUtil.debug(GotoneRetryTaskNotifier.class, "Gotone retry notifier skipped: no receivers configured");
            return;
        }

        Map<String, Object> params = new LinkedHashMap<>();
        params.put("bizType", failure.bizType());
        params.put("bizId", failure.bizId());
        params.put("jobId", failure.jobId());
        params.put("attempts", failure.attempts());
        params.put("errorMessage", failure.errorMessage());

        NotificationRequest request = NotificationRequest.builder()
                .serviceCode(properties.getServiceCode())
                .receivers(List.copyOf(receivers))
                .templateParams(params)
                .requestId("retry:" + failure.bizType() + ":" + failure.bizId())
                .build();
        try {
            notificationService.send(request);
        } catch (Exception e) {
            LogUtil.warn(
                    GotoneRetryTaskNotifier.class,
                    "Gotone failure notification send failed for bizType={} bizId={}",
                    failure.bizType(),
                    failure.bizId(),
                    e);
        }
    }
}
