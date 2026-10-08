/*
 * #%L
 * LoadUp Kms
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
package io.github.loadup.components.kms;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/** RSA and AES Transit operations; private key material never returns to the application. */
public final class OpenBaoKmsTemplate implements KmsTemplate, AutoCloseable {
    private final OpenBaoTransitClient client;

    OpenBaoKmsTemplate(OpenBaoTransitClient client) {
        this.client = client;
    }

    @Override
    public KmsCiphertext encrypt(KmsKeyRef key, byte[] plaintext) {
        requireKey(key);
        client.validateInput(plaintext);
        KmsKeyMetadata metadata = metadata(key.name());
        if (!metadata.supportsEncryption()) throw new IllegalArgumentException("Key does not support encryption");
        int version = selectVersion(key, metadata);
        String value = OpenBaoTransitClient.text(
                client.call(
                        "encrypt",
                        key.name(),
                        Map.of("plaintext", Base64.getEncoder().encodeToString(plaintext), "key_version", version),
                        true),
                "ciphertext");
        int actual = OpenBaoTransitClient.version(value);
        if (actual != version) throw new KmsException(KmsException.Code.INVALID_RESPONSE);
        return new KmsCiphertext(key.name(), actual, value);
    }

    @Override
    public byte[] decrypt(KmsKeyRef expectedKey, KmsCiphertext ciphertext) {
        if (ciphertext == null) throw new IllegalArgumentException("ciphertext is required");
        expected(expectedKey, ciphertext.keyName(), ciphertext.version());
        client.validateEncoded(ciphertext.value());
        var result = client.call("decrypt", expectedKey.name(), Map.of("ciphertext", ciphertext.value()), true);
        var plaintext = result.path("plaintext");
        if (!plaintext.isString()) throw new KmsException(KmsException.Code.INVALID_RESPONSE);
        try {
            byte[] decoded = Base64.getDecoder().decode(plaintext.asString());
            client.validateInput(decoded);
            return decoded;
        } catch (IllegalArgumentException invalid) {
            throw new KmsException(KmsException.Code.INVALID_RESPONSE);
        }
    }

    @Override
    public KmsSignature sign(KmsKeyRef key, KmsSignatureAlgorithm algorithm, byte[] message) {
        requireKey(key);
        client.validateInput(message);
        KmsKeyMetadata metadata = metadata(key.name());
        if (!metadata.type().startsWith("rsa-") || !metadata.supportsSigning()) {
            throw new IllegalArgumentException("Key does not support RSA signing");
        }
        Map<String, Object> request = signatureParameters(algorithm, message);
        int version = selectVersion(key, metadata);
        request.put("key_version", version);
        String value = OpenBaoTransitClient.text(client.call("sign", key.name(), request, true), "signature");
        int actual = OpenBaoTransitClient.version(value);
        if (actual != version) throw new KmsException(KmsException.Code.INVALID_RESPONSE);
        try {
            return new KmsSignature(
                    key.name(),
                    actual,
                    algorithm,
                    Base64.getDecoder().decode(value.substring(value.indexOf(':', 7) + 1)));
        } catch (IllegalArgumentException invalid) {
            throw new KmsException(KmsException.Code.INVALID_RESPONSE);
        }
    }

    @Override
    public boolean verify(KmsKeyRef expectedKey, KmsSignature signature, byte[] message) {
        if (signature == null) throw new IllegalArgumentException("signature is required");
        expected(expectedKey, signature.keyName(), signature.version());
        client.validateInput(message);
        var request = signatureParameters(signature.algorithm(), message);
        request.put("signature", signature.transitValue());
        var valid = client.call("verify", expectedKey.name(), request, true).path("valid");
        if (!valid.isBoolean()) throw new KmsException(KmsException.Code.INVALID_RESPONSE);
        return valid.asBoolean();
    }

    @Override
    public KmsKeyMetadata metadata(String name) {
        var data = client.call("metadata", name, null, true);
        if (!name.equals(OpenBaoTransitClient.text(data, "name"))
                || !data.path("latest_version").isIntegralNumber()
                || !data.path("latest_version").canConvertToInt()
                || data.path("latest_version").asInt() < 1
                || !data.path("keys").isObject()) {
            throw new KmsException(KmsException.Code.INVALID_RESPONSE);
        }
        Map<Integer, String> publicKeys = new HashMap<>();
        try {
            for (var entry : data.path("keys").properties()) {
                var publicKey = entry.getValue().path("public_key");
                if (publicKey.isString()) publicKeys.put(Integer.parseInt(entry.getKey()), publicKey.asString());
            }
        } catch (NumberFormatException invalid) {
            throw new KmsException(KmsException.Code.INVALID_RESPONSE);
        }
        return new KmsKeyMetadata(
                name,
                OpenBaoTransitClient.text(data, "type"),
                data.path("latest_version").asInt(),
                data.path("min_encryption_version").asInt(),
                data.path("min_decryption_version").asInt(),
                data.path("supports_encryption").asBoolean(),
                data.path("supports_decryption").asBoolean(),
                data.path("supports_signing").asBoolean(),
                publicKeys);
    }

    @Override
    public String publicKey(KmsKeyRef key) {
        requireKey(key);
        KmsKeyMetadata metadata = metadata(key.name());
        String pem = metadata.publicKeys().get(selectVersion(key, metadata));
        if (pem == null) throw new IllegalArgumentException("Key version has no public key");
        return pem;
    }

    private static Map<String, Object> signatureParameters(KmsSignatureAlgorithm algorithm, byte[] message) {
        if (algorithm == null) throw new IllegalArgumentException("signature algorithm is required");
        Map<String, Object> request = new HashMap<>();
        request.put("input", Base64.getEncoder().encodeToString(message));
        request.put("signature_algorithm", algorithm.transitName());
        request.put("prehashed", false);
        if (algorithm == KmsSignatureAlgorithm.RSA_SHA256_PSS) request.put("salt_length", "hash");
        return request;
    }

    private static int selectVersion(KmsKeyRef key, KmsKeyMetadata metadata) {
        int version = key.version() == 0 ? metadata.latestVersion() : key.version();
        if (version > metadata.latestVersion()) throw new IllegalArgumentException("Key version is not available");
        return version;
    }

    private static void requireKey(KmsKeyRef key) {
        if (key == null) throw new IllegalArgumentException("key is required");
    }

    private static void expected(KmsKeyRef key, String name, int version) {
        requireKey(key);
        if (!key.name().equals(name) || (key.version() != 0 && key.version() != version)) {
            throw new IllegalArgumentException("Signature or ciphertext does not match expected key");
        }
    }

    @Override
    public void close() {
        client.close();
    }
}
