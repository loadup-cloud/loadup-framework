/*-
 * #%L
 * Loadup Gotone Engine
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
package io.github.loadup.components.gotone.engine;

import io.github.loadup.components.gotone.NotificationService;
import io.github.loadup.components.gotone.config.ChannelConfigProvider;
import io.github.loadup.components.gotone.config.ChannelConfigProvider.ChannelConfig;
import io.github.loadup.components.gotone.model.ChannelSendRequest;
import io.github.loadup.components.gotone.model.NotificationRequest;
import io.github.loadup.components.gotone.model.NotificationResponse;
import io.github.loadup.components.gotone.record.RecordHandler;
import io.github.loadup.components.gotone.record.RecordHandler.SendAttemptResult;
import io.github.loadup.components.gotone.template.TemplateRenderer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.TaskExecutor;

/**
 * Default {@link NotificationService} implementation: a pure send engine with zero storage and
 * zero database dependencies.
 *
 * <p>The engine resolves channel configuration through the optional {@link ChannelConfigProvider},
 * renders templates through the optional {@link TemplateRenderer}, sends through the registered
 * channel providers (with fallback and resilience), and reports outcomes through the optional
 * {@link RecordHandler}. Without a channel configuration provider, requests can still be sent
 * directly to the channels listed in {@link NotificationRequest#channels()}.
 */
public class DefaultNotificationService implements NotificationService {
    private static final Logger log = LoggerFactory.getLogger(DefaultNotificationService.class);

    private final NotificationChannelManager channelManager;
    private final Optional<ChannelConfigProvider> channelConfigProvider;
    private final Optional<TemplateRenderer> templateRenderer;
    private final Optional<RecordHandler> recordHandler;
    private final Optional<TaskExecutor> taskExecutor;

    public DefaultNotificationService(
            NotificationChannelManager channelManager,
            Optional<ChannelConfigProvider> channelConfigProvider,
            Optional<TemplateRenderer> templateRenderer,
            Optional<RecordHandler> recordHandler,
            Optional<TaskExecutor> taskExecutor) {
        this.channelManager = channelManager;
        this.channelConfigProvider = channelConfigProvider;
        this.templateRenderer = templateRenderer;
        this.recordHandler = recordHandler;
        this.taskExecutor = taskExecutor;
    }

    @Override
    public NotificationResponse send(NotificationRequest request) {
        String traceId = UUID.randomUUID().toString();
        List<ChannelConfig> channels = resolveChannels(request);
        if (channels.isEmpty()) {
            log.warn(
                    "No channel configuration resolved for serviceCode={} channels={}",
                    request.serviceCode(),
                    request.channels());
        }

        List<NotificationResponse.ChannelSendResult> results = new ArrayList<>();
        for (ChannelConfig config : channels) {
            results.add(sendToChannel(request, config));
        }

        boolean anySuccess = results.stream().anyMatch(NotificationResponse.ChannelSendResult::success);
        String errorMessage =
                results.isEmpty() ? "no channel configuration resolved" : anySuccess ? null : "all channels failed";
        return new NotificationResponse(
                traceId, request.serviceCode(), request.receivers().size(), results, anySuccess, errorMessage);
    }

    @Override
    public void sendAsync(NotificationRequest request) {
        if (taskExecutor.isPresent()) {
            taskExecutor.get().execute(() -> send(request));
        } else {
            log.warn("No TaskExecutor bean available, falling back to a synchronous send");
            send(request);
        }
    }

    private NotificationResponse.ChannelSendResult sendToChannel(NotificationRequest request, ChannelConfig config) {
        try {
            String content = renderContent(config, request);
            ChannelSendRequest sendRequest = ChannelSendRequest.builder()
                    .receivers(request.receivers())
                    .content(content)
                    .channelConfig(config.channelConfig())
                    .templateParams(request.templateParams())
                    .build();

            NotificationChannelManager.SendResult sendResult = channelManager.sendWithFallback(
                    config.channel(), config.provider(), config.fallbackProviders(), sendRequest);

            if (recordHandler.isPresent()) {
                try {
                    recordHandler.get().onResult(request, config, toAttemptResult(sendResult));
                } catch (Exception e) {
                    log.warn("RecordHandler failed while send succeeded", e);
                }
            }

            return new NotificationResponse.ChannelSendResult(
                    config.channel(),
                    sendResult.getSuccessfulProvider(),
                    request.receivers().size(),
                    sendResult.response() != null ? sendResult.response().successCount() : 0,
                    sendResult.response() != null ? sendResult.response().failedCount() : 0,
                    sendResult.success(),
                    sendResult.success() ? null : "provider chain failed");
        } catch (Exception e) {
            log.warn("Channel {} send failed", config.channel(), e);
            return new NotificationResponse.ChannelSendResult(
                    config.channel(), null, request.receivers().size(), 0, 0, false, e.getMessage());
        }
    }

    private List<ChannelConfig> resolveChannels(NotificationRequest request) {
        List<ChannelConfig> resolved = channelConfigProvider
                .filter(provider -> request.serviceCode() != null)
                .map(provider -> provider.findEnabledByServiceCode(request.serviceCode()))
                .orElseGet(List::of);

        if (request.channels().isEmpty()) {
            return resolved;
        }
        if (!resolved.isEmpty()) {
            return resolved.stream()
                    .filter(config -> request.channels().contains(config.channel()))
                    .toList();
        }
        // Direct channel mode: no configuration provider is available, so let every registered
        // provider of the requested channel type participate in the fallback chain.
        return request.channels().stream()
                .map(channel -> new ChannelConfig(channel, null, List.of(), null, Map.of()))
                .toList();
    }

    private String renderContent(ChannelConfig config, NotificationRequest request) {
        if (config.templateContent() != null && templateRenderer.isPresent()) {
            return templateRenderer.get().render(config.templateContent(), request.templateParams());
        }
        return "";
    }

    private SendAttemptResult toAttemptResult(NotificationChannelManager.SendResult sendResult) {
        return new SendAttemptResult(
                sendResult.getSuccessfulProvider(),
                sendResult.success(),
                sendResult.response() != null ? sendResult.response().successCount() : 0,
                sendResult.response() != null ? sendResult.response().failedCount() : 0,
                sendResult.response() != null && sendResult.response().content() != null
                        ? sendResult.response().content()
                        : "",
                sendResult.response() != null ? sendResult.response().receiverStatus() : Map.of(),
                sendResult.response() != null ? sendResult.response().receiverErrors() : Map.of(),
                sendResult.attempts().stream()
                        .map(attempt ->
                                new RecordHandler.Attempt(attempt.providerName(), attempt.success(), attempt.error()))
                        .toList());
    }
}
