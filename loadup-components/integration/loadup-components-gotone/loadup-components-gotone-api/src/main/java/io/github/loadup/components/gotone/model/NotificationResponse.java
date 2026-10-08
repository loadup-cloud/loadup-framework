package io.github.loadup.components.gotone.model;

import java.util.List;

/**
 * Aggregated notification send response.
 */
public record NotificationResponse(
        String traceId,
        String serviceCode,
        int totalReceivers,
        List<ChannelSendResult> channelResults,
        boolean success,
        String errorMessage) {

    /**
     * Per-channel send result.
     */
    public record ChannelSendResult(
            String channel,
            String provider,
            int totalReceivers,
            int successCount,
            int failedCount,
            boolean success,
            String errorMessage) {}
}
