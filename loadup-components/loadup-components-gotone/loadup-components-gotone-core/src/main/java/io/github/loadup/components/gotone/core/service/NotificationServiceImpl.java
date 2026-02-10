package io.github.loadup.components.gotone.core.service;

/*-
 * #%L
 * loadup-components-gotone-core
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

import io.github.loadup.commons.enums.CommonResultCodeEnum;
import io.github.loadup.commons.error.CommonException;
import io.github.loadup.commons.util.IdUtils;
import io.github.loadup.components.gotone.api.NotificationChannelProvider;
import io.github.loadup.components.gotone.api.NotificationService;
import io.github.loadup.components.gotone.core.dataobject.NotificationRecordDO;
import io.github.loadup.components.gotone.core.dataobject.ServiceChannelDO;
import io.github.loadup.components.gotone.core.manager.NotificationChannelManager;
import io.github.loadup.components.gotone.core.processor.TemplateProcessor;
import io.github.loadup.components.gotone.core.repository.NotificationRecordRepository;
import io.github.loadup.components.gotone.core.repository.NotificationServiceRepository;
import io.github.loadup.components.gotone.core.repository.ServiceChannelRepository;
import io.github.loadup.components.gotone.enums.NotificationChannel;
import io.github.loadup.components.gotone.model.ChannelSendRequest;
import io.github.loadup.components.gotone.model.ChannelSendResponse;
import io.github.loadup.components.gotone.model.NotificationRequest;
import io.github.loadup.components.gotone.model.NotificationResponse;
import io.github.loadup.components.gotone.model.NotificationResponse.ChannelSendResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 通知服务实现 - ServiceCode驱动的智能路由
 *
 * <p>核心流程：
 * <ol>
 *   <li>根据 serviceCode 查询服务配置</li>
 *   <li>查询启用的渠道配置（按优先级排序）</li>
 *   <li>渲染模板内容</li>
 *   <li>智能过滤收件人（根据渠道类型）</li>
 *   <li>发送到各渠道</li>
 *   <li>保存发送记录</li>
 * </ol>
 */
