/*
 * #%L
 * LoadUp Components DFS Test
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
package io.github.loadup.components.dfs.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import io.github.loadup.components.dfs.DfsObjectNotFoundException;
import io.github.loadup.components.dfs.DfsStorageException;
import io.github.loadup.components.dfs.model.FileUploadRequest;
import io.github.loadup.components.dfs.model.MultipartPart;
import io.github.loadup.components.dfs.model.MultipartUploadRequest;
import io.github.loadup.components.dfs.s3.S3DfsProperties;
import io.github.loadup.components.dfs.s3.S3DfsProvider;
import java.io.ByteArrayInputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.AbortMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/**
 * SDK boundary tests complement the real S3-compatible container tests.
 */
class S3DfsProviderContractTest {
    private final S3Client client = mock(S3Client.class);
    private final S3Presigner presigner = mock(S3Presigner.class);

    private S3DfsProperties settings() {
        var settings = new S3DfsProperties();
        settings.setBucket("test-bucket");
        return settings;
    }

    @Test
    void bucketCreationOnlyHandlesMissingBucketsAndRejectsInvalidSettings() {
        for (String bucket : new String[] {null, " "}) {
            var settings = settings();
            settings.setBucket(bucket);
            assertThatThrownBy(() -> new S3DfsProvider(client, presigner, settings))
                    .isInstanceOf(IllegalArgumentException.class);
        }
        var settings = settings();
        settings.setCreateBucket(true);
        when(client.headBucket(any(HeadBucketRequest.class)))
                .thenThrow(S3Exception.builder().statusCode(404).build());
        new S3DfsProvider(client, presigner, settings);
        verify(client).createBucket(any(CreateBucketRequest.class));
        var failure = S3Exception.builder().statusCode(403).build();
        when(client.headBucket(any(HeadBucketRequest.class))).thenThrow(failure);
        assertThatThrownBy(() -> new S3DfsProvider(client, presigner, settings))
                .isInstanceOf(DfsStorageException.class)
                .hasCause(failure);
    }

    @Test
    void pathNormalizationRetainsPrefixesAndRejectsParentTraversal() {
        for (String prefix : new String[] {null, " ", "\\root\\"}) {
            var settings = settings();
            settings.setKeyPrefix(prefix);
            var provider = new S3DfsProvider(client, presigner, settings);
            for (String path : new String[] {null, " ", "/", "/documents/"}) {
                var uploaded = provider.upload(new FileUploadRequest(
                        "file.txt", new ByteArrayInputStream(new byte[0]), 0, null, path, Map.of()));
                assertThat(uploaded.fileId()).doesNotContain("//");
                if (prefix != null && !prefix.isBlank()) {
                    assertThat(uploaded.fileId()).startsWith("root/");
                }
                if ("/documents/".equals(path)) {
                    assertThat(uploaded.fileId()).contains("documents/");
                }
            }
            for (String path : new String[] {"..", "../bad", "folder/../bad"}) {
                assertThatThrownBy(() -> provider.upload(new FileUploadRequest(
                                "file.txt", new ByteArrayInputStream(new byte[0]), 0, null, path, Map.of())))
                        .isInstanceOf(IllegalArgumentException.class);
            }
        }
    }

    @Test
    void legacyMetadataFallsBackToObjectNameContentTypeAndModificationTime() {
        var provider = new S3DfsProvider(client, presigner, settings());
        var modified = Instant.parse("2026-10-15T00:00:00Z");
        when(client.headObject(any(HeadObjectRequest.class)))
                .thenReturn(HeadObjectResponse.builder()
                        .contentLength(3L)
                        .lastModified(modified)
                        .metadata(Map.of("custom", "value"))
                        .build());
        var result = provider.getMetadata("folder/object.txt");
        assertThat(result.filename()).isEqualTo("object.txt");
        assertThat(result.contentType()).isEqualTo("application/octet-stream");
        assertThat(result.uploadedAt()).isEqualTo(modified);
        assertThat(result.metadata()).containsOnlyKeys("custom");
        when(client.headObject(any(HeadObjectRequest.class)))
                .thenReturn(HeadObjectResponse.builder()
                        .contentLength(3L)
                        .contentType("text/plain")
                        .metadata(Map.of("loadup-filename", "name.txt", "loadup-uploaded-at", modified.toString()))
                        .build());
        assertThat(provider.getMetadata("key").filename()).isEqualTo("name.txt");
    }

