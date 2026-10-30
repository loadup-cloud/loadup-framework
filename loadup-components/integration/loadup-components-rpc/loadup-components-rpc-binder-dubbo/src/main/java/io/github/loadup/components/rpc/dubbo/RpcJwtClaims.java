package io.github.loadup.components.rpc.dubbo;

import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import org.springframework.security.oauth2.jwt.Jwt;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * Rebuilds a {@link Jwt} from the claims the caller propagated.
 *
 * <p>Signature, issuer and audience are verified once at the edge; this internal hop only needs the
 * claim set to build the caller identity. Expiry is still checked so a stale token cannot be
 * replayed indefinitely inside the namespace.
 */
final class RpcJwtClaims {
    private static final TypeReference<Map<String, Object>> MAP = new TypeReference<>() {};
    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    private RpcJwtClaims() {}

    static Jwt parse(String token) {
        String[] parts = token.split("\\.", -1);
        if (parts.length != 3) throw new IllegalArgumentException("Invalid access token");
        Map<String, Object> headers;
        Map<String, Object> claims;
        try {
            headers = MAPPER.readValue(Base64.getUrlDecoder().decode(parts[0]), MAP);
            claims = MAPPER.readValue(Base64.getUrlDecoder().decode(parts[1]), MAP);
        } catch (RuntimeException invalid) {
            throw new IllegalArgumentException("Invalid access token", invalid);
        }
        Instant issuedAt = instant(claims.get("iat"));
        Instant expiresAt = instant(claims.get("exp"));
        if (expiresAt == null || !expiresAt.isAfter(Instant.now()))
            throw new IllegalArgumentException("Expired access token");
        var builder = Jwt.withTokenValue(token)
                .headers(target -> target.putAll(headers))
                .claims(target -> target.putAll(claims))
                .expiresAt(expiresAt);
        if (issuedAt != null) builder.issuedAt(issuedAt);
        return builder.build();
    }

    private static Instant instant(Object value) {
        if (value instanceof Number number) return Instant.ofEpochSecond(number.longValue());
        if (value instanceof String text && !text.isBlank()) {
            try {
                return Instant.ofEpochSecond(Long.parseLong(text));
            } catch (NumberFormatException invalid) {
                throw new IllegalArgumentException("Invalid access token", invalid);
            }
        }
        return null;
    }
}
