package io.github.loadup.components.gotone.model;

import java.util.Map;

/**
 * Channel-level send response returned by {@link
 * io.github.loadup.components.gotone.NotificationChannelProvider}.
 */
public record ChannelSendResponse(
        String content,
        int successCount,
        int failedCount,
        Map<String, Boolean> receiverStatus,
        Map<String, String> receiverErrors) {

    public ChannelSendResponse {
        receiverStatus = receiverStatus == null ? Map.of() : Map.copyOf(receiverStatus);
        receiverErrors = receiverErrors == null ? Map.of() : Map.copyOf(receiverErrors);
    }

    /**
     * Returns whether the given receiver was sent successfully.
     *
     * @param receiver the receiver
     * @return {@code true} when the receiver succeeded
     */
    public boolean isSuccess(String receiver) {
        return Boolean.TRUE.equals(receiverStatus.get(receiver));
    }

    /**
     * Returns the error message for the given receiver, if any.
     *
     * @param receiver the receiver
     * @return the error message, or {@code null}
     */
    public String getErrorMessage(String receiver) {
        return receiverErrors.get(receiver);
    }

    /**
     * Returns whether at least one receiver was sent successfully.
     *
     * @return {@code true} when any receiver succeeded
     */
    public boolean isSuccess() {
        return successCount > 0;
    }
}