    @Test
    void notFoundIsDistinctFromOtherStorageFailures() {
        var provider = new S3DfsProvider(client, presigner, settings());
        var missing = S3Exception.builder().statusCode(404).build();
        when(client.headObject(any(HeadObjectRequest.class))).thenThrow(missing);
        assertThat(provider.exists("missing")).isFalse();
        assertThat(provider.delete("missing")).isFalse();
        assertThatThrownBy(() -> provider.getMetadata("missing")).isInstanceOf(DfsObjectNotFoundException.class);
        when(client.getObject(any(GetObjectRequest.class))).thenThrow(missing);
        assertThatThrownBy(() -> provider.download("missing")).isInstanceOf(DfsObjectNotFoundException.class);
        assertThatThrownBy(() -> provider.generatePresignedDownloadUrl("missing", Duration.ofSeconds(1)))
                .isInstanceOf(DfsObjectNotFoundException.class);
        var forbidden = S3Exception.builder().statusCode(403).build();
        when(client.headObject(any(HeadObjectRequest.class))).thenThrow(forbidden);
        assertThatThrownBy(() -> provider.exists("key"))
                .isInstanceOf(DfsStorageException.class)
                .hasCause(forbidden);
        assertThatThrownBy(() -> provider.getMetadata("key"))
                .isInstanceOf(DfsStorageException.class)
                .hasCause(forbidden);
        var network = new IllegalStateException("synthetic connection failure");
        when(client.getObject(any(GetObjectRequest.class))).thenThrow(network);
        assertThatThrownBy(() -> provider.download("key"))
                .isInstanceOf(DfsStorageException.class)
                .hasCause(network);
    }

    @Test
    void uploadDeleteAndMultipartFailuresPreserveCauses() {
        var provider = new S3DfsProvider(client, presigner, settings());
        var failure = new IllegalStateException("synthetic S3 failure");
        when(client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenThrow(failure);
        assertThatThrownBy(() ->
                        provider.upload(FileUploadRequest.of("file", new ByteArrayInputStream(new byte[0]), 0, null)))
                .isInstanceOf(DfsStorageException.class)
                .hasCause(failure);
        when(client.headObject(any(HeadObjectRequest.class)))
                .thenReturn(HeadObjectResponse.builder().build());
        when(client.deleteObject(any(DeleteObjectRequest.class))).thenThrow(failure);
        assertThatThrownBy(() -> provider.delete("key"))
                .isInstanceOf(DfsStorageException.class)
                .hasCause(failure);
        when(client.createMultipartUpload(any(CreateMultipartUploadRequest.class)))
                .thenThrow(failure);
        assertThatThrownBy(() -> provider.initiateMultipartUpload(new MultipartUploadRequest("file", null, null, null)))
                .isInstanceOf(DfsStorageException.class)
                .hasCause(failure);
        when(client.uploadPart(any(UploadPartRequest.class), any(RequestBody.class)))
                .thenThrow(failure);
        assertThatThrownBy(() -> provider.uploadPart("key", "upload", 1, new ByteArrayInputStream(new byte[0]), 0))
                .isInstanceOf(DfsStorageException.class)
                .hasCause(failure);
        when(client.completeMultipartUpload(any(CompleteMultipartUploadRequest.class)))
                .thenThrow(failure);
        assertThatThrownBy(
                        () -> provider.completeMultipartUpload("key", "upload", List.of(new MultipartPart(1, "etag"))))
                .isInstanceOf(DfsStorageException.class)
                .hasCause(failure);
        when(client.abortMultipartUpload(any(AbortMultipartUploadRequest.class)))
                .thenThrow(failure);
        assertThatThrownBy(() -> provider.abortMultipartUpload("key", "upload"))
                .isInstanceOf(DfsStorageException.class)
                .hasCause(failure);
    }

    @Test
    void invalidExpiryAndMultipartInputsFailBeforeSdkCalls() {
        var provider = new S3DfsProvider(client, presigner, settings());
        for (Duration duration : new Duration[] {null, Duration.ZERO, Duration.ofSeconds(-1), Duration.ofDays(8)}) {
            assertThatThrownBy(() -> provider.generatePresignedDownloadUrl("key", duration))
                    .isInstanceOf(IllegalArgumentException.class);
        }
        assertThatThrownBy(() -> provider.uploadPart("key", "upload", 1, new ByteArrayInputStream(new byte[0]), -1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> provider.completeMultipartUpload("key", "upload", null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> provider.completeMultipartUpload("key", "upload", List.of()))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(client, presigner);
    }
}
