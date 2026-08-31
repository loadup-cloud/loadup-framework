package io.github.loadup.components.signature.util;

/*-
 * #%L
 * LoadUp Components :: Signature
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

import io.github.loadup.components.signature.enums.SignatureAlgorithm;
import io.github.loadup.components.signature.exception.SignatureException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Static asymmetric sign / verify helpers backed by JCA {@link Signature}. */
public final class SignatureUtils {
    private static final Logger log = LoggerFactory.getLogger(SignatureUtils.class);

    private SignatureUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Quick RSA signing with SHA256withRSA.
     *
     * @param data the input string
     * @param privateKeyBase64 the Base64-encoded RSA private key
     * @return the Base64-encoded signature
     */
    public static String signRSA(String data, String privateKeyBase64) {
        return sign(data, privateKeyBase64, SignatureAlgorithm.SHA256_WITH_RSA);
    }

    /**
     * Quick RSA verification with SHA256withRSA.
     *
     * @param data the original string
     * @param signature the Base64-encoded signature
     * @param publicKeyBase64 the Base64-encoded RSA public key
     * @return {@code true} when the signature is valid
     */
    public static boolean verifyRSA(String data, String signature, String publicKeyBase64) {
        return verify(data, signature, publicKeyBase64, SignatureAlgorithm.SHA256_WITH_RSA);
    }

    /**
     * Signs the given string with the given algorithm.
     *
     * @param data the input string
     * @param privateKeyBase64 the Base64-encoded private key
     * @param algorithm the signature algorithm
     * @return the Base64-encoded signature
     */
    public static String sign(String data, String privateKeyBase64, SignatureAlgorithm algorithm) {
        try {
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            PrivateKey privateKey = loadPrivateKey(privateKeyBase64, algorithm.getKeyAlgorithm());

            Signature signature = Signature.getInstance(algorithm.getJcaName());
            signature.initSign(privateKey);
            signature.update(dataBytes);
            byte[] signatureBytes = signature.sign();

            return Base64.getEncoder().encodeToString(signatureBytes);
        } catch (Exception e) {
            log.error("Sign failed: algorithm={}, error={}", algorithm, e.getMessage(), e);
            throw new SignatureException(
                    SignatureException.SignatureErrorCode.SIGN_FAILED, "Sign failed: " + e.getMessage(), e);
        }
    }

    /**
     * Verifies the signature with the given algorithm.
     *
     * @param data the original string
     * @param signatureBase64 the Base64-encoded signature
     * @param publicKeyBase64 the Base64-encoded public key
     * @param algorithm the signature algorithm
     * @return {@code true} when the signature is valid
     */
    public static boolean verify(
            String data, String signatureBase64, String publicKeyBase64, SignatureAlgorithm algorithm) {
        try {
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            PublicKey publicKey = loadPublicKey(publicKeyBase64, algorithm.getKeyAlgorithm());

            Signature signature = Signature.getInstance(algorithm.getJcaName());
            signature.initVerify(publicKey);
            signature.update(dataBytes);
            byte[] signatureBytes = Base64.getDecoder().decode(signatureBase64);

            return signature.verify(signatureBytes);
        } catch (Exception e) {
            log.error("Verify failed: algorithm={}, error={}", algorithm, e.getMessage(), e);
            throw new SignatureException(
                    SignatureException.SignatureErrorCode.VERIFY_FAILED, "Verify failed: " + e.getMessage(), e);
        }
    }

    /**
     * Loads a private key from its Base64-encoded PKCS#8 representation.
     */
    private static PrivateKey loadPrivateKey(String base64PrivateKey, String keyAlgorithm)
            throws GeneralSecurityException {
        byte[] keyBytes = Base64.getDecoder().decode(base64PrivateKey);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(keyAlgorithm);
        return keyFactory.generatePrivate(keySpec);
    }

    /**
     * Loads a public key from its Base64-encoded X.509 representation.
     */
    private static PublicKey loadPublicKey(String base64PublicKey, String keyAlgorithm)
            throws GeneralSecurityException {
        byte[] keyBytes = Base64.getDecoder().decode(base64PublicKey);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(keyAlgorithm);
        return keyFactory.generatePublic(keySpec);
    }
}
