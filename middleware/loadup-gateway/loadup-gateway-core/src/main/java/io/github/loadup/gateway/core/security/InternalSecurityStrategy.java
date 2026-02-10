package io.github.loadup.gateway.core.security;

/*-
 * #%L
 * LoadUp Gateway Core
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import io.github.loadup.gateway.facade.context.GatewayContext;
import io.github.loadup.gateway.facade.exception.GatewayExceptionFactory;
import io.github.loadup.gateway.facade.model.GatewayRequest;
import io.github.loadup.gateway.facade.spi.SecurityStrategy;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Internal Call Security Strategy
 *
 * <p>Validates that the request comes from a trusted internal source.</p>
 *
 * <p>Validation methods:</p>
 * <ul>
 *   <li>IP whitelist (127.0.0.1, 10.*, 172.16-31.*, 192.168.*)</li>
 *   <li>Internal header check (X-Internal-Call: true)</li>
 *   <li>Localhost detection</li>
 * </ul>
 *
 * <p>Use case: Service-to-service calls within the same application or trusted network.</p>
 */
@Slf4j
public class InternalSecurityStrategy implements SecurityStrategy {

    private static final String HEADER_INTERNAL_CALL = "X-Internal-Call";
    private static final List<String> INTERNAL_IP_PREFIXES = Arrays.asList(
            "127.", // Localhost
            "10.", // Private Class A
            "172.16.", // Private Class B
            "172.17.",
            "172.18.",
            "172.19.",
            "172.20.",
            "172.21.",
            "172.22.",
            "172.23.",
            "172.24.",
            "172.25.",
            "172.26.",
            "172.27.",
            "172.28.",
            "172.29.",
            "172.30.",
            "172.31.",
            "192.168." // Private Class C
            );

    @Override
    public String getCode() {
        return "internal";
    }

    @Override
    public void process(GatewayContext context) {
        GatewayRequest request = context.getRequest();

        // 1. Check internal header
        String internalHeader = getHeader(request, HEADER_INTERNAL_CALL);
        if ("true".equalsIgnoreCase(internalHeader)) {
            log.debug("Internal call validated via header");
            return;
        }

        // 2. Check IP address
        String clientIp = getClientIp(request);
        if (isInternalIp(clientIp)) {
            log.debug("Internal call validated via IP: {}", clientIp);
            return;
        }

        // 3. Reject if not internal
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

    private String getClientIp(GatewayRequest request) {
        // Try common proxy headers first
        String ip = getHeader(request, "X-Forwarded-For");
        if (StringUtils.isNotBlank(ip) && !"unknown".equalsIgnoreCase(ip)) {
            // X-Forwarded-For may contain multiple IPs
            int index = ip.indexOf(',');
            if (index != -1) {
                ip = ip.substring(0, index);
            }
            return ip.trim();
        }

        ip = getHeader(request, "X-Real-IP");
        if (StringUtils.isNotBlank(ip) && !"unknown".equalsIgnoreCase(ip)) {
            return ip.trim();
        }

        // Fallback to servlet request
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest servletRequest = attributes.getRequest();
                return servletRequest.getRemoteAddr();
            }
        } catch (Exception e) {
            log.debug("Failed to get client IP from servlet request", e);
        }

        return "unknown";
    }

    private boolean isInternalIp(String ip) {
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            return false;
        }

        // Exact match for localhost
        if ("127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
            return true;
        }

        // Check prefixes
        for (String prefix : INTERNAL_IP_PREFIXES) {
            if (ip.startsWith(prefix)) {
                return true;
            }
        }

        return false;
    }
}
