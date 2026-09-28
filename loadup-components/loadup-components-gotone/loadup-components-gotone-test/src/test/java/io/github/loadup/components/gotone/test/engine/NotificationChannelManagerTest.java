package io.github.loadup.components.gotone.test.engine;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.loadup.components.gotone.NotificationChannelProvider;
import io.github.loadup.components.gotone.engine.NotificationChannelManager;
import io.github.loadup.components.gotone.engine.NotificationChannelManager.SendResult;
import io.github.loadup.components.gotone.model.ChannelSendRequest;
import io.github.loadup.components.gotone.model.ChannelSendResponse;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class NotificationChannelManagerTest {

    private static final ChannelSendRequest REQUEST =
            new ChannelSendRequest(List.of("receiver-1"), "content", Map.of(), Map.of());

    @Test
    void fallsBackToNextProviderWhenPrimaryFails() {
        NotificationChannelManager manager = new NotificationChannelManager(
                List.of(new StubProvider("EMAIL", "smtp", false), new StubProvider("EMAIL", "fallback", true)));

        SendResult result = manager.sendWithFallback("EMAIL", "smtp", List.of("fallback"), REQUEST);

        assertThat(result.success()).isTrue();
        assertThat(result.getSuccessfulProvider()).isEqualTo("fallback");
        assertThat(result.attempts()).hasSize(2);
    }

    @Test
    void skipsUnavailableProviders() {
        NotificationChannelManager manager = new NotificationChannelManager(
                List.of(new StubProvider("EMAIL", "smtp", true, false), new StubProvider("EMAIL", "fallback", true)));

        SendResult result = manager.sendWithFallback("EMAIL", "smtp", List.of("fallback"), REQUEST);

        assertThat(result.success()).isTrue();
        assertThat(result.getSuccessfulProvider()).isEqualTo("fallback");
        assertThat(result.attempts()).hasSize(1);
    }

    @Test
    void unknownChannelFailsWithoutAttempts() {
        NotificationChannelManager manager =
                new NotificationChannelManager(List.of(new StubProvider("EMAIL", "smtp", true)));

        SendResult result = manager.sendWithFallback("SMS", "aliyun", List.of(), REQUEST);

        assertThat(result.success()).isFalse();
        assertThat(result.attempts()).isEmpty();
    }

    private static final class StubProvider implements NotificationChannelProvider {
        private final String channelType;
        private final String providerName;
        private final boolean succeed;
        private final boolean available;

        private StubProvider(String channelType, String providerName, boolean succeed) {
            this(channelType, providerName, succeed, true);
        }

        private StubProvider(String channelType, String providerName, boolean succeed, boolean available) {
            this.channelType = channelType;
            this.providerName = providerName;
            this.succeed = succeed;
            this.available = available;
        }

        @Override
        public String getChannelType() {
            return channelType;
        }

        @Override
        public String getProviderName() {
            return providerName;
        }

        @Override
        public ChannelSendResponse send(ChannelSendRequest request) {
            if (!succeed) {
                throw new IllegalStateException("provider failed");
            }
            return new ChannelSendResponse(request.content(), 1, 0, Map.of("receiver-1", true), Map.of());
        }

        @Override
        public boolean isAvailable() {
            return available;
        }
    }
}
