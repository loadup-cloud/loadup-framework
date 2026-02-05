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
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * HMAC-SHA256 Signature Security Strategy
 *
 * <p>Validates request signature to prevent tampering.</p>
 *
 * <p>Expected headers:</p>
 * <ul>
 *   <li>X-App-Id: Application identifier</li>
 *   <li>X-Timestamp: Unix timestamp (seconds)</li>
 *   <li>X-Nonce: Random string for replay protection</li>
 *   <li>X-Signature: HMAC-SHA256(sorted params + timestamp + nonce)</li>
 * </ul>
 *
 * <p>Signature calculation:</p>
 * <pre>
 * String signStr = sortedParams + timestamp + nonce;
 * String signature = HMAC-SHA256(signStr, appSecret);
 * </pre>
 */
@Slf4j
@Component
public class SignatureSecurityStrategy implements SecurityStrategy {

    private static final String HEADER_APP_ID = "X-App-Id";
    private static final String HEADER_TIMESTAMP = "X-Timestamp";
    private static final String HEADER_NONCE = "X-Nonce";
    private static final String HEADER_SIGNATURE = "X-Signature";

    // TODO: Load from database or config
    private static final Map<String, String> APP_SECRETS = Map.of(
            "test-app-001", "test-secret-key-001",
            "test-app-002", "test-secret-key-002");

    private static final long TIMESTAMP_TOLERANCE_SECONDS = 300; // 5 minutes

    @Override
    public String getCode() {
        return "signature";
    }

    @Override
    public void process(GatewayContext context) {
        GatewayRequest request = context.getRequest();

        // 1. Extract headers
        String appId = getHeader(request, HEADER_APP_ID);
        String timestamp = getHeader(request, HEADER_TIMESTAMP);
        String nonce = getHeader(request, HEADER_NONCE);
        String clientSignature = getHeader(request, HEADER_SIGNATURE);

        if (StringUtils.isAnyBlank(appId, timestamp, nonce, clientSignature)) {
            throw GatewayExceptionFactory.unauthorized("Missing signature headers");
        }

        // 2. Validate timestamp (prevent replay attack)
        try {
            long ts = Long.parseLong(timestamp);
            long now = System.currentTimeMillis() / 1000;
            if (Math.abs(now - ts) > TIMESTAMP_TOLERANCE_SECONDS) {
                throw GatewayExceptionFactory.unauthorized("Timestamp expired");
            }
        } catch (NumberFormatException e) {
            throw GatewayExceptionFactory.unauthorized("Invalid timestamp format");
        }

        // 3. Get app secret
        String appSecret = APP_SECRETS.get(appId);
        if (appSecret == null) {
            throw GatewayExceptionFactory.unauthorized("Unknown app ID");
        }

        // 4. Calculate server signature
        Map<String, String> params = flattenQueryParams(request.getQueryParameters());
        String serverSignature = calculateSignature(params, timestamp, nonce, appSecret);

        // 5. Compare signatures
        if (!serverSignature.equalsIgnoreCase(clientSignature)) {
            log.warn("Signature mismatch: expected={}, actual={}", serverSignature, clientSignature);
            throw GatewayExceptionFactory.unauthorized("Invalid signature");
        }

        log.debug("Signature validated for app: {}", appId);

        // 6. Store app info in context
        request.getAttributes().put("appId", appId);
        request.getHeaders().put("X-App-Id", appId);
    }

    private String getHeader(GatewayRequest request, String headerName) {
        String value = request.getHeaders().get(headerName);
        if (value == null) {
            // Try case-insensitive
            value = request.getHeaders().entrySet().stream()
                    .filter(e -> e.getKey().equalsIgnoreCase(headerName))
                    .map(Map.Entry::getValue)
                    .findFirst()
                    .orElse(null);
        }
        return value;
    }

    /**
     * Flatten query parameters from Map<String, List<String>> to Map<String, String>
     */
    private Map<String, String> flattenQueryParams(Map<String, List<String>> queryParams) {
        Map<String, String> result = new HashMap<>();
        if (queryParams != null) {
            for (Map.Entry<String, List<String>> entry : queryParams.entrySet()) {
                List<String> values = entry.getValue();
                if (values != null && !values.isEmpty()) {
                    result.put(entry.getKey(), values.get(0)); // Take first value
                }
            }
        }
        return result;
    }

    /**
     * Calculate HMAC-SHA256 signature
     */
    private String calculateSignature(Map<String, String> params, String timestamp, String nonce, String secret) {
        try {
            // Sort parameters by key
            TreeMap<String, String> sortedParams = new TreeMap<>(params);

            // Build sign string: key1=value1&key2=value2&timestamp=xxx&nonce=xxx
            StringBuilder signStr = new StringBuilder();
            for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
                if (!signStr.isEmpty()) {
                    signStr.append("&");
                }
                signStr.append(entry.getKey()).append("=").append(entry.getValue());
            }
            if (!signStr.isEmpty()) {
                signStr.append("&");
            }
            signStr.append("timestamp=").append(timestamp);
            signStr.append("&nonce=").append(nonce);

            // HMAC-SHA256
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(signStr.toString().getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            log.error("Failed to calculate signature", e);
            throw GatewayExceptionFactory.systemError("Signature calculation failed");
        }
    }
}
