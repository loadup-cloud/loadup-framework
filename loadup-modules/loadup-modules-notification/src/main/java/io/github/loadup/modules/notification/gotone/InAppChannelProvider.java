package io.github.loadup.modules.notification.gotone;

import io.github.loadup.commons.log.LogUtil;
import io.github.loadup.components.gotone.NotificationChannelProvider;
import io.github.loadup.components.gotone.model.ChannelSendRequest;
import io.github.loadup.components.gotone.model.ChannelSendResponse;
import io.github.loadup.modules.notification.InboxService;
import java.util.LinkedHashMap;
import java.util.Map;

/** Gotone IN_APP channel backed by the persistent inbox. */
public class InAppChannelProvider implements NotificationChannelProvider {

    private final InboxService inbox;

    public InAppChannelProvider(InboxService inbox) {
        this.inbox = inbox;
    }

    @Override
    public String getChannelType() {
        return "IN_APP";
    }

    @Override
    public String getProviderName() {
        return "inbox";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public ChannelSendResponse send(ChannelSendRequest request) {
        Map<String, Object> params = request.templateParams();
        String title = string(params, "title");
        String body = request.content().isBlank() ? string(params, "body") : request.content();
        try {
            inbox.publish(
                    string(params, "tenantId"),
                    string(params, "senderId"),
                    request.receivers(),
                    string(params, "category"),
                    title,
                    body,
                    string(params, "actionUrl"),
                    string(params, "requestKey"));
            Map<String, Boolean> status = new LinkedHashMap<>();
            request.receivers().forEach(receiver -> status.put(receiver, true));
            return new ChannelSendResponse(body, status.size(), 0, status, Map.of());
        } catch (RuntimeException failure) {
            LogUtil.warn(InAppChannelProvider.class, "In-app notification delivery failed", failure);
            Map<String, Boolean> status = new LinkedHashMap<>();
            Map<String, String> errors = new LinkedHashMap<>();
            request.receivers().forEach(receiver -> {
                status.put(receiver, false);
                errors.put(receiver, "in-app delivery failed");
            });
            return new ChannelSendResponse(body, 0, status.size(), status, errors);
        }
    }

    private static String string(Map<String, Object> values, String key) {
        Object value = values.get(key);
        return value == null ? null : String.valueOf(value);
    }
}
