package io.github.loadup.components.gotone;

import io.github.loadup.components.gotone.model.NotificationRequest;
import io.github.loadup.components.gotone.model.NotificationResponse;

/**
 * Unified notification facade.
 *
 * <p>Sends are driven by {@code serviceCode}: the engine resolves channel configuration through
 * the configured {@code ChannelConfigProvider} and routes to the matching channel providers.
 * Business code never touches a concrete channel SDK.
 */
public interface NotificationService {

    /**
     * Sends a notification synchronously and returns the per-channel results.
     *
     * @param request the notification request
     * @return the aggregated send result
     */
    NotificationResponse send(NotificationRequest request);

    /**
     * Sends a notification asynchronously on the configured task executor.
     *
     * @param request the notification request
     */
    void sendAsync(NotificationRequest request);
}
