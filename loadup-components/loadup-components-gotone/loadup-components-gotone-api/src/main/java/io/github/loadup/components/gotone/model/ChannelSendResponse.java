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
