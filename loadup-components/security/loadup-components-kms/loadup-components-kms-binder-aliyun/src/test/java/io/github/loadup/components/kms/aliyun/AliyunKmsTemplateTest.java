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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aliyun.kms20160120.Client;
import com.aliyun.kms20160120.models.AsymmetricSignResponse;
import com.aliyun.kms20160120.models.AsymmetricSignResponseBody;
import com.aliyun.kms20160120.models.AsymmetricVerifyResponse;
import com.aliyun.kms20160120.models.AsymmetricVerifyResponseBody;
import com.aliyun.kms20160120.models.DecryptResponse;
import com.aliyun.kms20160120.models.DecryptResponseBody;
import com.aliyun.kms20160120.models.DescribeKeyRequest;
import com.aliyun.kms20160120.models.DescribeKeyResponse;
import com.aliyun.kms20160120.models.DescribeKeyResponseBody;
import com.aliyun.kms20160120.models.DescribeKeyResponseBody.DescribeKeyResponseBodyKeyMetadata;
import com.aliyun.kms20160120.models.EncryptResponse;
import com.aliyun.kms20160120.models.EncryptResponseBody;
import com.aliyun.kms20160120.models.GetPublicKeyRequest;
import com.aliyun.kms20160120.models.GetPublicKeyResponse;
import com.aliyun.kms20160120.models.GetPublicKeyResponseBody;
import com.aliyun.kms20160120.models.ListKeyVersionsRequest;
import com.aliyun.kms20160120.models.ListKeyVersionsResponse;
import com.aliyun.kms20160120.models.ListKeyVersionsResponseBody;
import com.aliyun.kms20160120.models.ListKeyVersionsResponseBody.ListKeyVersionsResponseBodyKeyVersions;
import com.aliyun.kms20160120.models.ListKeyVersionsResponseBody.ListKeyVersionsResponseBodyKeyVersionsKeyVersion;
import com.aliyun.kms20160120.models.ListKeysResponse;
import com.aliyun.kms20160120.models.ListKeysResponseBody;
import com.aliyun.kms20160120.models.ListKeysResponseBody.ListKeysResponseBodyKeys;
import com.aliyun.kms20160120.models.ListKeysResponseBody.ListKeysResponseBodyKeysKey;
import com.aliyun.kms20160120.models.ListResourceTagsRequest;
import com.aliyun.kms20160120.models.ListResourceTagsResponse;
import com.aliyun.kms20160120.models.ListResourceTagsResponseBody;
import com.aliyun.kms20160120.models.ListResourceTagsResponseBody.ListResourceTagsResponseBodyTags;
import com.aliyun.kms20160120.models.ListResourceTagsResponseBody.ListResourceTagsResponseBodyTagsTag;
import io.github.loadup.components.kms.KmsCiphertext;
import io.github.loadup.components.kms.KmsException;
import io.github.loadup.components.kms.KmsKeyRef;
import io.github.loadup.components.kms.KmsProviderVersion;
import io.github.loadup.components.kms.KmsSignature;
import io.github.loadup.components.kms.KmsSignatureAlgorithm;
import io.github.loadup.components.kms.KmsVerificationKey;
import io.github.loadup.components.kms.aliyun.config.AliyunKmsProperties;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class AliyunKmsTemplateTest {
    private static final String RSA_VERSION = "rsa-version-7";
    private static final String AES_VERSION = "aes-version-2";
    private static final byte[] MESSAGE = "pinned signing payload".getBytes(StandardCharsets.UTF_8);
    private static final String SIGNING_ALIAS_TAG = "loadup-signing-alias";

    private final Client client = mock(Client.class);
    private final AliyunKmsTemplate template = new AliyunKmsTemplate(client, properties());

    @Test
    void signsUsingTheAliasAndReturnsTheActualProviderVersion() throws Exception {
        when(client.describeKeyWithOptions(any(), any()))
                .thenReturn(describeResponse("signing-master", "RSA_2048", "SIGN/VERIFY", null));
        when(client.listResourceTagsWithOptions(any(), any()))
                .thenReturn(tagsResponse("signing-master", SIGNING_ALIAS_TAG, "signing"));
        when(client.listKeyVersionsWithOptions(any(), any()))
                .thenReturn(listVersionsResponse("signing-master", RSA_VERSION));
        when(client.asymmetricSignWithOptions(any(), any()))
                .thenReturn(new AsymmetricSignResponse()
                        .setBody(new AsymmetricSignResponseBody()
                                .setKeyId("signing-master")
                                .setKeyVersionId(RSA_VERSION)
                                .setValue(Base64.getEncoder().encodeToString(new byte[256]))));

        var key = new KmsKeyRef("signing");
        assertThat(key).isEqualTo(new KmsKeyRef("signing", 0));
        var signature = template.sign(key, KmsSignatureAlgorithm.RSA_SHA256_PKCS1, MESSAGE);
        assertThat(signature.version()).isZero();
        assertThat(signature.keyRef().providerVersion())
                .isEqualTo(new KmsProviderVersion("signing-master", RSA_VERSION));
        String digest = Base64.getEncoder()
                .encodeToString(MessageDigest.getInstance("SHA-256").digest(MESSAGE));
        verify(client)
                .listKeyVersionsWithOptions(argThat(request -> "alias/signing".equals(request.getKeyId())), any());
        verify(client)
                .asymmetricSignWithOptions(
                        argThat(request -> "alias/signing".equals(request.getKeyId())
                                && RSA_VERSION.equals(request.getKeyVersionId())
                                && digest.equals(request.getDigest())),
                        any());
    }

    @Test
    void archivedSignatureVerifiesAgainstTheTrustedOriginalKeyAfterRebinding() throws Exception {
        var version = new KmsProviderVersion("old-master", "old-version");
        var trusted = new KmsKeyRef("signing", 0, version);
        var signature = new KmsSignature("signing", 0, KmsSignatureAlgorithm.RSA_SHA256_PSS, new byte[256], version);
        when(client.asymmetricVerifyWithOptions(any(), any()))
                .thenReturn(new AsymmetricVerifyResponse()
                        .setBody(new AsymmetricVerifyResponseBody()
                                .setKeyId("old-master")
                                .setKeyVersionId("old-version")
                                .setValue(true)));

        assertThat(template.verify(trusted, signature, MESSAGE)).isTrue();
        verify(client, never()).describeKeyWithOptions(any(), any());
        verify(client)
                .asymmetricVerifyWithOptions(
                        argThat(request -> "old-master".equals(request.getKeyId())
                                && "old-version".equals(request.getKeyVersionId())),
                        any());
    }

    @Test
    void incomingSignatureCannotOverrideTheCallersAlias() throws Exception {
        var claimed = new KmsProviderVersion("another-master", RSA_VERSION);
        var signature = new KmsSignature("signing", 0, KmsSignatureAlgorithm.RSA_SHA256_PSS, new byte[256], claimed);
        when(client.listResourceTagsWithOptions(any(), any()))
                .thenReturn(tagsResponse("another-master", SIGNING_ALIAS_TAG, "another-alias"));

        assertThat(template.verify(KmsKeyRef.latest("signing"), signature, MESSAGE))
                .isFalse();
        verify(client, never()).asymmetricVerifyWithOptions(any(), any());
    }

    @Test
    void newTemplateFindsTrustedHistoricalKeyAndVerifiesItAfterAliasRebinding() throws Exception {
        KeyPair pair = rsaKeyPair();
        KeyPair replacement = rsaKeyPair();
        Client freshClient = mock(Client.class);
        AliyunKmsTemplate freshTemplate = new AliyunKmsTemplate(freshClient, properties());
        when(freshClient.listKeysWithOptions(any(), any())).thenReturn(keysResponse(1, 2, "old-master", "new-master"));
        when(freshClient.listResourceTagsWithOptions(any(), any())).thenAnswer(invocation -> {
            var request = (ListResourceTagsRequest) invocation.getArgument(0);
            return tagsResponse(request.getKeyId(), SIGNING_ALIAS_TAG, "signing");
        });
        when(freshClient.describeKeyWithOptions(any(), any())).thenAnswer(invocation -> {
            var request = (DescribeKeyRequest) invocation.getArgument(0);
            String keyId = request.getKeyId().equals("alias/signing") ? "new-master" : request.getKeyId();
            return describeResponse(keyId, "RSA_2048", "SIGN/VERIFY", null);
        });
        when(freshClient.listKeyVersionsWithOptions(any(), any())).thenAnswer(invocation -> {
            var request = (ListKeyVersionsRequest) invocation.getArgument(0);
            boolean previous = request.getKeyId().equals("old-master");
            return listVersionsResponse(
                    previous ? "old-master" : "new-master", previous ? "old-version" : "new-version");
        });
        when(freshClient.getPublicKeyWithOptions(any(), any())).thenAnswer(invocation -> {
            var request = (GetPublicKeyRequest) invocation.getArgument(0);
            var publicKey = request.getKeyId().equals("old-master") ? pair : replacement;
            String keyId = request.getKeyId().equals("alias/signing") ? "new-master" : request.getKeyId();
            return publicKeyResponse(keyId, request.getKeyVersionId(), publicKeyPem(publicKey));
        });
        when(freshClient.asymmetricVerifyWithOptions(any(), any()))
                .thenReturn(new AsymmetricVerifyResponse()
                        .setBody(new AsymmetricVerifyResponseBody()
                                .setKeyId("old-master")
                                .setKeyVersionId("old-version")
                                .setValue(true)));

        assertThat(freshTemplate.currentSigningKey("signing").actual().providerVersion())
                .isEqualTo(new KmsProviderVersion("new-master", "new-version"));
        List<KmsVerificationKey> keys = freshTemplate.verificationKeys("signing");
        assertThat(keys)
                .extracting(KmsVerificationKey::fingerprint)
                .doesNotHaveDuplicates()
                .hasSize(2);
        String fingerprint = KmsVerificationKey.fromPem(
                        new KmsKeyRef("signing", 0, new KmsProviderVersion("old-master", "old-version")),
                        publicKeyPem(pair))
                .fingerprint();
        assertThat(freshTemplate.verify(
                        "signing", fingerprint, KmsSignatureAlgorithm.RSA_SHA256_PSS, MESSAGE, new byte[256]))
                .isTrue();
        verify(freshClient, Mockito.times(2))
                .listKeysWithOptions(
                        argThat(request -> request.getFilters() != null
                                && request.getFilters().contains(SIGNING_ALIAS_TAG)
                                && request.getFilters().contains("signing")),
                        any());
        verify(freshClient)
                .asymmetricVerifyWithOptions(
                        argThat(request -> "old-master".equals(request.getKeyId())
                                && "old-version".equals(request.getKeyVersionId())),
                        any());
    }

    @Test
    void rejectsCandidatesWithoutTheExactAliasTagPair() throws Exception {
        when(client.listKeysWithOptions(any(), any()))
                .thenReturn(keysResponse(1, 3, "split-tag", "wrong-resource", "untagged"));
        when(client.listResourceTagsWithOptions(any(), any())).thenAnswer(invocation -> {
            var request = (ListResourceTagsRequest) invocation.getArgument(0);
            return switch (request.getKeyId()) {
                case "split-tag" ->
                    new ListResourceTagsResponse()
                            .setBody(new ListResourceTagsResponseBody()
                                    .setTags(new ListResourceTagsResponseBodyTags()
                                            .setTag(List.of(
                                                    new ListResourceTagsResponseBodyTagsTag()
                                                            .setKeyId("split-tag")
                                                            .setTagKey(SIGNING_ALIAS_TAG)
                                                            .setTagValue("another-alias"),
                                                    new ListResourceTagsResponseBodyTagsTag()
                                                            .setKeyId("split-tag")
                                                            .setTagKey("other-tag")
                                                            .setTagValue("signing")))));
                case "wrong-resource" -> tagsResponse("different-key", SIGNING_ALIAS_TAG, "signing");
                default -> emptyTagsResponse();
            };
        });

        assertThat(template.verificationKeys("signing")).isEmpty();
        verify(client, never()).describeKeyWithOptions(any(), any());
        verify(client, never()).getPublicKeyWithOptions(any(), any());
    }

    @Test
    void listsHistoricalKeysAcrossPages() throws Exception {
        when(client.listKeysWithOptions(any(), any()))
                .thenReturn(keysResponse(1, 3, "old-master", "second-master"))
                .thenReturn(keysResponse(2, 3, "third-master"));
        when(client.listResourceTagsWithOptions(any(), any())).thenAnswer(invocation -> {
            var request = (ListResourceTagsRequest) invocation.getArgument(0);
            return tagsResponse(request.getKeyId(), SIGNING_ALIAS_TAG, "signing");
        });
        when(client.describeKeyWithOptions(any(), any())).thenAnswer(invocation -> {
            var request = (DescribeKeyRequest) invocation.getArgument(0);
            return describeResponse(request.getKeyId(), "RSA_2048", "SIGN/VERIFY", null);
        });
        when(client.listKeyVersionsWithOptions(any(), any())).thenAnswer(invocation -> {
            var request = (ListKeyVersionsRequest) invocation.getArgument(0);
            return listVersionsResponse(request.getKeyId(), request.getKeyId() + "-version");
        });
        when(client.getPublicKeyWithOptions(any(), any())).thenAnswer(invocation -> {
            var request = (GetPublicKeyRequest) invocation.getArgument(0);
            return publicKeyResponse(request.getKeyId(), request.getKeyVersionId(), publicKeyPem(rsaKeyPair()));
        });

        assertThat(template.verificationKeys("signing")).hasSize(3);
        verify(client)
                .listKeysWithOptions(argThat(request -> Integer.valueOf(1).equals(request.getPageNumber())), any());
        verify(client)
                .listKeysWithOptions(argThat(request -> Integer.valueOf(2).equals(request.getPageNumber())), any());
    }

    @Test
    void encryptsWithAliasAndDecryptsWithTheOriginalKeyAndContext() throws Exception {
        var alias = KmsKeyRef.latest("data");
        when(client.describeKeyWithOptions(any(), any()))
                .thenReturn(describeResponse("data-master", "Aliyun_AES_256", "ENCRYPT/DECRYPT", "previous-version"));
        when(client.encryptWithOptions(any(), any()))
                .thenReturn(new EncryptResponse()
                        .setBody(new EncryptResponseBody()
                                .setKeyId("data-master")
                                .setKeyVersionId(AES_VERSION)
                                .setCiphertextBlob("AQID")));
        var ciphertext = template.encrypt(alias, MESSAGE);
        var trusted = ciphertext.keyRef();
        assertThat(trusted.providerVersion()).isEqualTo(new KmsProviderVersion("data-master", AES_VERSION));
        verify(client)
                .encryptWithOptions(
                        argThat(request -> "alias/data".equals(request.getKeyId())
                                && Map.of("loadup-key", "data").equals(request.getEncryptionContext())),
                        any());

        when(client.decryptWithOptions(any(), any()))
                .thenReturn(new DecryptResponse()
                        .setBody(new DecryptResponseBody()
                                .setKeyId("data-master")
                                .setKeyVersionId(AES_VERSION)
                                .setPlaintext(Base64.getEncoder().encodeToString(MESSAGE))));
        assertThat(template.decrypt(trusted, ciphertext)).containsExactly(MESSAGE);
        verify(client)
                .decryptWithOptions(
                        argThat(request -> "AQID".equals(request.getCiphertextBlob())
                                && Map.of("loadup-key", "data").equals(request.getEncryptionContext())),
                        any());
    }

    @Test
    void legacyCiphertextKeepsItsEncryptionContextAndRejectsADifferentMaster() throws Exception {
        when(client.describeKeyWithOptions(any(), any()))
                .thenReturn(describeResponse("data-master", "Aliyun_AES_256", "ENCRYPT/DECRYPT", AES_VERSION));
        when(client.decryptWithOptions(any(), any()))
                .thenReturn(new DecryptResponse()
                        .setBody(new DecryptResponseBody()
                                .setKeyId("different-master")
                                .setKeyVersionId(AES_VERSION)
                                .setPlaintext("AQID")));
        assertThatThrownBy(() ->
                        template.decrypt(KmsKeyRef.latest("data"), new KmsCiphertext("data", 7, "aliyun:v7:AQID")))
                .isInstanceOf(KmsException.class);
        verify(client)
                .decryptWithOptions(
                        argThat(request -> Map.of("loadup-key", "data", "loadup-version", "7")
                                .equals(request.getEncryptionContext())),
                        any());
    }

    private static DescribeKeyResponse describeResponse(
            String keyId, String spec, String usage, String primaryVersion) {
        return new DescribeKeyResponse()
                .setBody(new DescribeKeyResponseBody()
                        .setKeyMetadata(new DescribeKeyResponseBodyKeyMetadata()
                                .setKeyId(keyId)
                                .setKeySpec(spec)
                                .setKeyUsage(usage)
                                .setKeyState("Enabled")
                                .setPrimaryKeyVersion(primaryVersion)));
    }

    private static ListKeyVersionsResponse listVersionsResponse(String keyId, String versionId) {
        var version = new ListKeyVersionsResponseBodyKeyVersionsKeyVersion()
                .setKeyId(keyId)
                .setKeyVersionId(versionId);
        return new ListKeyVersionsResponse()
                .setBody(new ListKeyVersionsResponseBody()
                        .setTotalCount(1)
                        .setKeyVersions(new ListKeyVersionsResponseBodyKeyVersions().setKeyVersion(List.of(version))));
    }

    private static ListKeysResponse keysResponse(int pageNumber, int totalCount, String... keyIds) {
        var keys = Arrays.stream(keyIds)
                .map(keyId -> new ListKeysResponseBodyKeysKey().setKeyId(keyId))
                .toList();
        return new ListKeysResponse()
                .setBody(new ListKeysResponseBody()
                        .setPageNumber(pageNumber)
                        .setTotalCount(totalCount)
                        .setKeys(new ListKeysResponseBodyKeys().setKey(keys)));
    }

    private static ListResourceTagsResponse tagsResponse(String keyId, String tagKey, String tagValue) {
        return new ListResourceTagsResponse()
                .setBody(new ListResourceTagsResponseBody()
                        .setTags(new ListResourceTagsResponseBodyTags()
                                .setTag(List.of(new ListResourceTagsResponseBodyTagsTag()
                                        .setKeyId(keyId)
                                        .setTagKey(tagKey)
                                        .setTagValue(tagValue)))));
    }

    private static ListResourceTagsResponse emptyTagsResponse() {
        return new ListResourceTagsResponse()
                .setBody(new ListResourceTagsResponseBody()
                        .setTags(new ListResourceTagsResponseBodyTags().setTag(List.of())));
    }

    private static GetPublicKeyResponse publicKeyResponse(String keyId, String versionId, String pem) {
        return new GetPublicKeyResponse()
                .setBody(new GetPublicKeyResponseBody()
                        .setKeyId(keyId)
                        .setKeyVersionId(versionId)
                        .setPublicKey(pem));
    }

    private static KeyPair rsaKeyPair() throws Exception {
        var generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        return generator.generateKeyPair();
    }

    private static String publicKeyPem(KeyPair pair) {
        return "-----BEGIN PUBLIC KEY-----\n"
                + Base64.getMimeEncoder(64, new byte[] {'\n'})
                        .encodeToString(pair.getPublic().getEncoded()) + "\n-----END PUBLIC KEY-----";
    }

    private static AliyunKmsProperties properties() {
        return new AliyunKmsProperties("cn-hangzhou", null, Duration.ofSeconds(2), Duration.ofSeconds(5), null);
    }
}
