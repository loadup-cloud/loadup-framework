/*-
 * #%L
 * Loadup Components Retrytask Notifier Gotone
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
package io.github.loadup.retrytask.notifier.gotone;

import io.github.loadup.components.gotone.NotificationService;
import io.github.loadup.components.gotone.model.NotificationRequest;
import io.github.loadup.retrytask.facade.RetryTaskNotifier;
import io.github.loadup.retrytask.facade.model.RetryTaskFailure;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger log = LoggerFactory.getLogger(GotoneRetryTaskNotifier.class);

    private final NotificationService notificationService;
    private final RetryTaskNotifyProperties properties;

    public GotoneRetryTaskNotifier(NotificationService notificationService, RetryTaskNotifyProperties properties) {
        this.notificationService = notificationService;
        this.properties = properties;
    }

    @Override
    public void notifyFailed(RetryTaskFailure failure) {
        if (!properties.isEnabled()) {
            log.debug("Gotone retry notifier skipped: disabled");
            return;
        }
        if (properties.getServiceCode() == null || properties.getServiceCode().isBlank()) {
            log.debug("Gotone retry notifier skipped: loadup.retrytask.notify.service-code is not configured");
            return;
        }
        List<String> receivers = properties.getReceivers();
        if (receivers == null || receivers.isEmpty()) {
            log.debug("Gotone retry notifier skipped: no receivers configured");
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
            log.warn(
                    "Gotone failure notification send failed for bizType={} bizId={}",
                    failure.bizType(),
                    failure.bizId(),
                    e);
        }
    }
}
