package io.github.loadup.gateway.webmvc.security;

import io.github.loadup.gateway.webmvc.config.GatewayProperties;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.web.servlet.function.ServerRequest;

/** Verifies a bounded HMAC request and rejects nonce reuse on this node. */
public final class RequestSignatureVerifier {
    private static final long MAX_SKEW_SECONDS = 300;
    private final Map<String, String> appSecrets;
    private final int maxBodyBytes;
    private final SignatureNonceStore nonces;

    public RequestSignatureVerifier(GatewayProperties properties, SignatureNonceStore nonces) {
        GatewayProperties.Security security = properties.getSecurity();
        this.appSecrets = security.getAppSecrets() == null ? Map.of() : Map.copyOf(security.getAppSecrets());
        this.maxBodyBytes = security.getMaxSignedBodyBytes();
        this.nonces = nonces;
        if (maxBodyBytes < 1 || maxBodyBytes == Integer.MAX_VALUE) {
            throw new IllegalArgumentException("max-signed-body-bytes must be between 1 and 2147483646");
        }
    }

    public boolean isConfigured() {
        return !appSecrets.isEmpty();
    }

    public ServerRequest verify(ServerRequest request, boolean httpTarget) throws Exception {
        String appId = request.headers().firstHeader("X-App-Id");
        String timestamp = request.headers().firstHeader("X-Timestamp");
        String nonce = request.headers().firstHeader("X-Nonce");
        String signature = request.headers().firstHeader("X-Signature");
        if (blank(appId) || blank(timestamp) || blank(nonce) || blank(signature) || nonce.length() > 128) {
            return null;
        }
        String secret = appSecrets.get(appId);
        if (blank(secret)) {
            return null;
        }
        long seconds;
        try {
            seconds = Long.parseLong(timestamp);
        } catch (NumberFormatException e) {
            return null;
        }
        long now = System.currentTimeMillis() / 1000;
        if (seconds < now - MAX_SKEW_SECONDS || seconds > now + MAX_SKEW_SECONDS) {
            return null;
        }
        byte[] body = request.servletRequest().getInputStream().readNBytes(maxBodyBytes + 1);
        if (body.length > maxBodyBytes) {
            throw new SignedBodyTooLargeException();
        }
        String rawPathAndQuery = request.servletRequest().getRequestURI();
        String query = request.servletRequest().getQueryString();
        if (query != null) {
            rawPathAndQuery += "?" + query;
        }
        String canonical = request.method().name() + "\n" + rawPathAndQuery + "\n" + timestamp + "\n" + nonce + "\n"
                + HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(body));
        byte[] expected = hmac(canonical, secret);
        byte[] supplied;
        try {
            supplied = HexFormat.of().parseHex(signature);
        } catch (IllegalArgumentException e) {
            return null;
        }
        if (!MessageDigest.isEqual(expected, supplied)) {
            return null;
        }
        if (!nonces.reserve(appId, nonce)) {
            return null;
        }
        if (httpTarget) {
            ServerRequest replayable = ServerRequest.create(
                    new ReplayableRequest(request.servletRequest(), body),
                    java.util.List.of(new ByteArrayHttpMessageConverter()));
            return ServerRequest.from(replayable)
                    .uri(request.uri())
                    .headers(headers -> {
                        headers.remove("X-App-Id");
                        headers.remove("X-Timestamp");
                        headers.remove("X-Nonce");
                        headers.remove("X-Signature");
                    })
                    .body(body)
                    .build();
        }
        return ServerRequest.from(request).body(body).build();
    }

    private static byte[] hmac(String canonical, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return mac.doFinal(canonical.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException("Cannot verify request signature", e);
        }
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    public static final class SignedBodyTooLargeException extends RuntimeException {}

    private static final class ReplayableRequest extends HttpServletRequestWrapper {
        private final byte[] body;

        private ReplayableRequest(HttpServletRequest request, byte[] body) {
            super(request);
            this.body = body;
        }

        @Override
        public ServletInputStream getInputStream() {
            ByteArrayInputStream input = new ByteArrayInputStream(body);
            return new ServletInputStream() {
                @Override
                public int read() {
                    return input.read();
                }

                @Override
                public boolean isFinished() {
                    return input.available() == 0;
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setReadListener(ReadListener listener) {
                    throw new UnsupportedOperationException("Async signed request bodies are not supported");
                }
            };
        }

        @Override
        public BufferedReader getReader() {
            return new BufferedReader(new InputStreamReader(new ByteArrayInputStream(body), StandardCharsets.UTF_8));
        }

        @Override
        public int getContentLength() {
            return body.length;
        }

        @Override
        public long getContentLengthLong() {
            return body.length;
        }
    }
}
