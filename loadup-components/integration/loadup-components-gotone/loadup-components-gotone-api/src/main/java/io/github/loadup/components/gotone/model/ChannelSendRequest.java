package io.github.loadup.components.gotone.model;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Channel-level send request passed to {@link
 * io.github.loadup.components.gotone.NotificationChannelProvider}.
 */
public record ChannelSendRequest(
        List<String> receivers, String content, Map<String, Object> channelConfig, Map<String, Object> templateParams) {

    public ChannelSendRequest {
        receivers = receivers == null ? List.of() : List.copyOf(receivers);
        channelConfig = channelConfig == null ? Map.of() : Map.copyOf(channelConfig);
        templateParams = templateParams == null ? Map.of() : Map.copyOf(templateParams);
        Objects.requireNonNull(content, "content must not be null");
    }

    public static Builder builder() {
        return new Builder();
    }

    /** Fluent builder for {@link ChannelSendRequest}. */
    public static final class Builder {
        private List<String> receivers;
        private String content;
        private Map<String, Object> channelConfig;
        private Map<String, Object> templateParams;

        public Builder receivers(List<String> receivers) {
            this.receivers = receivers;
            return this;
        }

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder channelConfig(Map<String, Object> channelConfig) {
            this.channelConfig = channelConfig;
            return this;
        }

        public Builder templateParams(Map<String, Object> templateParams) {
            this.templateParams = templateParams;
            return this;
        }

        public ChannelSendRequest build() {
            return new ChannelSendRequest(receivers, content, channelConfig, templateParams);
        }
    }
}
