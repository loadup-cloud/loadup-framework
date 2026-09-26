package io.github.loadup.gateway.test.webmvcapp;

import io.github.loadup.gateway.api.GatewayExpose;

public interface DemoContract {
    @GatewayExpose
    String reply(String id);
}
