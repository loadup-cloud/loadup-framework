package io.github.loadup.components.gotone.engine;

/*-
 * #%L
 * Loadup Gotone Engine
 * %%
 * Copyright (C) 2025 - 2026 loadup_cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
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
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultGotoneTemplate implements GotoneTemplate {
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
                results.add(NotificationResponse.ChannelSendResult.builder()
                        .channel(cfg.channel())
                        .provider(sendResult.getSuccessfulProvider())
                        .success(sendResult.success())
                        .successCount(sendResult.response() != null ? sendResult.response().getSuccessCount() : 0)
                        .failedCount(sendResult.response() != null ? sendResult.response().getFailedCount() : 0)
                        .build());
            } catch (Exception e) {
                log.warn("Channel {} send failed", cfg.channel(), e);
                results.add(NotificationResponse.ChannelSendResult.builder()
                        .channel(cfg.channel()).success(false).errorMessage(e.getMessage()).build());
            }
        }

        boolean anySuccess = results.stream().anyMatch(NotificationResponse.ChannelSendResult::getSuccess);
        return NotificationResponse.builder()
                .traceId(traceId)
                .totalReceivers(request.getReceivers() != null ? request.getReceivers().size() : 0)
                .channelResults(results)
                .success(anySuccess)
                .build();
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
                sr.getSuccessfulProvider(), sr.success(),
                sr.response() != null ? sr.response().getSuccessCount() : 0,
                sr.response() != null ? sr.response().getFailedCount() : 0,
                sr.attempts().stream()
                        .map(a -> new RecordHandler.Attempt(a.providerName(), a.success(), a.error()))
                        .toList());
    }
}
