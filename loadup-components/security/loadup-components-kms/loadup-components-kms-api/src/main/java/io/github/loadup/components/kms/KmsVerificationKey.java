/*
 * Copyright (C) 2026 LoadUp Cloud
 *
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
 */
package io.github.loadup.components.kms;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Objects;

/** A trusted RSA verification key resolved by the selected binder. */
public record KmsVerificationKey(KmsKeyRef actual, RSAPublicKey publicKey) {
    public KmsVerificationKey {
        Objects.requireNonNull(actual);
        if (publicKey == null
                || publicKey.getModulus().bitLength() < 2048
                || publicKey.getModulus().bitLength() > 4096) {
            throw new KmsException(KmsException.Code.INVALID_RESPONSE);
        }
    }

    public String fingerprint() {
        return fingerprint(publicKey);
    }

    public static KmsVerificationKey fromPem(KmsKeyRef actual, String pem) {
        if (pem == null) {
            throw new KmsException(KmsException.Code.INVALID_RESPONSE);
        }
        try {
            var encoded = Base64.getDecoder()
                    .decode(pem.replace("-----BEGIN PUBLIC KEY-----", "")
                            .replace("-----END PUBLIC KEY-----", "")
                            .replaceAll("\\s+", ""));
            var rsa = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(encoded));
            return new KmsVerificationKey(actual, rsa);
        } catch (GeneralSecurityException | IllegalArgumentException failure) {
            throw new KmsException(KmsException.Code.INVALID_RESPONSE);
        }
    }

    public static String fingerprint(RSAPublicKey key) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(sha256(key.getEncoded()));
    }

    private static byte[] sha256(byte[] input) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(input);
        } catch (GeneralSecurityException impossible) {
            throw new IllegalStateException(impossible);
        }
    }

    public static boolean verify(RSAPublicKey key, KmsSignatureAlgorithm algorithm, byte[] message, byte[] signature) {
        try {
            if (signature.length != (key.getModulus().bitLength() + 7) / 8) {
                return false;
            }
            var verifier =
                    switch (algorithm) {
                        case RSA_SHA256_PKCS1 -> Signature.getInstance("SHA256withRSA");
                        case RSA_SHA256_PSS -> Signature.getInstance("RSASSA-PSS");
                    };
            if (algorithm == KmsSignatureAlgorithm.RSA_SHA256_PSS) {
                verifier.setParameter(new PSSParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, 32, 1));
            }
            verifier.initVerify(key);
            verifier.update(message);
            return verifier.verify(signature);
        } catch (SignatureException invalid) {
            return false;
        } catch (GeneralSecurityException failure) {
            throw new IllegalStateException("RSA verification unavailable", failure);
        }
    }
}
