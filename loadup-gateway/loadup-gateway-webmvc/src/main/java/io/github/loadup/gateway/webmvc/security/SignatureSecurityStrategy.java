package io.github.loadup.gateway.webmvc.security;

import io.github.loadup.gateway.facade.config.GatewayProperties;
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
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HMAC-SHA256 signature security strategy (security code {@code signature}).
 */
public class SignatureSecurityStrategy implements SecurityStrategy {
    private static final Logger log = LoggerFactory.getLogger(SignatureSecurityStrategy.class);

    private static final String HEADER_APP_ID = "X-App-Id";
    private static final String HEADER_TIMESTAMP = "X-Timestamp";
    private static final String HEADER_NONCE = "X-Nonce";
    private static final String HEADER_SIGNATURE = "X-Signature";
    private static final long TIMESTAMP_TOLERANCE_SECONDS = 300;

    private final Map<String, String> appSecrets;

    public SignatureSecurityStrategy(GatewayProperties gatewayProperties) {
        this.appSecrets = loadSecrets(gatewayProperties);
    }

    private static Map<String, String> loadSecrets(GatewayProperties props) {
        if (props != null && props.getSecurity() != null && props.getSecurity().getAppSecrets() != null) {
            return new HashMap<>(props.getSecurity().getAppSecrets());
        }
        log.warn("No signature app secrets configured — signature verification will fail for all requests");
        return Map.of();
    }

    @Override
    public String getCode() {
        return "signature";
    }

    @Override
    public void process(GatewayContext context) {
        GatewayRequest request = context.getRequest();
        String appId = getHeader(request, HEADER_APP_ID);
        String timestamp = getHeader(request, HEADER_TIMESTAMP);
        String nonce = getHeader(request, HEADER_NONCE);
        String clientSignature = getHeader(request, HEADER_SIGNATURE);

        if (StringUtils.isAnyBlank(appId, timestamp, nonce, clientSignature)) {
            throw GatewayExceptionFactory.unauthorized("Missing signature headers");
        }

        try {
            long ts = Long.parseLong(timestamp);
            if (Math.abs(System.currentTimeMillis() / 1000 - ts) > TIMESTAMP_TOLERANCE_SECONDS) {
                throw GatewayExceptionFactory.unauthorized("Timestamp expired");
            }
        } catch (NumberFormatException e) {
            throw GatewayExceptionFactory.unauthorized("Invalid timestamp format");
        }

        String appSecret = appSecrets.get(appId);
        if (appSecret == null) {
            throw GatewayExceptionFactory.unauthorized("Unknown app ID: " + appId);
        }

        Map<String, String> params = flattenQueryParams(request.getQueryParameters());
        String serverSignature = calculateSignature(params, timestamp, nonce, appSecret);

        if (!serverSignature.equalsIgnoreCase(clientSignature)) {
            log.warn("Signature mismatch for app={}", appId);
            throw GatewayExceptionFactory.unauthorized("Invalid signature");
        }

        request.getAttributes().put("appId", appId);
        request.getHeaders().put("X-App-Id", appId);
    }

    private String getHeader(GatewayRequest request, String name) {
        String value = request.getHeaders().get(name);
        if (value == null) {
            value = request.getHeaders().entrySet().stream()
                    .filter(e -> e.getKey().equalsIgnoreCase(name))
                    .map(Map.Entry::getValue)
                    .findFirst()
                    .orElse(null);
        }
        return value;
    }

    private Map<String, String> flattenQueryParams(Map<String, List<String>> params) {
        Map<String, String> result = new HashMap<>();
        if (params != null) {
            params.forEach((k, v) -> {
                if (v != null && !v.isEmpty()) {
                    result.put(k, v.get(0));
                }
            });
        }
        return result;
    }

    private String calculateSignature(Map<String, String> params, String timestamp, String nonce, String secret) {
        try {
            TreeMap<String, String> sorted = new TreeMap<>(params);
            StringBuilder sb = new StringBuilder();
            sorted.forEach((k, v) ->
                    sb.append(sb.isEmpty() ? "" : "&").append(k).append("=").append(v));
            if (!sb.isEmpty()) {
                sb.append("&");
            }
            sb.append("timestamp=").append(timestamp).append("&nonce=").append(nonce);

            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(sb.toString().getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            log.error("Signature calculation failed", e);
            throw GatewayExceptionFactory.systemError("Signature calculation failed");
        }
    }
}
