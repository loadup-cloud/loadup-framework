package io.github.loadup.gateway.webmvc.security;

import io.github.loadup.gateway.facade.context.GatewayContext;
import io.github.loadup.gateway.facade.spi.SecurityStrategy;

/**
 * No-op security strategy used for the {@code OFF} security code.
 */
public class OffSecurityStrategy implements SecurityStrategy {

    @Override
    public String getCode() {
        return "OFF";
    }

    @Override
    public void process(GatewayContext context) {
        // no security check
    }
}
