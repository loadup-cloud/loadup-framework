package io.github.loadup.components.gotone;

import io.github.loadup.components.gotone.model.NotificationRequest;
import io.github.loadup.components.gotone.model.NotificationResponse;

public interface GotoneTemplate {
    NotificationResponse send(NotificationRequest request);
    void sendAsync(NotificationRequest request);
}