@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern PHONE_PATTERN =
        Pattern.compile("^1[3-9]\\d{9}$");

    private final NotificationServiceRepository serviceRepository;
    private final ServiceChannelRepository channelRepository;
    private final NotificationRecordRepository recordRepository;
    private final NotificationChannelManager channelManager;
    private final TemplateProcessor templateProcessor;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NotificationResponse send(NotificationRequest request) {
        log.info(">>> [GOTONE] 开始发送通知，serviceCode={}, receivers={}",
            request.getServiceCode(), request.getReceivers());

        // 1. 幂等性检查
        if (request.getRequestId() != null &&
            recordRepository.existsByRequestId(request.getRequestId())) {
            log.warn(">>> [GOTONE] 请求已处理，跳过，requestId={}", request.getRequestId());
            return buildDuplicateResponse(request);
        }

        // 2. 查询服务配置
        serviceRepository
            .findEnabledByServiceCode(request.getServiceCode())
            .orElseThrow(() -> new CommonException(
                CommonResultCodeEnum.PROCESS_FAIL, "服务不存在或已禁用: " + request.getServiceCode()));

        // 3. 查询启用的渠道配置
        List<ServiceChannelDO> channelConfigs = channelRepository
            .findEnabledChannelsByServiceCode(request.getServiceCode());

        if (channelConfigs.isEmpty()) {
            throw new CommonException(CommonResultCodeEnum.PROCESS_FAIL, "服务未配置任何渠道: " + request.getServiceCode());
        }

        // 4. 过滤渠道（如果请求中指定了渠道）
        if (request.getChannels() != null && !request.getChannels().isEmpty()) {
            channelConfigs = channelConfigs.stream()
                .filter(c -> request.getChannels().contains(c.getChannel()))
                .toList();
        }

        // 5. 生成追踪ID
        String traceId = UUID.randomUUID().toString();

        // 6. 按渠道分组发送
        List<ChannelSendResult> results = new ArrayList<>();
        for (ServiceChannelDO channelConfig : channelConfigs) {
            try {
                ChannelSendResult result = sendToChannel(request, channelConfig, traceId);
                results.add(result);
            } catch (Exception e) {
                log.error(">>> [GOTONE] 渠道发送失败，channel={}, error={}",
                    channelConfig.getChannel(), e.getMessage(), e);
                results.add(buildFailedResult(channelConfig.getChannel(), e.getMessage()));
            }
        }

        // 7. 构建响应
        boolean anySuccess = results.stream().anyMatch(ChannelSendResult::getSuccess);
        return NotificationResponse.builder()
            .traceId(traceId)
            .serviceCode(request.getServiceCode())
            .totalReceivers(request.getReceivers().size())
            .channelResults(results)
            .success(anySuccess)
            .errorMessage(anySuccess ? null : "所有渠道发送失败")
            .build();
    }

    @Override
    public void sendAsync(NotificationRequest request) {
        try {
            send(request);
        } catch (Exception e) {
            log.error(">>> [GOTONE] 异步发送失败，serviceCode={}, error={}",
                request.getServiceCode(), e.getMessage(), e);
        }
    }

    /**
     * 发送到指定渠道
     */
    private ChannelSendResult sendToChannel(
        NotificationRequest request,
        ServiceChannelDO channelConfig,
        String traceId) {

        String channel = channelConfig.getChannel();
        log.info(">>> [GOTONE] 发送到渠道，channel={}, provider={}",
            channel, channelConfig.getProvider());

        // 1. 渲染模板
        String content = templateProcessor.render(
            channelConfig.getTemplateContent(),
            request.getTemplateParams()
        );

        // 2. 获取渠道提供商
        NotificationChannelProvider provider = channelManager.getProvider(
            NotificationChannel.valueOf(channel),
            channelConfig.getProvider()
        );

        if (provider == null) {
            log.error(">>> [GOTONE] 渠道提供商不可用，channel={}, provider={}",
                channel, channelConfig.getProvider());
            return buildFailedResult(channel, "提供商不可用");
        }

        // 3. 过滤收件人（根据渠道类型）
        List<String> validReceivers = filterReceivers(request.getReceivers(), channel);

        if (validReceivers.isEmpty()) {
            log.warn(">>> [GOTONE] 无有效收件人，channel={}, receivers={}",
                channel, request.getReceivers());
            return buildFailedResult(channel, "无有效收件人");
        }

        // 4. 构建渠道请求
        ChannelSendRequest channelRequest = ChannelSendRequest.builder()
            .receivers(validReceivers)
            .content(content)
            .channelConfig(channelConfig.getChannelConfig())
            .templateParams(request.getTemplateParams())
            .build();

        // 5. 发送
        ChannelSendResponse response;
        try {
            response = provider.send(channelRequest);
        } catch (Exception e) {
            log.error(">>> [GOTONE] 渠道发送异常，channel={}, error={}",
                channel, e.getMessage(), e);
            response = ChannelSendResponse.builder()
                .content(content)
                .successCount(0)
                .failedCount(validReceivers.size())
                .build();
        }

        // 6. 保存记录
        saveRecords(request, channelConfig, validReceivers, response, traceId);

        // 7. 返回结果
        return ChannelSendResult.builder()
            .channel(channel)
            .provider(channelConfig.getProvider())
            .totalReceivers(validReceivers.size())
            .successCount(response.getSuccessCount())
            .failedCount(response.getFailedCount())
            .success(response.getSuccessCount() > 0)
            .build();
    }

    /**
     * 保存发送记录
     */
    private void saveRecords(
        NotificationRequest request,
        ServiceChannelDO channelConfig,
        List<String> receivers,
        ChannelSendResponse response,
        String traceId) {

        for (String receiver : receivers) {
            boolean success = response.isSuccess(receiver);
            NotificationRecordDO record = NotificationRecordDO.builder()
                .id(IdUtils.uuid2())
                .serviceCode(request.getServiceCode())
                .traceId(traceId)
                .requestId(request.getRequestId())
                .channel(channelConfig.getChannel())
                .provider(channelConfig.getProvider())
                .receiver(receiver)
                .templateCode(channelConfig.getTemplateCode())
                .content(response.getContent())
                .channelData(buildChannelData(channelConfig, request, receiver))
                .status(success ? "SUCCESS" : "FAILED")
                .errorMessage(success ? null : response.getErrorMessage(receiver))
                .retryCount(0)
                .maxRetries(getMaxRetries(channelConfig))
                .sendTime(LocalDateTime.now())
                .successTime(success ? LocalDateTime.now() : null)
                .build();

            recordRepository.insert(record);
        }
    }

    /**
     * 构建渠道扩展数据
     */
    private Map<String, Object> buildChannelData(
        ServiceChannelDO channelConfig,
        NotificationRequest request,
        String receiver) {

        Map<String, Object> data = new HashMap<>();
        Map<String, Object> config = channelConfig.getChannelConfig();

        switch (NotificationChannel.valueOf(channelConfig.getChannel())) {
            case EMAIL -> {
                data.put("subject", config.get("subject"));
                data.put("from", config.get("from"));
                // 可以从request中获取cc、bcc等
            }
            case SMS -> {
                data.put("phoneNumber", receiver);
                data.put("signName", config.get("signName"));
                data.put("templateId", config.get("templateId"));
            }
            case PUSH -> {
                data.put("deviceToken", receiver);
                data.put("title", config.get("title"));
                data.put("sound", config.get("sound"));
            }
        }

        return data;
    }

    /**
     * 过滤收件人（根据渠道类型识别）
     */
    private List<String> filterReceivers(List<String> receivers, String channel) {
        return switch (NotificationChannel.valueOf(channel)) {
            case EMAIL -> receivers.stream()
                .filter(this::isValidEmail)
                .toList();
            case SMS -> receivers.stream()
                .filter(this::isValidPhone)
                .toList();
            case PUSH -> receivers.stream()
                .filter(this::isPushCompatibleReceiver)
                .toList();
            default -> receivers;
        };
    }

    /**
     * 判断是否有效邮箱
     */
    private boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * 判断是否有效手机号
     */
    private boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * 判断是否为PUSH兼容的接收人（既不是邮箱也不是手机号）
     */
    private boolean isPushCompatibleReceiver(String receiver) {
        return !isValidEmail(receiver) && !isValidPhone(receiver);
    }

    /**
     * 获取最大重试次数
     */
    private Integer getMaxRetries(ServiceChannelDO channelConfig) {
        if (channelConfig.getRetryConfig() == null) {
            return 3;
        }
        Object maxRetries = channelConfig.getRetryConfig().get("maxRetries");
        return maxRetries != null ? Integer.parseInt(maxRetries.toString()) : 3;
    }

    /**
     * 构建失败结果
     */
    private ChannelSendResult buildFailedResult(String channel, String errorMessage) {
        return ChannelSendResult.builder()
            .channel(channel)
            .totalReceivers(0)
            .successCount(0)
            .failedCount(0)
            .success(false)
            .errorMessage(errorMessage)
            .build();
    }

    /**
     * 构建重复请求响应
     */
    private NotificationResponse buildDuplicateResponse(NotificationRequest request) {
        return NotificationResponse.builder()
            .serviceCode(request.getServiceCode())
            .totalReceivers(0)
            .channelResults(List.of())
            .success(true)
            .errorMessage("请求已处理（幂等性）")
            .build();
    }
}
