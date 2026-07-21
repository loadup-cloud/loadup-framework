package io.github.loadup.components.gotone.record;

import io.github.loadup.components.gotone.config.ChannelConfigProvider.ChannelConfig;
import io.github.loadup.components.gotone.model.NotificationRequest;

public interface RecordHandler {
    void onResult(NotificationRequest request, ChannelConfig config, SendAttemptResult result);

    record SendAttemptResult(
            String actualProvider,
            boolean success,
            int successCount,
            int failedCount,
            java.util.List<Attempt> attempts) {}

    record Attempt(String providerName, boolean success, String error) {}
}
