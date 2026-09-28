package io.github.loadup.components.gotone.record;

import io.github.loadup.components.gotone.config.ChannelConfigProvider.ChannelConfig;
import io.github.loadup.components.gotone.model.NotificationRequest;
import java.util.List;
import java.util.Map;

/**
 * Optional storage SPI that receives the outcome of every channel send attempt.
 *
 * <p>The default JDBC store implements this SPI to persist one notification record per receiver.
 */
public interface RecordHandler {

    /**
     * Records the outcome of one channel send.
     *
     * @param request the original notification request
     * @param config the channel configuration used
     * @param result the aggregated attempt result
     */
    void onResult(NotificationRequest request, ChannelConfig config, SendAttemptResult result);

    /**
     * Aggregated outcome of a channel send, including the fallback chain.
     */
    record SendAttemptResult(
            String actualProvider,
            boolean success,
            int successCount,
            int failedCount,
            String content,
            Map<String, Boolean> receiverStatus,
            Map<String, String> receiverErrors,
            List<Attempt> attempts) {

        public SendAttemptResult {
            content = content == null ? "" : content;
            receiverStatus = receiverStatus == null ? Map.of() : Map.copyOf(receiverStatus);
            receiverErrors = receiverErrors == null ? Map.of() : Map.copyOf(receiverErrors);
            attempts = attempts == null ? List.of() : List.copyOf(attempts);
        }
    }

    /**
     * One attempt in the fallback chain.
     */
    record Attempt(String providerName, boolean success, String error) {}
}
