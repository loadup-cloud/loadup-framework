/*-
 * #%L
 * Loadup Gotone Binder Push
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
package io.github.loadup.components.gotone.channel.push;

import io.github.loadup.components.gotone.NotificationChannelProvider;
import io.github.loadup.components.gotone.model.ChannelSendRequest;
import io.github.loadup.components.gotone.model.ChannelSendResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Firebase Cloud Messaging push provider.
 *
 * <p>Channel-level configuration keys: {@code title}, {@code sound}, {@code badge}, {@code extras}.
 * The actual FCM HTTP v1 call is a placeholder: implement {@link #sendMessage(String, String,
 * Map)} with the Firebase Admin SDK when credentials are provisioned.
 */
public class FcmPushProvider implements NotificationChannelProvider {

    private static final Logger log = LoggerFactory.getLogger(FcmPushProvider.class);
    private static final Pattern DEVICE_TOKEN = Pattern.compile("^[A-Za-z0-9_\\-]{20,}$");

    private final String serverKey;
    private final String projectId;

    public FcmPushProvider(String serverKey, String projectId) {
        this.serverKey = serverKey;
        this.projectId = projectId;
    }

    @Override
    public String getChannelType() {
        return "PUSH";
    }

    @Override
    public String getProviderName() {
        return "fcm";
    }

    @Override
    public ChannelSendResponse send(ChannelSendRequest request) {
        Map<String, Object> extras = configMap(request.channelConfig(), "extras");

        Map<String, Boolean> receiverStatus = new HashMap<>();
        Map<String, String> receiverErrors = new HashMap<>();
        int successCount = 0;

        for (String token : request.receivers()) {
            if (!DEVICE_TOKEN.matcher(token).matches()) {
                receiverStatus.put(token, false);
                receiverErrors.put(token, "invalid device token format");
                log.warn("Invalid device token={}", maskToken(token));
                continue;
            }
            try {
                sendMessage(token, request.content(), extras);
                receiverStatus.put(token, true);
                successCount++;
                log.info("Push sent to token={}", maskToken(token));
            } catch (Exception e) {
                receiverStatus.put(token, false);
                receiverErrors.put(token, e.getMessage());
                log.warn("Push failed for token={}", maskToken(token), e);
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
     * Sends one message to one device token.
     *
     * <p>Placeholder: replace with the Firebase Admin SDK call
     * {@code FirebaseMessaging.getInstance().send(message)}.
     *
     * @param token the device token
     * @param content the message body
     * @param extras the custom data payload
     * @throws Exception when the send fails
     */
    protected void sendMessage(String token, String content, Map<String, Object> extras) throws Exception {
        if (serverKey == null || serverKey.isBlank()) {
            throw new IllegalStateException("FCM server key is not configured");
        }
        log.debug("FCM send placeholder: projectId={} contentLength={} extras={}", projectId, content.length(), extras);
    }

    @Override
    public boolean isAvailable() {
        return serverKey != null && !serverKey.isBlank();
    }

    private static String configValue(Map<String, Object> config, String key, String defaultValue) {
        if (config == null || !config.containsKey(key)) {
            return defaultValue;
        }
        Object value = config.get(key);
        return value != null ? value.toString() : defaultValue;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> configMap(Map<String, Object> config, String key) {
        if (config == null || !config.containsKey(key)) {
            return Map.of();
        }
        Object value = config.get(key);
        return value instanceof Map<?, ?> map ? (Map<String, Object>) map : Map.of();
    }

    private static String maskToken(String token) {
        if (token == null || token.length() < 8) {
            return "***";
        }
        return token.substring(0, 4) + "***" + token.substring(token.length() - 4);
    }
}
