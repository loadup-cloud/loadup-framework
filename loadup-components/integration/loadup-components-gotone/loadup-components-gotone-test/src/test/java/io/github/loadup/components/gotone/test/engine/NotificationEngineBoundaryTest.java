/*
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import io.github.loadup.components.gotone.NotificationChannelProvider;
import io.github.loadup.components.gotone.NotificationService;
import io.github.loadup.components.gotone.config.ChannelConfigProvider;
import io.github.loadup.components.gotone.config.ChannelConfigProvider.ChannelConfig;
import io.github.loadup.components.gotone.engine.DefaultNotificationService;
import io.github.loadup.components.gotone.engine.GotoneEngineAutoConfiguration;
import io.github.loadup.components.gotone.engine.NotificationChannelManager;
import io.github.loadup.components.gotone.engine.ResilientNotificationChannelProvider;
import io.github.loadup.components.gotone.model.ChannelSendRequest;
import io.github.loadup.components.gotone.model.ChannelSendResponse;
import io.github.loadup.components.gotone.model.NotificationRequest;
import io.github.loadup.components.gotone.record.RecordHandler;
import io.github.loadup.components.gotone.template.TemplateRenderer;
import io.github.loadup.components.resilience4j.ResilienceRegistries;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.task.TaskExecutor;

class NotificationEngineBoundaryTest {
    private final NotificationRequest request = NotificationRequest.of("ALERT", List.of("receiver"), Map.of());

    @Test
    void failedProviderChainStillRecordsAnEmptyAttemptResult() {
        AtomicReference<RecordHandler.SendAttemptResult> recorded = new AtomicReference<>();
        RecordHandler records = (request, config, result) -> recorded.set(result);
        var service = new DefaultNotificationService(
                new NotificationChannelManager(List.of()),
                Optional.of(code -> List.of(new ChannelConfig("SMS", "missing", List.of(), null, Map.of()))),
                Optional.empty(),
                Optional.of(records),
                Optional.empty());
        var result = service.send(request);
        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).isEqualTo("all channels failed");
        assertThat(result.channelResults().getFirst().successCount()).isZero();
        assertThat(recorded.get().content()).isEmpty();
        assertThat(recorded.get().receiverStatus()).isEmpty();
        assertThat(recorded.get().receiverErrors()).isEmpty();
        assertThat(recorded.get().attempts()).isEmpty();
    }

    @Test
    void missingRendererAndNullResponseContentProduceAnEmptyRecordBody() {
        var manager = mock(NotificationChannelManager.class);
        var response = new ChannelSendResponse(null, 1, 0, Map.of("receiver", true), Map.of());
        when(manager.sendWithFallback(any(), any(), any(), any()))
                .thenReturn(new NotificationChannelManager.SendResult(
                        response, List.of(new NotificationChannelManager.Attempt("smtp", true, null)), true));
        AtomicReference<RecordHandler.SendAttemptResult> recorded = new AtomicReference<>();
        RecordHandler records = (request, config, result) -> recorded.set(result);
        var service = new DefaultNotificationService(
                manager,
                Optional.of(code -> List.of(new ChannelConfig("EMAIL", "smtp", List.of(), "template", Map.of()))),
                Optional.empty(),
                Optional.of(records),
                Optional.empty());
        assertThat(service.send(request).success()).isTrue();
        assertThat(recorded.get().content()).isEmpty();
        assertThat(recorded.get().attempts()).hasSize(1);
        assertThat(recorded.get().actualProvider()).isEqualTo("smtp");
    }

    @Test
    void renderingFailuresAreChannelFailuresAndRecordingFailuresDoNotUndoDelivery() {
        var manager = mock(NotificationChannelManager.class);
        TemplateRenderer brokenRenderer = (template, params) -> {
            throw new IllegalStateException("synthetic rendering failure");
        };
        ChannelConfigProvider configs =
                code -> List.of(new ChannelConfig("EMAIL", "smtp", List.of(), "template", Map.of()));
        var failed = new DefaultNotificationService(
                        manager, Optional.of(configs), Optional.of(brokenRenderer), Optional.empty(), Optional.empty())
                .send(request);
        assertThat(failed.success()).isFalse();
        assertThat(failed.channelResults().getFirst().errorMessage()).isEqualTo("synthetic rendering failure");
        verifyNoInteractions(manager);
        when(manager.sendWithFallback(any(), any(), any(), any()))
                .thenReturn(new NotificationChannelManager.SendResult(
                        new ChannelSendResponse("body", 1, 0, Map.of(), Map.of()), List.of(), true));
        RecordHandler brokenRecords = (request, config, result) -> {
            throw new IllegalStateException("synthetic store failure");
        };
        assertThat(new DefaultNotificationService(
                                manager,
                                Optional.of(configs),
                                Optional.empty(),
                                Optional.of(brokenRecords),
                                Optional.empty())
                        .send(request)
                        .success())
                .isTrue();
    }

    @Test
    void explicitChannelsFilterConfiguredRoutesAndAsyncWithoutExecutorRunsSynchronously() {
        var manager = mock(NotificationChannelManager.class);
        when(manager.sendWithFallback(any(), any(), any(), any()))
                .thenReturn(new NotificationChannelManager.SendResult(null, List.of(), false));
        ChannelConfigProvider configs = code -> List.of(
                new ChannelConfig("EMAIL", "smtp", List.of(), null, Map.of()),
                new ChannelConfig("SMS", "sms", List.of(), null, Map.of()));
        var service = new DefaultNotificationService(
                manager, Optional.of(configs), Optional.empty(), Optional.empty(), Optional.empty());
        var mail = NotificationRequest.builder()
                .serviceCode("ALERT")
                .receivers(List.of("receiver"))
                .channels(List.of("EMAIL"))
                .build();
        assertThat(service.send(mail).channelResults()).hasSize(1);
        service.sendAsync(mail);
        verify(manager, times(2)).sendWithFallback(eq("EMAIL"), eq("smtp"), any(), any());
        verifyNoMoreInteractions(manager);
    }

    @Test
    void managerHandlesMissingUnavailableDuplicateAndNullFallbackProviders() {
        var provider = mock(NotificationChannelProvider.class);
        when(provider.getChannelType()).thenReturn("EMAIL");
        when(provider.getProviderName()).thenReturn("smtp");
        when(provider.isAvailable()).thenReturn(true);
        when(provider.send(any())).thenReturn(new ChannelSendResponse("body", 1, 0, Map.of(), Map.of()));
        var manager = new NotificationChannelManager(List.of(provider));
        var sendRequest = new ChannelSendRequest(List.of("receiver"), "body", null, null);
        assertThat(manager.sendWithFallback("EMAIL", "missing", List.of("missing", "smtp", "smtp"), sendRequest)
                        .success())
                .isTrue();
        assertThat(manager.sendWithFallback("EMAIL", null, null, sendRequest).success())
                .isTrue();
        verify(provider, times(2)).send(any());
        when(provider.isAvailable()).thenReturn(false);
        assertThat(manager.sendWithFallback("EMAIL", null, null, sendRequest).success())
                .isFalse();
        assertThat(new NotificationChannelManager(null)
                        .sendWithFallback("EMAIL", null, null, sendRequest)
                        .attempts())
                .isEmpty();
        assertThat(new NotificationChannelManager.SendResult(null, List.of(), true).getSuccessfulProvider())
                .isNull();
    }

    @Test
    void resilienceHonorsDelegateAvailabilityAndOpenCircuitState() {
        var delegate = mock(NotificationChannelProvider.class);
        when(delegate.getChannelType()).thenReturn("SMS");
        when(delegate.getProviderName()).thenReturn("synthetic");
        var registries = ResilienceRegistries.ofDefaults();
        var wrapper = ResilientNotificationChannelProvider.wrap(delegate, registries);
        assertThat(wrapper.isAvailable()).isFalse();
        when(delegate.isAvailable()).thenReturn(true);
        assertThat(wrapper.isAvailable()).isTrue();
        registries
                .circuitBreakerRegistry()
                .circuitBreaker(ResilientNotificationChannelProvider.instanceName(delegate))
                .transitionToOpenState();
        assertThat(wrapper.isAvailable()).isFalse();
    }

    @Test
    void assemblyWorksWithAndWithoutResilienceRegistriesAndCanDisableWrapping() {
        var contexts = new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(GotoneEngineAutoConfiguration.class));
        contexts.run(context -> assertThat(context).hasSingleBean(NotificationService.class));
        for (boolean enabled : new boolean[] {true, false}) {
            contexts.withBean(ResilienceRegistries.class, ResilienceRegistries::ofDefaults)
                    .withPropertyValues("loadup.gotone.resilience.enabled=" + enabled)
                    .run(context -> assertThat(context).hasSingleBean(NotificationService.class));
        }
    }

    @Test
    void assemblyToleratesSeveralTaskExecutorsAndPrefersTheApplicationExecutor() {
        AtomicBoolean schedulerExecutorUsed = new AtomicBoolean();
        var contexts = new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(GotoneEngineAutoConfiguration.class))
                .withBean("applicationTaskExecutor", TaskExecutor.class, () -> Runnable::run)
                .withBean("taskScheduler", TaskExecutor.class, () -> task -> schedulerExecutorUsed.set(true));
        contexts.run(context -> {
            assertThat(context).hasSingleBean(NotificationService.class);
            context.getBean(NotificationService.class).sendAsync(request);
            assertThat(schedulerExecutorUsed).isFalse();
        });
    }
}
