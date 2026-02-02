package io.github.loadup.components.gotone.service.impl;

/*-
 * #%L
 * loadup-components-gotone-api
 * %%
 * Copyright (C) 2026 LoadUp Cloud
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

import io.github.loadup.components.extension.core.BizScenario;
import io.github.loadup.components.extension.exector.ExtensionExecutor;
import io.github.loadup.components.gotone.api.GotoneNotificationService;
import io.github.loadup.components.gotone.api.INotificationProvider;
import io.github.loadup.components.gotone.converter.GotoneConverter;
import io.github.loadup.components.gotone.dataobject.ChannelMappingDO;
import io.github.loadup.components.gotone.dataobject.NotificationTemplateDO;
import io.github.loadup.components.gotone.domain.ChannelMapping;
import io.github.loadup.components.gotone.domain.NotificationRecord;
import io.github.loadup.components.gotone.enums.NotificationChannel;
import io.github.loadup.components.gotone.enums.NotificationStatus;
import io.github.loadup.components.gotone.model.NotificationRequest;
import io.github.loadup.components.gotone.model.NotificationResponse;
import io.github.loadup.components.gotone.repository.*;
import io.github.loadup.components.gotone.repository.ChannelMappingRepository;
import io.github.loadup.components.gotone.repository.NotificationRecordRepository;
import io.github.loadup.components.gotone.repository.NotificationTemplateRepository;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/** Gotone 通知服务实现 */
@Slf4j
@Service
public class GotoneNotificationServiceImpl implements GotoneNotificationService {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");

    @Autowired
    private ChannelMappingRepository channelMappingRepository;

    @Autowired
    private NotificationTemplateRepository templateRepository;

    @Autowired
    private NotificationRecordRepository recordRepository;

    @Autowired
    private ExtensionExecutor extensionExecutor;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Autowired
    private GotoneConverter gotoneConverter;

    @Override
    public boolean send(String businessCode, List<String> addresses, Map<String, Object> params) {
        return send(businessCode, UUID.randomUUID().toString(), addresses, params);
    }

