/*-
 * #%L
 * Loadup Gotone Test
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
package io.github.loadup.components.gotone.test.engine;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.loadup.components.gotone.NotificationChannelProvider;
import io.github.loadup.components.gotone.NotificationService;
import io.github.loadup.components.gotone.config.ChannelConfigProvider;
import io.github.loadup.components.gotone.config.ChannelConfigProvider.ChannelConfig;
import io.github.loadup.components.gotone.engine.DefaultNotificationService;
import io.github.loadup.components.gotone.engine.NotificationChannelManager;
import io.github.loadup.components.gotone.engine.SimpleTemplateRenderer;
import io.github.loadup.components.gotone.model.ChannelSendRequest;
import io.github.loadup.components.gotone.model.ChannelSendResponse;
import io.github.loadup.components.gotone.model.NotificationRequest;
import io.github.loadup.components.gotone.model.NotificationResponse;
import io.github.loadup.components.gotone.record.RecordHandler;
import io.github.loadup.components.gotone.record.RecordHandler.SendAttemptResult;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.core.task.TaskExecutor;

class DefaultNotificationServiceTest {

    private static final TaskExecutor SYNC_EXECUTOR = Runnable::run;

    @Test
    void sendsThroughResolvedChannelConfig() {
        ChannelConfigProvider configProvider = serviceCode ->
                List.of(new ChannelConfig("EMAIL", "smtp", List.of("fallback"), "Hello ${name}", Map.of()));
        CapturingRecordHandler recordHandler = new CapturingRecordHandler();
        NotificationService service = service(configProvider, recordHandler);

        NotificationResponse response = service.send(
                NotificationRequest.of("ORDER_CREATED", List.of("a@example.com"), Map.of("name", "Alice")));

        assertThat(response.success()).isTrue();
        assertThat(response.channelResults()).hasSize(1);
        assertThat(response.channelResults().get(0).channel()).isEqualTo("EMAIL");
        assertThat(response.channelResults().get(0).provider()).isEqualTo("smtp");
        assertThat(recordHandler.captured().actualProvider()).isEqualTo("smtp");
        assertThat(recordHandler.captured().content()).isEqualTo("Hello Alice");
    }

    @Test
    void directChannelModeWorksWithoutConfigProvider() {
        NotificationService service = service(null, null);

        NotificationResponse response = service.send(NotificationRequest.builder()
                .serviceCode("DIRECT")
                .receivers(List.of("receiver-1"))
                .channels(List.of("EMAIL"))
                .build());

        assertThat(response.success()).isTrue();
        assertThat(response.channelResults().get(0).provider()).isEqualTo("smtp");
    }

    @Test
    void reportsFailureWhenAllChannelsFail() {
        ChannelConfigProvider configProvider =
                serviceCode -> List.of(new ChannelConfig("EMAIL", "smtp", List.of(), null, Map.of()));
        NotificationChannelManager channelManager =
                new NotificationChannelManager(List.of(alwaysFailingEmailProvider()));
        NotificationService service = new DefaultNotificationService(
                channelManager,
                Optional.of(configProvider),
                Optional.of(new SimpleTemplateRenderer()),
                Optional.empty(),
                Optional.of(SYNC_EXECUTOR));

        NotificationResponse response =
                service.send(NotificationRequest.of("ORDER_CREATED", List.of("a@example.com"), Map.of()));

        assertThat(response.success()).isFalse();
        assertThat(response.channelResults().get(0).success()).isFalse();
    }

    @Test
    void noResolvedChannelReturnsFailureWithMessage() {
        NotificationService service = service(serviceCode -> List.of(), null);

        NotificationResponse response =
                service.send(NotificationRequest.of("UNKNOWN", List.of("a@example.com"), Map.of()));

        assertThat(response.success()).isFalse();
        assertThat(response.errorMessage()).contains("no channel configuration");
    }

    @Test
    void sendAsyncSubmitsToTheConfiguredExecutor() {
        AtomicReference<Runnable> submitted = new AtomicReference<>();
        TaskExecutor capturingExecutor = submitted::set;
        ChannelConfigProvider configProvider =
                serviceCode -> List.of(new ChannelConfig("EMAIL", "smtp", List.of(), null, Map.of()));
        NotificationChannelManager channelManager =
                new NotificationChannelManager(List.of(alwaysSucceedingEmailProvider()));
        NotificationService service = new DefaultNotificationService(
                channelManager,
                Optional.of(configProvider),
                Optional.of(new SimpleTemplateRenderer()),
                Optional.empty(),
                Optional.of(capturingExecutor));

        service.sendAsync(NotificationRequest.of("ORDER_CREATED", List.of("a@example.com"), Map.of()));

        assertThat(submitted.get()).isNotNull();
    }

    private NotificationService service(ChannelConfigProvider configProvider, RecordHandler recordHandler) {
        NotificationChannelProvider smtp = alwaysSucceedingEmailProvider();
        NotificationChannelManager channelManager = new NotificationChannelManager(List.of(smtp));
        return new DefaultNotificationService(
                channelManager,
                Optional.ofNullable(configProvider),
                Optional.of(new SimpleTemplateRenderer()),
                Optional.ofNullable(recordHandler),
                Optional.of(SYNC_EXECUTOR));
    }

    private static NotificationChannelProvider alwaysSucceedingEmailProvider() {
        return new NotificationChannelProvider() {
            @Override
            public String getChannelType() {
                return "EMAIL";
            }

            @Override
            public String getProviderName() {
                return "smtp";
            }

            @Override
            public ChannelSendResponse send(ChannelSendRequest request) {
                return new ChannelSendResponse(request.content(), 1, 0, Map.of("a@example.com", true), Map.of());
            }

            @Override
            public boolean isAvailable() {
                return true;
            }
        };
    }

    private static NotificationChannelProvider alwaysFailingEmailProvider() {
        return new NotificationChannelProvider() {
            @Override
            public String getChannelType() {
                return "EMAIL";
            }

            @Override
            public String getProviderName() {
                return "smtp";
            }

            @Override
            public ChannelSendResponse send(ChannelSendRequest request) {
                throw new IllegalStateException("smtp down");
            }

            @Override
            public boolean isAvailable() {
                return true;
            }
        };
    }

    private static final class CapturingRecordHandler implements RecordHandler {
        private SendAttemptResult captured;

        @Override
        public void onResult(NotificationRequest request, ChannelConfig config, SendAttemptResult result) {
            this.captured = result;
        }

        SendAttemptResult captured() {
            return captured;
        }
    }
}
