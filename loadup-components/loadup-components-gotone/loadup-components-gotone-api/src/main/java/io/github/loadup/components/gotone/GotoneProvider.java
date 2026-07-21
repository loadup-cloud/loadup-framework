package io.github.loadup.components.gotone;

import io.github.loadup.components.gotone.model.ChannelSendRequest;
import io.github.loadup.components.gotone.model.ChannelSendResponse;

public interface GotoneProvider {
    String getChannelType();
    String getProviderName();
    ChannelSendResponse send(ChannelSendRequest request);
    boolean isAvailable();
}