    @Override
    public boolean send(String businessCode, String bizId, List<String> addresses, Map<String, Object> params) {
        log.info("Sending notification for businessCode: {}, bizId: {}", businessCode, bizId);

        try {
            // 1. 查询渠道映射
            List<ChannelMappingDO> mappingDOs = channelMappingRepository.findByBusinessCodeAndEnabled(businessCode);
            if (CollectionUtils.isEmpty(mappingDOs)) {
                log.warn("No channel mapping found for businessCode: {}", businessCode);
                return false;
            }

            // 2. 转换为 Domain 对象
            List<ChannelMapping> mappings = gotoneConverter.toChannelMappingList(mappingDOs);

            // 3. 为每个渠道发送通知
            boolean allSuccess = true;
            for (ChannelMapping mapping : mappings) {
                try {
                    boolean success = sendChannel(mapping, bizId, addresses, params);
                    if (!success) {
                        allSuccess = false;
                    }
                } catch (Exception e) {
                    log.error("Failed to send channel {}: {}", mapping.getChannel(), e.getMessage(), e);
                    allSuccess = false;
                }
            }

            return allSuccess;
        } catch (Exception e) {
            log.error("Failed to send notification: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public void sendAsync(String businessCode, List<String> addresses, Map<String, Object> params) {
        sendAsync(businessCode, UUID.randomUUID().toString(), addresses, params);
    }

    @Override
    public void sendAsync(String businessCode, String bizId, List<String> addresses, Map<String, Object> params) {
        CompletableFuture.runAsync(() -> send(businessCode, bizId, addresses, params));
    }

    /** 发送单个渠道 */
    private boolean sendChannel(
            ChannelMapping mapping, String bizId, List<String> addresses, Map<String, Object> params) {
        log.debug("Sending channel: {} for bizId: {}", mapping.getChannel(), bizId);

        // 1. 加载模板
        NotificationTemplateDO templateDO = templateRepository
                .findByTemplateCodeAndEnabled(mapping.getTemplateCode())
                .orElse(null);

        if (templateDO == null) {
            log.warn("Template not found: {}", mapping.getTemplateCode());
            return false;
        }

        // 2. 渲染模板
        String content = renderTemplate(templateDO.getContent(), params);
        String title =
                templateDO.getTitleTemplate() != null ? renderTemplate(templateDO.getTitleTemplate(), params) : null;

        // 3. 构建发送请求
        NotificationRequest request = NotificationRequest.builder()
                .bizId(bizId + "-" + mapping.getChannel())
                .channel(NotificationChannel.valueOf(mapping.getChannel()))
                .receivers(addresses)
                .templateCode(mapping.getTemplateCode())
                .title(title)
                .content(content)
                .priority(mapping.getPriority())
                .providers(mapping.getProviderList())
                .async(false)
                .build();

        // 4. 发送（带降级和重试）
        NotificationResponse response = sendWithFallback(request);

        // 5. 保存记录
        saveRecord(mapping.getBusinessCode(), request, response);

        return response.getSuccess();
    }

    /** 带降级发送 */
    private NotificationResponse sendWithFallback(NotificationRequest request) {
        List<String> providers = request.getProviders();

        if (CollectionUtils.isEmpty(providers)) {
            // 使用扩展点默认路由
            return sendWithExtension(request, "default");
        }

        // 按提供商列表依次尝试
        NotificationResponse lastResponse = null;
        for (String provider : providers) {
            try {
                NotificationResponse response = sendWithExtension(request, provider);
                if (response.getSuccess()) {
                    log.info("Sent successfully via provider: {}", provider);
                    return response;
                }
                lastResponse = response;
                log.warn("Provider {} failed: {}", provider, response.getErrorMessage());
            } catch (Exception e) {
                log.error("Provider {} error: {}", provider, e.getMessage());
                lastResponse = buildFailedResponse(request, provider, e.getMessage());
            }
        }

        return lastResponse != null ? lastResponse : buildFailedResponse(request, null, "All providers failed");
    }

    /** 使用扩展点发送 */
    private NotificationResponse sendWithExtension(NotificationRequest request, String provider) {
        BizScenario scenario = BizScenario.valueOf(request.getChannel().name(), provider, "default");

        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(provider);

        return circuitBreaker.executeSupplier(
                () -> extensionExecutor.execute(INotificationProvider.class, scenario, p -> p.send(request)));
    }

    /** 渲染模板 */
    private String renderTemplate(String template, Map<String, Object> params) {
        if (template == null || params == null) {
            return template;
        }

        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String key = matcher.group(1);
            Object value = params.get(key);
            String replacement = value != null ? value.toString() : "";
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /** 保存发送记录 */
    private void saveRecord(String businessCode, NotificationRequest request, NotificationResponse response) {
        try {
            NotificationRecord record = new NotificationRecord();
            record.setId(UUID.randomUUID().toString());
            record.setTraceId(getCurrentTraceId());
            record.setBusinessCode(businessCode);
            record.setBizId(request.getBizId());
            record.setMessageId(response.getMessageId());
            record.setChannel(request.getChannel().name());
            record.setReceivers(request.getReceivers());
            record.setTemplateCode(request.getTemplateCode());
            record.setTitle(request.getTitle());
            record.setContent(request.getContent());
            record.setProvider(response.getProvider());
            record.setStatus(response.getStatus().name());
            record.setRetryCount(0);
            record.setPriority(request.getPriority());
            record.setErrorMessage(response.getErrorMessage());
            record.setSendTime(response.getSendTime());
            record.setCreatedAt(LocalDateTime.now());
            record.setUpdatedAt(LocalDateTime.now());

            // 转换为 DO 并保存
            recordRepository.save(gotoneConverter.toNotificationRecordDO(record));
        } catch (Exception e) {
            log.error("Failed to save notification record: {}", e.getMessage(), e);
        }
    }

    /** 获取当前链路追踪ID（集成 LoadUp Tracer） */
    private String getCurrentTraceId() {
        // TODO: 集成 LoadUp Tracer 组件
        return UUID.randomUUID().toString();
    }

    /** 构建失败响应 */
    private NotificationResponse buildFailedResponse(
            NotificationRequest request, String provider, String errorMessage) {
        return NotificationResponse.builder()
                .success(false)
                .status(NotificationStatus.FAILED)
                .bizId(request.getBizId())
                .provider(provider)
                .errorMessage(errorMessage)
                .sendTime(LocalDateTime.now())
                .build();
    }
}
