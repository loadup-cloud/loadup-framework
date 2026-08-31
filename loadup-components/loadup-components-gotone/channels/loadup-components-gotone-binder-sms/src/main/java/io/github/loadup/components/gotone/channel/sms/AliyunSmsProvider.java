/*-
 * #%L
 * Loadup Gotone Binder SMS
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
package io.github.loadup.components.gotone.channel.sms;

import io.github.loadup.components.gotone.NotificationChannelProvider;
import io.github.loadup.components.gotone.model.ChannelSendRequest;
import io.github.loadup.components.gotone.model.ChannelSendResponse;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Aliyun SMS provider.
 *
 * <p>Channel-level configuration keys: {@code templateId}, {@code signName}. The vendor SDK call
 * is a placeholder: implement {@link #sendMessage(String, String, String, String)} with the Aliyun
 * SMS SDK when credentials are provisioned.
 */
public class AliyunSmsProvider implements NotificationChannelProvider {

    private static final Logger log = LoggerFactory.getLogger(AliyunSmsProvider.class);

    private final String accessKeyId;
    private final String accessKeySecret;
    private final String signName;
    private final String regionId;

    public AliyunSmsProvider(String accessKeyId, String accessKeySecret, String signName, String regionId) {
        this.accessKeyId = accessKeyId;
        this.accessKeySecret = accessKeySecret;
        this.signName = signName;
        this.regionId = regionId;
    }

    @Override
    public String getChannelType() {
        return "SMS";
    }

    @Override
    public String getProviderName() {
        return "aliyun";
    }

    @Override
    public ChannelSendResponse send(ChannelSendRequest request) {
        String templateId = configValue(request.channelConfig(), "templateId", null);
        String currentSignName = configValue(request.channelConfig(), "signName", signName);

        Map<String, Boolean> receiverStatus = new HashMap<>();
        Map<String, String> receiverErrors = new HashMap<>();
        int successCount = 0;

        for (String receiver : request.receivers()) {
            if (!isPhoneNumber(receiver)) {
                receiverStatus.put(receiver, false);
                receiverErrors.put(receiver, "invalid phone number");
                log.warn("Invalid phone number={}", maskPhone(receiver));
                continue;
            }
            try {
                sendMessage(receiver, templateId, currentSignName, request.content());
                receiverStatus.put(receiver, true);
                successCount++;
            } catch (Exception e) {
                receiverStatus.put(receiver, false);
                receiverErrors.put(receiver, e.getMessage());
                log.warn("SMS send failed for receiver={}", maskPhone(receiver), e);
            }
        }

        return new ChannelSendResponse(
                request.content(),
                successCount,
                request.receivers().size() - successCount,
                receiverStatus,
                receiverErrors);
    }

    /**
     * Sends one SMS.
     *
     * <p>Placeholder: replace with the Aliyun SMS SDK call.
     *
     * @param receiver the phone number
     * @param templateId the vendor template id
     * @param signName the signature
     * @param content the rendered content
     * @throws Exception when the send fails
     */
    protected void sendMessage(String receiver, String templateId, String signName, String content) throws Exception {
        if (accessKeyId == null || accessKeyId.isBlank() || accessKeySecret == null || accessKeySecret.isBlank()) {
            throw new IllegalStateException("Aliyun SMS credentials are not configured");
        }
        log.debug(
                "Aliyun SMS placeholder: regionId={} templateId={} signName={} contentLength={}",
                regionId,
                templateId,
                signName,
                content.length());
    }

    @Override
    public boolean isAvailable() {
        return accessKeyId != null && !accessKeyId.isBlank() && accessKeySecret != null && !accessKeySecret.isBlank();
    }

    private static String configValue(Map<String, Object> config, String key, String defaultValue) {
        if (config == null || !config.containsKey(key)) {
            return defaultValue;
        }
        Object value = config.get(key);
        return value != null ? value.toString() : defaultValue;
    }

    private static boolean isPhoneNumber(String receiver) {
        return receiver != null && receiver.matches("^\\+?[0-9]{5,20}$");
    }

    private static String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return "***";
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}
