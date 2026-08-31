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
 * Huawei Cloud SMS provider.
 *
 * <p>Channel-level configuration keys: {@code templateId}, {@code signature}, {@code sender}. The
 * vendor SDK call is a placeholder: implement {@link #sendMessage(String, String, String, String,
 * String)} with the Huawei Cloud SMS SDK when credentials are provisioned.
 */
public class HuaweiSmsProvider implements NotificationChannelProvider {

    private static final Logger log = LoggerFactory.getLogger(HuaweiSmsProvider.class);

    private final String appKey;
    private final String appSecret;
    private final String sender;
    private final String signature;
    private final String endpoint;

    public HuaweiSmsProvider(String appKey, String appSecret, String sender, String signature, String endpoint) {
        this.appKey = appKey;
        this.appSecret = appSecret;
        this.sender = sender;
        this.signature = signature;
        this.endpoint = endpoint;
    }

    @Override
    public String getChannelType() {
        return "SMS";
    }

    @Override
    public String getProviderName() {
        return "huawei";
    }

    @Override
    public ChannelSendResponse send(ChannelSendRequest request) {
        String templateId = configValue(request.channelConfig(), "templateId", null);
        String currentSignature = configValue(request.channelConfig(), "signature", signature);
        String currentSender = configValue(request.channelConfig(), "sender", sender);

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
                sendMessage(receiver, templateId, currentSignature, currentSender, request.content());
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
     * <p>Placeholder: replace with the Huawei Cloud SMS SDK call.
     *
     * @param receiver the phone number
     * @param templateId the vendor template id
     * @param signature the signature
     * @param sender the sender id
     * @param content the rendered content
     * @throws Exception when the send fails
     */
    protected void sendMessage(String receiver, String templateId, String signature, String sender, String content)
            throws Exception {
        if (appKey == null || appKey.isBlank() || appSecret == null || appSecret.isBlank()) {
            throw new IllegalStateException("Huawei SMS credentials are not configured");
        }
        log.debug(
                "Huawei SMS placeholder: endpoint={} templateId={} signature={} sender={} contentLength={}",
                endpoint,
                templateId,
                signature,
                sender,
                content.length());
    }

    @Override
    public boolean isAvailable() {
        return appKey != null && !appKey.isBlank() && appSecret != null && !appSecret.isBlank();
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
