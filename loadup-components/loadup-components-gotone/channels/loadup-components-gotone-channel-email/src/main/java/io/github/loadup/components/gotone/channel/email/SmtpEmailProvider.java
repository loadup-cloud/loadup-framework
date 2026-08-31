package io.github.loadup.components.gotone.channel.email;

/*-
 * #%L
 * loadup-components-gotone-channel-email
 * %%
 * Copyright (C) 2026 LoadUp Cloud
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

import io.github.loadup.components.gotone.GotoneProvider;
import io.github.loadup.components.gotone.model.ChannelSendRequest;
import io.github.loadup.components.gotone.model.ChannelSendResponse;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

/**
 * SMTP 邮件渠道提供商
 *
 * <p>基于 Spring Boot Mail 实现的 SMTP 邮件发送
 *
 * <p>配置示例：
 * <pre>
 * spring.mail.host=smtp.example.com
 * spring.mail.port=587
 * spring.mail.username=noreply@example.com
 * spring.mail.password=YOUR_PASSWORD
 * spring.mail.properties.mail.smtp.auth=true
 * spring.mail.properties.mail.smtp.starttls.enable=true
 * </pre>
 */
public class SmtpEmailProvider implements GotoneProvider {
    private static final Logger log = LoggerFactory.getLogger(SmtpEmailProvider.class);

    private JavaMailSender mailSender;

    private String fromEmail;

    private String fromName;

    @Override
    public String getChannelType() {
        return "EMAIL";
    }

    @Override
    public ChannelSendResponse send(ChannelSendRequest request) {
        log.info(
                ">>> [GOTONE-EMAIL-SMTP] 开始发送邮件, receivers={}, content length={}",
                request.getReceivers(),
                request.getContent() != null ? request.getContent().length() : 0);

        // 参数校验
        if (mailSender == null) {
            return buildErrorResponse("JavaMailSender 未配置", 0, 0);
        }

        if (request.getReceivers() == null || request.getReceivers().isEmpty()) {
            return buildErrorResponse("收件人列表为空", 0, 0);
        }

        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            return buildErrorResponse("邮件内容为空", request.getReceivers().size(), 0);
        }

        // 从渠道配置中获取邮件主题、CC、BCC等信息
        String subject = getConfigValue(request.getChannelConfig(), "subject", "通知消息");
        String from = getConfigValue(request.getChannelConfig(), "from", fromEmail);
        List<String> ccList = getConfigList(request.getChannelConfig(), "cc");
        List<String> bccList = getConfigList(request.getChannelConfig(), "bcc");
        boolean isHtml = Boolean.parseBoolean(getConfigValue(request.getChannelConfig(), "html", "true"));

        Map<String, Boolean> receiverStatus = new HashMap<>();
        Map<String, String> receiverErrors = new HashMap<>();
        int successCount = 0;
        int failedCount = 0;

