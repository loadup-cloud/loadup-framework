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
package io.github.loadup.components.kms.aliyun;

import static io.github.loadup.components.kms.aliyun.AliyunKmsRequests.cloud;
import static io.github.loadup.components.kms.aliyun.AliyunKmsRequests.requireResponse;

import com.aliyun.kms20160120.Client;
import com.aliyun.kms20160120.models.AsymmetricSignRequest;
import com.aliyun.kms20160120.models.AsymmetricVerifyRequest;
import com.aliyun.kms20160120.models.DecryptRequest;
import com.aliyun.kms20160120.models.DescribeKeyRequest;
import com.aliyun.kms20160120.models.DescribeKeyResponseBody.DescribeKeyResponseBodyKeyMetadata;
import com.aliyun.kms20160120.models.EncryptRequest;
import com.aliyun.kms20160120.models.GetPublicKeyRequest;
import com.aliyun.kms20160120.models.ListKeyVersionsRequest;
import com.aliyun.kms20160120.models.ListKeysRequest;
import com.aliyun.kms20160120.models.ListResourceTagsRequest;
import io.github.loadup.components.kms.KmsCiphertext;
import io.github.loadup.components.kms.KmsException;
import io.github.loadup.components.kms.KmsException.Code;
import io.github.loadup.components.kms.KmsKeyMetadata;
import io.github.loadup.components.kms.KmsKeyRef;
import io.github.loadup.components.kms.KmsKeyType;
import io.github.loadup.components.kms.KmsProviderVersion;
import io.github.loadup.components.kms.KmsSignature;
import io.github.loadup.components.kms.KmsSignatureAlgorithm;
import io.github.loadup.components.kms.KmsTemplate;
import io.github.loadup.components.kms.KmsVerificationKey;
import io.github.loadup.components.kms.aliyun.config.AliyunKmsProperties;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class AliyunKmsTemplate implements KmsTemplate {
    private static final int MAX_PLAINTEXT_BYTES = 6 * 1024;
    private static final int MAX_MESSAGE_BYTES = 1024 * 1024;
    private static final int MAX_CIPHERTEXT_LENGTH = 16 * 1024;
    private static final String SIGNING_ALIAS_TAG = "loadup-signing-alias";
    private final Client client;
    private final AliyunKmsRequests requests;

    public AliyunKmsTemplate(Client client, AliyunKmsProperties properties) {
        this.client = Objects.requireNonNull(client);
        this.requests = new AliyunKmsRequests(properties);
    }

    @Override
    public KmsSignature sign(KmsKeyRef key, KmsSignatureAlgorithm algorithm, byte[] message) {
        String sdkAlgorithm = algorithmName(algorithm);
        String digest = digest(message);
        String id = requestKeyId(key);
        if (key.providerVersion() != null) {
            throw new KmsException(Code.INVALID_REQUEST);
        }
        var version = signingVersion(key, id);
        requireSigningAlias(version.keyId(), key.name());
        var request = new AsymmetricSignRequest()
                .setKeyId(id)
                .setKeyVersionId(version.versionId())
                .setAlgorithm(sdkAlgorithm)
                .setDigest(digest);
        var body = requireResponse(cloud(() -> client.asymmetricSignWithOptions(request, requests.runtime()))
                .getBody());
        requireIdentity(version, body.getKeyId(), body.getKeyVersionId());
        byte[] signature = decode(body.getValue(), 384);
        if (signature.length != 256 && signature.length != 384) {
            throw new KmsException(Code.INVALID_RESPONSE);
        }
        return new KmsSignature(key.name(), 0, algorithm, signature, version);
    }

    @Override
    public boolean verify(KmsKeyRef key, KmsSignature signature, byte[] message) {
        if (signature == null) {
            throw new KmsException(Code.INVALID_REQUEST);
        }
        requireExpected(key, signature.keyName(), signature.providerVersion());
        String digest = digest(message);
        String id = requestKeyId(key);
        var version = signature.providerVersion();
        if (version == null) {
            version = signingVersion(key, id);
        } else if (key.providerVersion() == null) {
            if (!hasSigningAlias(version.keyId(), key.name())) {
                return false;
            }
            id = version.keyId();
        }
        var request = new AsymmetricVerifyRequest()
                .setKeyId(id)
                .setKeyVersionId(version.versionId())
                .setAlgorithm(algorithmName(signature.algorithm()))
                .setDigest(digest)
                .setValue(signature.base64());
        var body = requireResponse(cloud(() -> client.asymmetricVerifyWithOptions(request, requests.runtime()))
                .getBody());
        requireIdentity(version, body.getKeyId(), body.getKeyVersionId());
        return requireResponse(body.getValue());
    }

    @Override
    public KmsCiphertext encrypt(KmsKeyRef key, byte[] plaintext) {
        String id = requestKeyId(key);
        if (key.providerVersion() != null
                || plaintext == null
                || plaintext.length == 0
                || plaintext.length > MAX_PLAINTEXT_BYTES) {
            throw new KmsException(Code.INVALID_REQUEST);
        }
        var metadata = describeKey(id);
        if (keyType(metadata) != KmsKeyType.AES_256_GCM) {
            throw new KmsException(Code.INVALID_REQUEST);
        }
        var request = new EncryptRequest()
                .setKeyId(id)
                .setPlaintext(Base64.getEncoder().encodeToString(plaintext))
                .setEncryptionContext(Map.of("loadup-key", key.name()));
        var body = requireResponse(cloud(() -> client.encryptWithOptions(request, requests.runtime()))
                .getBody());
        var version = identity(body.getKeyId(), body.getKeyVersionId());
        if (!version.keyId().equals(metadata.getKeyId()) || !validCiphertextBlob(body.getCiphertextBlob())) {
            throw new KmsException(Code.INVALID_RESPONSE);
        }
        return new KmsCiphertext(key.name(), 0, "aliyun:" + body.getCiphertextBlob(), version);
    }

    @Override
    public byte[] decrypt(KmsKeyRef key, KmsCiphertext ciphertext) {
        if (ciphertext == null) {
            throw new KmsException(Code.INVALID_REQUEST);
        }
        requireExpected(key, ciphertext.keyName(), ciphertext.providerVersion());
        boolean legacy = ciphertext.providerVersion() == null;
        String prefix = legacy ? "aliyun:v" + ciphertext.version() + ":" : "aliyun:";
        if (!ciphertext.value().startsWith(prefix)) {
            throw new KmsException(Code.INVALID_REQUEST);
        }
        String blob = ciphertext.value().substring(prefix.length());
        if (!validCiphertextBlob(blob)) {
            throw new KmsException(Code.INVALID_REQUEST);
        }
        String expectedId = key.providerVersion() == null
                ? describeKey(requestKeyId(key)).getKeyId()
                : key.providerVersion().keyId();
        var context = legacy
                ? Map.of("loadup-key", key.name(), "loadup-version", Integer.toString(ciphertext.version()))
                : Map.of("loadup-key", key.name());
        var request = new DecryptRequest().setCiphertextBlob(blob).setEncryptionContext(context);
        var body = requireResponse(cloud(() -> client.decryptWithOptions(request, requests.runtime()))
                .getBody());
        var actual = identity(body.getKeyId(), body.getKeyVersionId());
        var expected = key.providerVersion() != null ? key.providerVersion() : ciphertext.providerVersion();
        if (!expectedId.equals(actual.keyId()) || (expected != null && !expected.equals(actual))) {
            throw new KmsException(Code.INVALID_RESPONSE);
        }
        return decode(body.getPlaintext(), MAX_PLAINTEXT_BYTES);
    }

    @Override
    public KmsKeyMetadata metadata(String name) {
        var key = KmsKeyRef.latest(name);
        String id = requestKeyId(key);
        var metadata = describeKey(id);
        var type = keyType(metadata);
        boolean symmetric = type == KmsKeyType.AES_256_GCM;
        var version = symmetric ? identity(metadata.getKeyId(), metadata.getPrimaryKeyVersion()) : rsaVersion(id);
        if (!version.keyId().equals(metadata.getKeyId())) {
            throw new KmsException(Code.INVALID_RESPONSE);
        }
        return new KmsKeyMetadata(
                name,
                type.transitName(),
                0,
                0,
                0,
                symmetric,
                symmetric,
                !symmetric,
                symmetric ? Map.of() : Map.of(0, publicKey(id, version)),
                version);
    }

    @Override
    public String publicKey(KmsKeyRef key) {
        String id = requestKeyId(key);
        return publicKey(id, signingVersion(key, id));
    }

    @Override
    public KmsVerificationKey currentSigningKey(String alias) {
        var current = KmsTemplate.super.currentSigningKey(alias);
        requireSigningAlias(current.actual().providerVersion().keyId(), alias);
        return current;
    }

    @Override
    public List<KmsVerificationKey> verificationKeys(String alias) {
        KmsKeyRef.latest(alias); // Validate before embedding the alias in the SDK's JSON filter.
        String filters = """
            [{"Key":"TagKey","Values":["%s"]},{"Key":"TagValue","Values":["%s"]},
             {"Key":"KeyUsage","Values":["SIGN/VERIFY"]},{"Key":"KeyState","Values":["Enabled"]}]
            """.formatted(SIGNING_ALIAS_TAG, alias);
        var keys = new ArrayList<KmsVerificationKey>();
        var seen = new HashSet<String>();
        for (int page = 1; ; page++) {
            var request = new ListKeysRequest()
                    .setFilters(filters)
                    .setPageNumber(page)
                    .setPageSize(100);
            var body = requireResponse(cloud(() -> client.listKeysWithOptions(request, requests.runtime()))
                    .getBody());
            int total = requireResponse(body.getTotalCount());
            var candidates = body.getKeys() == null ? null : body.getKeys().getKey();
            if (candidates == null) {
                if (total == 0 && seen.isEmpty()) {
                    return List.of();
                }
                throw new KmsException(Code.INVALID_RESPONSE);
            }
            if (total < 0 || (candidates.isEmpty() && seen.size() < total)) {
                throw new KmsException(Code.INVALID_RESPONSE);
            }
            for (var candidate : candidates) {
                String id = requireCloudId(requireResponse(candidate).getKeyId());
                if (!seen.add(id)) {
                    throw new KmsException(Code.INVALID_RESPONSE);
                }
                // TagKey and TagValue can match different tags on the same candidate.
                if (hasSigningAlias(id, alias)) {
                    var version = signingVersion(new KmsKeyRef(alias), id);
                    if (!id.equals(version.keyId())) {
                        throw new KmsException(Code.INVALID_RESPONSE);
                    }
                    keys.add(KmsVerificationKey.fromPem(new KmsKeyRef(alias, 0, version), publicKey(id, version)));
                }
            }
            if (seen.size() > total) {
                throw new KmsException(Code.INVALID_RESPONSE);
            }
            if (seen.size() == total) {
                return List.copyOf(keys);
            }
        }
    }

    private void requireSigningAlias(String id, String alias) {
        if (!hasSigningAlias(id, alias)) {
            throw new KmsException(Code.ACCESS_DENIED);
        }
    }

    private boolean hasSigningAlias(String id, String alias) {
        var request = new ListResourceTagsRequest().setKeyId(id);
        var body = requireResponse(cloud(() -> client.listResourceTagsWithOptions(request, requests.runtime()))
                .getBody());
        var tags = body.getTags();
        return tags != null
                && tags.getTag() != null
                && tags.getTag().stream()
                        .anyMatch(tag -> tag != null
                                && id.equals(tag.getKeyId())
                                && SIGNING_ALIAS_TAG.equals(tag.getTagKey())
                                && alias.equals(tag.getTagValue()));
    }

    private String publicKey(String id, KmsProviderVersion version) {
        var request = new GetPublicKeyRequest().setKeyId(id).setKeyVersionId(version.versionId());
        var body = requireResponse(cloud(() -> client.getPublicKeyWithOptions(request, requests.runtime()))
                .getBody());
        requireIdentity(version, body.getKeyId(), body.getKeyVersionId());
        String pem = body.getPublicKey();
        if (pem == null
                || !pem.startsWith("-----BEGIN PUBLIC KEY-----")
                || !pem.stripTrailing().endsWith("-----END PUBLIC KEY-----")) {
            throw new KmsException(Code.INVALID_RESPONSE);
        }
        return pem;
    }

    private static String requestKeyId(KmsKeyRef key) {
        if (key == null || key.version() != 0) {
            throw new KmsException(Code.INVALID_REQUEST);
        }
        return key.providerVersion() == null
                ? "alias/" + key.name()
                : key.providerVersion().keyId();
    }

    private KmsProviderVersion signingVersion(KmsKeyRef key, String id) {
        if (key.providerVersion() != null) {
            return key.providerVersion();
        }
        var metadata = describeKey(id);
        if (keyType(metadata) == KmsKeyType.AES_256_GCM) {
            throw new KmsException(Code.INVALID_REQUEST);
        }
        var version = rsaVersion(id);
        if (!version.keyId().equals(metadata.getKeyId())) {
            throw new KmsException(Code.INVALID_RESPONSE);
        }
        return version;
    }

    private DescribeKeyResponseBodyKeyMetadata describeKey(String id) {
        var request = new DescribeKeyRequest().setKeyId(id);
        var body = requireResponse(cloud(() -> client.describeKeyWithOptions(request, requests.runtime()))
                .getBody());
        var metadata = requireResponse(body.getKeyMetadata());
        requireCloudId(metadata.getKeyId());
        if (!Objects.equals("Enabled", metadata.getKeyState())) {
            throw new KmsException(Code.UNAVAILABLE);
        }
        return metadata;
    }

    private KmsProviderVersion rsaVersion(String id) {
        var request = new ListKeyVersionsRequest().setKeyId(id).setPageNumber(1).setPageSize(2);
        var body = requireResponse(cloud(() -> client.listKeyVersionsWithOptions(request, requests.runtime()))
                .getBody());
        var versions = requireResponse(requireResponse(body.getKeyVersions()).getKeyVersion());
        if (!Objects.equals(1, body.getTotalCount()) || versions.size() != 1) {
            throw new KmsException(Code.INVALID_RESPONSE);
        }
        var version = requireResponse(versions.getFirst());
        return identity(version.getKeyId(), version.getKeyVersionId());
    }

    private static KmsKeyType keyType(DescribeKeyResponseBodyKeyMetadata metadata) {
        String spec = requireResponse(metadata.getKeySpec());
        KmsKeyType type = switch (spec) {
            case "Aliyun_AES_256", "AES_256" -> KmsKeyType.AES_256_GCM;
            case "RSA_2048" -> KmsKeyType.RSA_2048;
            case "RSA_3072" -> KmsKeyType.RSA_3072;
            default -> throw new KmsException(Code.INVALID_REQUEST);
        };
        String usage = type == KmsKeyType.AES_256_GCM ? "ENCRYPT/DECRYPT" : "SIGN/VERIFY";
        if (!Objects.equals(usage, metadata.getKeyUsage())) {
            throw new KmsException(Code.INVALID_REQUEST);
        }
        return type;
    }

    private static String requireCloudId(String value) {
        if (value == null || !value.matches("[A-Za-z0-9][A-Za-z0-9_-]{0,127}")) {
            throw new KmsException(Code.INVALID_RESPONSE);
        }
        return value;
    }

    private static KmsProviderVersion identity(String keyId, String versionId) {
        return new KmsProviderVersion(requireCloudId(keyId), requireCloudId(versionId));
    }

    private static void requireIdentity(KmsProviderVersion expected, String keyId, String versionId) {
        if (!expected.equals(identity(keyId, versionId))) {
            throw new KmsException(Code.INVALID_RESPONSE);
        }
    }

    private static void requireExpected(KmsKeyRef key, String name, KmsProviderVersion recorded) {
        if (key == null
                || key.version() != 0
                || !key.name().equals(name)
                || (key.providerVersion() != null
                        && recorded != null
                        && !key.providerVersion().equals(recorded))) {
            throw new KmsException(Code.INVALID_REQUEST);
        }
    }

    private static String algorithmName(KmsSignatureAlgorithm algorithm) {
        if (algorithm == null) {
            throw new KmsException(Code.INVALID_REQUEST);
        }
        return switch (algorithm) {
            case RSA_SHA256_PKCS1 -> "RSA_PKCS1_SHA_256";
            case RSA_SHA256_PSS -> "RSA_PSS_SHA_256";
        };
    }

    private static String digest(byte[] message) {
        if (message == null || message.length > MAX_MESSAGE_BYTES) {
            throw new KmsException(Code.INVALID_REQUEST);
        }
        try {
            return Base64.getEncoder()
                    .encodeToString(MessageDigest.getInstance("SHA-256").digest(message));
        } catch (NoSuchAlgorithmException failure) {
            throw new IllegalStateException("JCA SHA-256 unavailable");
        }
    }

    private static boolean validCiphertextBlob(String value) {
        if (value == null || value.isEmpty() || value.length() > MAX_CIPHERTEXT_LENGTH) {
            return false;
        }
        try {
            Base64.getDecoder().decode(value);
            return true;
        } catch (IllegalArgumentException malformed) {
            return false;
        }
    }

    private static byte[] decode(String value, int limit) {
        if (value == null || value.isEmpty() || value.length() > ((limit + 2) / 3) * 4) {
            throw new KmsException(Code.INVALID_RESPONSE);
        }
        try {
            byte[] decoded = Base64.getDecoder().decode(value);
            if (decoded.length > limit) {
                throw new KmsException(Code.INVALID_RESPONSE);
            }
            return decoded;
        } catch (IllegalArgumentException malformed) {
            throw new KmsException(Code.INVALID_RESPONSE);
        }
    }
}
