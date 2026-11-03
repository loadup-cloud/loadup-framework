package io.github.loadup.components.authserver.sas;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import io.github.loadup.components.authserver.properties.LoadUpAuthServerProperties.Signing;
import io.github.loadup.components.authserver.properties.LoadUpAuthServerProperties.SigningKey;
import io.github.loadup.components.kms.KmsKeyRef;
import io.github.loadup.components.kms.KmsTemplate;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import org.springframework.security.converter.RsaKeyConverters;

/** Pinned signing identity and public-only JWK snapshot loaded at startup. */
final class KmsSigningKeys {
    private final KmsKeyRef activeKey;
    private final String activeKid;
    private final JWKSet publicKeys;

    KmsSigningKeys(KmsTemplate kms, Signing signing) {
        if (signing == null || signing.getVerificationKeys() == null) {
            throw new IllegalArgumentException("Authorization signing configuration is required");
        }
        activeKey = reference(signing.getActiveKey());
        activeKid = kid(activeKey);
        var metadata = kms.metadata(activeKey.name());
        if (!activeKey.name().equals(metadata.name())
                || !metadata.supportsSigning()
                || activeKey.version() > metadata.latestVersion()) {
            throw new IllegalArgumentException("Authorization active key must support signing at the pinned version");
        }
        var keys = new ArrayList<JWK>();
        keys.add(publicKey(kms, activeKey));
        var seen = new HashSet<String>();
        seen.add(activeKid);
        for (SigningKey retained : signing.getVerificationKeys()) {
            var reference = reference(retained);
            if (!seen.add(kid(reference))) {
                throw new IllegalArgumentException("Duplicate authorization signing/verification key");
            }
            keys.add(publicKey(kms, reference));
        }
        publicKeys = new JWKSet(keys);
    }

    private static KmsKeyRef reference(SigningKey key) {
        if (key == null || key.getVersion() < 1) {
            throw new IllegalArgumentException("Authorization keys require an explicit name and positive version");
        }
        return new KmsKeyRef(key.getName(), key.getVersion());
    }

    private static String kid(KmsKeyRef key) {
        return key.name() + "-v" + key.version();
    }

    private static RSAKey publicKey(KmsTemplate kms, KmsKeyRef key) {
        var pem = kms.publicKey(key);
        var rsa = RsaKeyConverters.x509()
                .convert(new ByteArrayInputStream(pem.getBytes(StandardCharsets.US_ASCII)));
        if (rsa.getModulus().bitLength() < 2048 || rsa.getModulus().bitLength() > 4096) {
            throw new IllegalArgumentException("Authorization signing requires RSA-2048 through RSA-4096");
        }
        return new RSAKey.Builder(rsa)
                .keyID(kid(key))
                .keyUse(KeyUse.SIGNATURE)
                .algorithm(JWSAlgorithm.RS256)
                .build();
    }

    KmsKeyRef activeKey() {
        return activeKey;
    }

    String activeKid() {
        return activeKid;
    }

    JWKSet publicKeys() {
        return publicKeys;
    }
}
