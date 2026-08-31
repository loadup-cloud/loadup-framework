/*-
 * #%L
 * Loadup Gateway WebMVC Engine
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
package io.github.loadup.gateway.webmvc.security;

import io.github.loadup.gateway.facade.context.GatewayContext;
import io.github.loadup.gateway.facade.exception.GatewayExceptionFactory;
import io.github.loadup.gateway.facade.model.GatewayRequest;
import io.github.loadup.gateway.facade.spi.SecurityStrategy;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Internal-call-only security strategy (security code {@code internal}).
 */
public class InternalSecurityStrategy implements SecurityStrategy {
    private static final Logger log = LoggerFactory.getLogger(InternalSecurityStrategy.class);

    private static final String HEADER_INTERNAL_CALL = "X-Internal-Call";
    private static final String[] INTERNAL_IP_PREFIXES = {
        "10.", "172.16.", "172.17.", "172.18.", "172.19.", "172.20.", "172.21.", "172.22.",
        "172.23.", "172.24.", "172.25.", "172.26.", "172.27.", "172.28.", "172.29.", "172.30.",
        "172.31.", "192.168."
    };

    @Override
    public String getCode() {
        return "internal";
    }

    @Override
    public void process(GatewayContext context) {
        GatewayRequest request = context.getRequest();

        if ("true".equalsIgnoreCase(getHeader(request, HEADER_INTERNAL_CALL))) {
            return;
        }

        String clientIp = request.getClientIp();
        if (isInternalIp(clientIp)) {
            return;
        }

        log.warn("Rejecting non-internal call from IP: {}", clientIp);
        throw GatewayExceptionFactory.forbidden("Only internal calls are allowed");
    }

    private String getHeader(GatewayRequest request, String headerName) {
        String value = request.getHeaders().get(headerName);
        if (value == null) {
            value = request.getHeaders().entrySet().stream()
                    .filter(e -> e.getKey().equalsIgnoreCase(headerName))
                    .map(java.util.Map.Entry::getValue)
                    .findFirst()
                    .orElse(null);
        }
        return value;
    }

    private boolean isInternalIp(String ip) {
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            return false;
        }
        if ("127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
            return true;
        }
        for (String prefix : INTERNAL_IP_PREFIXES) {
            if (ip.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
}