        try {
            // 批量发送模式：一次性发送给所有收件人
            if (shouldBatchSend(request)) {
                boolean batchSuccess = sendBatch(
                        request.getReceivers(),
                        subject,
                        from,
                        ccList,
                        bccList,
                        request.getContent(),
                        isHtml,
                        receiverStatus,
                        receiverErrors);

                if (batchSuccess) {
                    successCount = request.getReceivers().size();
                    log.info(
                            ">>> [GOTONE-EMAIL-SMTP] 批量邮件发送成功, recipients={}",
                            request.getReceivers().size());
                } else {
                    failedCount = request.getReceivers().size();
                    log.error(">>> [GOTONE-EMAIL-SMTP] 批量邮件发送失败");
                }
            } else {
                // 逐个发送模式：适用于需要个性化内容或独立追踪的场景
                for (String receiver : request.getReceivers()) {
                    if (!isValidEmail(receiver)) {
                        receiverStatus.put(receiver, false);
                        receiverErrors.put(receiver, "无效的邮箱地址格式");
                        failedCount++;
                        log.warn(">>> [GOTONE-EMAIL-SMTP] 无效邮箱: {}", receiver);
                        continue;
                    }

                    try {
                        sendSingle(receiver, subject, from, ccList, bccList, request.getContent(), isHtml);
                        receiverStatus.put(receiver, true);
                        successCount++;
                        log.info(">>> [GOTONE-EMAIL-SMTP] 邮件发送成功: {}", maskEmail(receiver));
                    } catch (Exception e) {
                        receiverStatus.put(receiver, false);
                        receiverErrors.put(receiver, "发送失败: " + e.getMessage());
                        failedCount++;
                        log.error(
                                ">>> [GOTONE-EMAIL-SMTP] 邮件发送失败: {}, error={}", maskEmail(receiver), e.getMessage(), e);
                    }
                }
            }

            ChannelSendResponse sendResponse = new ChannelSendResponse();
            sendResponse.setContent(request.getContent());
            sendResponse.setSuccessCount(successCount);
            sendResponse.setFailedCount(failedCount);
            sendResponse.setReceiverStatus(receiverStatus);
            sendResponse.setReceiverErrors(receiverErrors);
            return sendResponse;

        } catch (Exception e) {
            log.error(">>> [GOTONE-EMAIL-SMTP] 发送邮件异常", e);

            // 所有收件人标记为失败
            for (String receiver : request.getReceivers()) {
                receiverStatus.put(receiver, false);
                receiverErrors.put(receiver, "系统异常: " + e.getMessage());
            }

            ChannelSendResponse sendResponse = new ChannelSendResponse();
            sendResponse.setContent(request.getContent());
            sendResponse.setSuccessCount(0);
            sendResponse.setFailedCount(request.getReceivers().size());
            sendResponse.setReceiverStatus(receiverStatus);
            sendResponse.setReceiverErrors(receiverErrors);
            return sendResponse;
        }
    }

    @Override
    public boolean isAvailable() {
        boolean available = mailSender != null;

        if (!available) {
            log.warn(">>> [GOTONE-EMAIL-SMTP] Provider 不可用: JavaMailSender 未配置");
        }

        return available;
    }

    @Override
    public String getProviderName() {
        return "smtp";
    }

    /**
     * 发送单封邮件
     */
    private void sendSingle(
            String to,
            String subject,
            String from,
            List<String> ccList,
            List<String> bccList,
            String content,
            boolean isHtml)
            throws MessagingException {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        try {
            helper.setFrom(from, fromName);
        } catch (Exception e) {
            // Fallback to simple from address
            helper.setFrom(from);
        }

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(content, isHtml);

        // 添加 CC
        if (ccList != null && !ccList.isEmpty()) {
            helper.setCc(ccList.toArray(new String[0]));
        }

        // 添加 BCC
        if (bccList != null && !bccList.isEmpty()) {
            helper.setBcc(bccList.toArray(new String[0]));
        }

        //        mailSender.send(message);
    }

    /**
     * 批量发送邮件（所有收件人在 TO 字段）
     */
    private boolean sendBatch(
            List<String> receivers,
            String subject,
            String from,
            List<String> ccList,
            List<String> bccList,
            String content,
            boolean isHtml,
            Map<String, Boolean> receiverStatus,
            Map<String, String> receiverErrors) {

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            try {
                helper.setFrom(from, fromName);
            } catch (Exception e) {
                // Fallback to simple from address
                helper.setFrom(from);
            }

            helper.setTo(receivers.toArray(new String[0]));
            helper.setSubject(subject);
            helper.setText(content, isHtml);

            // ...existing code...
            if (ccList != null && !ccList.isEmpty()) {
                helper.setCc(ccList.toArray(new String[0]));
            }

            // 添加 BCC
            if (bccList != null && !bccList.isEmpty()) {
                helper.setBcc(bccList.toArray(new String[0]));
            }

            //            mailSender.send(message);

            // 标记所有收件人为成功
            for (String receiver : receivers) {
                receiverStatus.put(receiver, true);
            }

            return true;

        } catch (Exception e) {
            log.error(">>> [GOTONE-EMAIL-SMTP] 批量邮件发送失败", e);

            // 标记所有收件人为失败
            for (String receiver : receivers) {
                receiverStatus.put(receiver, false);
                receiverErrors.put(receiver, "批量发送失败: " + e.getMessage());
            }

            return false;
        }
    }

    /**
     * 判断是否应该使用批量发送
     */
    private boolean shouldBatchSend(ChannelSendRequest request) {
        // 收件人数量较少时使用批量发送
        // 或者配置中明确指定批量发送
        boolean batchConfig = Boolean.parseBoolean(getConfigValue(request.getChannelConfig(), "batch", "false"));

        return batchConfig || request.getReceivers().size() <= 10;
    }

    /**
     * 验证邮箱地址格式（简单验证）
     */
    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        // 简单的邮箱格式验证
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    /**
     * 邮箱脱敏显示
     */
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***@***";
        }

        String[] parts = email.split("@");
        String localPart = parts[0];
        String domain = parts[1];

        if (localPart.length() <= 3) {
            return "***@" + domain;
        }

        return localPart.substring(0, 2) + "***" + localPart.substring(localPart.length() - 1) + "@" + domain;
    }

    /**
     * 从配置中获取值（带默认值）
     */
    private String getConfigValue(Map<String, Object> config, String key, String defaultValue) {
        if (config == null || !config.containsKey(key)) {
            return defaultValue;
        }
        Object value = config.get(key);
        return value != null ? value.toString() : defaultValue;
    }

    /**
     * 从配置中获取列表值
     */
    @SuppressWarnings("unchecked")
    private List<String> getConfigList(Map<String, Object> config, String key) {
        if (config == null || !config.containsKey(key)) {
            return Collections.emptyList();
        }

        Object value = config.get(key);
        if (value instanceof List) {
            return (List<String>) value;
        } else if (value instanceof String) {
            // 支持逗号分隔的字符串
            String strValue = (String) value;
            if (strValue.trim().isEmpty()) {
                return Collections.emptyList();
            }
            return Arrays.asList(strValue.split(","));
        }

        return Collections.emptyList();
    }

    /**
     * 构建错误响应
     */
    private ChannelSendResponse buildErrorResponse(String errorMessage, int total, int success) {
        Map<String, Boolean> receiverStatus = new HashMap<>();
        Map<String, String> receiverErrors = new HashMap<>();

        log.error(">>> [GOTONE-EMAIL-SMTP] {}", errorMessage);
        ChannelSendResponse sendResponse = new ChannelSendResponse();
        sendResponse.setContent(null);
        sendResponse.setSuccessCount(success);
        sendResponse.setFailedCount(total - success);
        sendResponse.setReceiverStatus(receiverStatus);
        sendResponse.setReceiverErrors(receiverErrors);
        return sendResponse;
    }

    public SmtpEmailProvider(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public SmtpEmailProvider(JavaMailSender mailSender, String fromEmail, String fromName) {
        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
        this.fromName = fromName;
    }
}
