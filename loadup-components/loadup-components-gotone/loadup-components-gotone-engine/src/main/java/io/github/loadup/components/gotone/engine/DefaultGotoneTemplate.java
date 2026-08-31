package io.github.loadup.components.gotone.engine;

/*-
 * #%L
 * Loadup Gotone Engine
 * %%
 * Copyright (C) 2025 - 2026 loadup_cloud
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

import io.github.loadup.components.gotone.GotoneTemplate;
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
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultGotoneTemplate implements GotoneTemplate {
    private static final Logger log = LoggerFactory.getLogger(DefaultGotoneTemplate.class);

    private final NotificationChannelManager channelManager;
    private final Optional<ChannelConfigProvider> channelConfigProvider;
    private final Optional<TemplateRenderer> templateRenderer;
    private final Optional<RecordHandler> recordHandler;

    public DefaultGotoneTemplate(
            NotificationChannelManager channelManager,
            Optional<ChannelConfigProvider> channelConfigProvider,
            Optional<TemplateRenderer> templateRenderer,
            Optional<RecordHandler> recordHandler) {
        this.channelManager = channelManager;
        this.channelConfigProvider = channelConfigProvider;
        this.templateRenderer = templateRenderer;
        this.recordHandler = recordHandler;
    }

    @Override
    public NotificationResponse send(NotificationRequest request) {
        String traceId = UUID.randomUUID().toString();
        List<NotificationResponse.ChannelSendResult> results = new ArrayList<>();

        // 1. 获取渠道配置
        List<ChannelConfig> channels = resolveChannels(request);

        // 2. 逐个渠道发送（含降级）
        for (ChannelConfig cfg : channels) {
            try {
                // 渲染模板
                String content = renderContent(cfg, request);

                // 构建请求
                ChannelSendRequest sendReq = ChannelSendRequest.builder()
                        .receivers(request.getReceivers())
                        .content(content)
                        .channelConfig(cfg.channelConfig())
                        .templateParams(request.getTemplateParams())
                        .build();

                // 发送（含降级链）
                var sendResult = channelManager.sendWithFallback(
                        cfg.channel(), cfg.provider(), cfg.fallbackProviders(), sendReq);

                // 记录
                if (recordHandler.isPresent()) {
                    try {
                        recordHandler.get().onResult(request, cfg, toAttemptResult(sendResult));
                    } catch (Exception e) {
                        log.warn("RecordHandler failed, send succeeded", e);
                    }
                }

                // 构建响应
                NotificationResponse.ChannelSendResult channelSendResult = new NotificationResponse.ChannelSendResult();
                channelSendResult.setChannel(cfg.channel());
                channelSendResult.setProvider(sendResult.getSuccessfulProvider());
                channelSendResult.setSuccess(sendResult.success());
                channelSendResult.setSuccessCount(
                        sendResult.response() != null ? sendResult.response().getSuccessCount() : 0);
                channelSendResult.setFailedCount(
                        sendResult.response() != null ? sendResult.response().getFailedCount() : 0);
                results.add(channelSendResult);
            } catch (Exception e) {
                log.warn("Channel {} send failed", cfg.channel(), e);
                NotificationResponse.ChannelSendResult channelSendResult = new NotificationResponse.ChannelSendResult();
                channelSendResult.setChannel(cfg.channel());
                channelSendResult.setSuccess(false);
                channelSendResult.setErrorMessage(e.getMessage());
                results.add(channelSendResult);
            }
        }

        boolean anySuccess = results.stream().anyMatch(NotificationResponse.ChannelSendResult::getSuccess);
        NotificationResponse notificationResponse = new NotificationResponse();
        notificationResponse.setTraceId(traceId);
        notificationResponse.setTotalReceivers(
                request.getReceivers() != null ? request.getReceivers().size() : 0);
        notificationResponse.setChannelResults(results);
        notificationResponse.setSuccess(anySuccess);
        return notificationResponse;
    }

    @Override
    public void sendAsync(NotificationRequest request) {
        send(request);
    }

    private List<ChannelConfig> resolveChannels(NotificationRequest request) {
        if (request.getServiceCode() != null && channelConfigProvider.isPresent()) {
            return channelConfigProvider.get().findEnabledByServiceCode(request.getServiceCode());
        }
        return List.of();
    }

    private String renderContent(ChannelConfig cfg, NotificationRequest request) {
        if (cfg.templateContent() != null && templateRenderer.isPresent()) {
            return templateRenderer.get().render(cfg.templateContent(), request.getTemplateParams());
        }
        return "";
    }

    private SendAttemptResult toAttemptResult(NotificationChannelManager.SendResult sr) {
        return new SendAttemptResult(
                sr.getSuccessfulProvider(),
                sr.success(),
                sr.response() != null ? sr.response().getSuccessCount() : 0,
                sr.response() != null ? sr.response().getFailedCount() : 0,
                sr.attempts().stream()
                        .map(a -> new RecordHandler.Attempt(a.providerName(), a.success(), a.error()))
                        .toList());
    }
}
