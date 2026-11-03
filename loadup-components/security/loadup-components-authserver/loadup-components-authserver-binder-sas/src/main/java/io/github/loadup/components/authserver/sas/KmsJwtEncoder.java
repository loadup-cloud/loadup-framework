package io.github.loadup.components.authserver.sas;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.jca.JCAContext;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import io.github.loadup.components.kms.KmsSignatureAlgorithm;
import io.github.loadup.components.kms.KmsTemplate;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtEncodingException;

/**
 * Uses Nimbus JOSE serialization while keeping all private-key operations behind KMS.
 */
@NullMarked
final class KmsJwtEncoder implements JwtEncoder {
    private static final Set<String> TIMESTAMPS = Set.of("iat", "nbf", "exp");
    private final KmsSigningKeys keys;
    private final JWSSigner signer;

    KmsJwtEncoder(KmsTemplate kms, KmsSigningKeys keys) {
        this.keys = keys;
        signer = new JWSSigner() {
            private final JCAContext context = new JCAContext();

            @Override
            public Set<JWSAlgorithm> supportedJWSAlgorithms() {
                return Set.of(JWSAlgorithm.RS256);
            }

            @Override
            public JCAContext getJCAContext() {
                return context;
            }

            @Override
            public Base64URL sign(JWSHeader header, byte[] signingInput) throws JOSEException {
                if (!JWSAlgorithm.RS256.equals(header.getAlgorithm())
                        || !keys.activeKid().equals(header.getKeyID())
                        || !header.isBase64URLEncodePayload()
                        || (header.getCriticalParams() != null
                                && !header.getCriticalParams().isEmpty())) {
                    throw new JOSEException("Unsupported authorization token header");
                }
                var signature = kms.sign(keys.activeKey(), KmsSignatureAlgorithm.RSA_SHA256_PKCS1, signingInput);
                if (!keys.activeKey().name().equals(signature.keyName())
                        || keys.activeKey().version() != signature.version()
                        || signature.algorithm() != KmsSignatureAlgorithm.RSA_SHA256_PKCS1) {
                    throw new JOSEException("KMS returned an unexpected signing identity");
                }
                return Base64URL.encode(signature.bytes());
            }
        };
    }

    @Override
    public Jwt encode(JwtEncoderParameters parameters) throws JwtEncodingException {
        var supplied = parameters.getJwsHeader();
        if (supplied != null
                && (!SignatureAlgorithm.RS256.equals(supplied.getAlgorithm())
                        || (supplied.getKeyId() != null && !keys.activeKid().equals(supplied.getKeyId())))) {
            throw new JwtEncodingException("Authorization tokens require RS256 and the active KMS key");
        }
        var headers = (supplied == null ? JwsHeader.with(SignatureAlgorithm.RS256) : JwsHeader.from(supplied))
                .keyId(keys.activeKid())
                .build();
        var claims = parameters.getClaims();
        try {
            var builder = new JWTClaimsSet.Builder();
            claims.getClaims()
                    .forEach((name, value) -> builder.claim(
                            name,
                            TIMESTAMPS.contains(name)
                                    ? Date.from(Objects.requireNonNull(claims.getClaimAsInstant(name)))
                                    : value));
            // Spring stores alg as a JwsAlgorithm; Nimbus requires its JSON string representation.
            var joseHeaders = new HashMap<String, Object>(headers.getHeaders());
            joseHeaders.put("alg", headers.getAlgorithm().getName());
            for (String name : Set.of("jku", "x5u")) {
                if (joseHeaders.get(name) instanceof java.net.URI uri) {
                    joseHeaders.put(name, uri.toString());
                }
            }
            var token = new SignedJWT(JWSHeader.parse(joseHeaders), builder.build());
            token.sign(signer);
            return new Jwt(
                    token.serialize(),
                    claims.getIssuedAt(),
                    claims.getExpiresAt(),
                    token.getHeader().toJSONObject(),
                    claims.getClaims());
        } catch (JOSEException | ParseException | RuntimeException failure) {
            // Providers may include payloads in exception messages; do not propagate their details.
            throw new JwtEncodingException("KMS authorization token signing failed");
        }
    }
}
