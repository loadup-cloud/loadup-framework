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

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Notification send request (serviceCode driven).
 *
 * <p>Business code submits a service code plus receivers and template params; the engine resolves
 * channels and templates from the configured {@code ChannelConfigProvider}.
 */
public record NotificationRequest(
        String serviceCode,
        List<String> receivers,
        Map<String, Object> templateParams,
        String requestId,
        List<String> channels,
        Boolean async) {

    public NotificationRequest {
        Objects.requireNonNull(serviceCode, "serviceCode must not be null");
        receivers = receivers == null ? List.of() : List.copyOf(receivers);
        templateParams = templateParams == null ? Map.of() : Map.copyOf(templateParams);
        channels = channels == null ? List.of() : List.copyOf(channels);
        if (receivers.isEmpty()) {
            throw new IllegalArgumentException("receivers must not be empty");
        }
    }

    /**
     * Creates a request with only the mandatory fields.
     *
     * @param serviceCode the service code
     * @param receivers the receivers
     * @param templateParams the template params
     * @return the request
     */
    public static NotificationRequest of(
            String serviceCode, List<String> receivers, Map<String, Object> templateParams) {
        return new NotificationRequest(serviceCode, receivers, templateParams, null, null, null);
    }

    public static Builder builder() {
        return new Builder();
    }

    /** Fluent builder for {@link NotificationRequest}. */
    public static final class Builder {
        private String serviceCode;
        private List<String> receivers;
        private Map<String, Object> templateParams;
        private String requestId;
        private List<String> channels;
        private Boolean async;

        public Builder serviceCode(String serviceCode) {
            this.serviceCode = serviceCode;
            return this;
        }

        public Builder receivers(List<String> receivers) {
            this.receivers = receivers;
            return this;
        }

        public Builder templateParams(Map<String, Object> templateParams) {
            this.templateParams = templateParams;
            return this;
        }

        public Builder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public Builder channels(List<String> channels) {
            this.channels = channels;
            return this;
        }

        public Builder async(Boolean async) {
            this.async = async;
            return this;
        }

        public NotificationRequest build() {
            return new NotificationRequest(serviceCode, receivers, templateParams, requestId, channels, async);
        }
    }
}
