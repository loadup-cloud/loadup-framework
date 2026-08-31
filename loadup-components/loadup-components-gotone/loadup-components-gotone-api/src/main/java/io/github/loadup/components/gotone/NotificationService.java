/*-
 * #%L
 * Loadup Gotone API
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
