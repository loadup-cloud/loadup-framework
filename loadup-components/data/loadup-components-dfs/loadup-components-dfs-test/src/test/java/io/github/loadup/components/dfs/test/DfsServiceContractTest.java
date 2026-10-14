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
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.loadup.components.dfs.DefaultDfsService;
import io.github.loadup.components.dfs.DfsProvider;
import io.github.loadup.components.dfs.DfsService;
import io.github.loadup.components.dfs.autoconfigure.DfsAutoConfiguration;
import io.github.loadup.components.dfs.local.autoconfigure.LocalDfsAutoConfiguration;
import io.github.loadup.components.dfs.model.FileDownloadResponse;
import io.github.loadup.components.dfs.model.FileMetadata;
import io.github.loadup.components.dfs.model.FileUploadRequest;
import io.github.loadup.components.dfs.model.MultipartPart;
import io.github.loadup.components.dfs.model.MultipartUploadRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class DfsServiceContractTest {
    @TempDir Path directory;

    @Test
    void uploadAndMultipartInputsValidateAndSnapshotMetadata() {
        ByteArrayInputStream content = new ByteArrayInputStream(new byte[0]);
        assertThatThrownBy(() -> new FileUploadRequest(null, content, 0, null, null, null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> FileUploadRequest.of(" ", content, 0, null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> FileUploadRequest.of("file", null, 0, null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> FileUploadRequest.of("file", content, -1, null)).isInstanceOf(IllegalArgumentException.class);
        Map<String, String> labels = new HashMap<>(Map.of("owner", "original"));
        FileUploadRequest request = new FileUploadRequest("file", content, 0, null, null, labels);
        MultipartUploadRequest multipart = new MultipartUploadRequest("file", " ", null, labels);
        labels.put("owner", "changed");
        assertThat(request.metadata()).containsEntry("owner", "original");
        assertThat(multipart.metadata()).containsEntry("owner", "original");
        assertThat(request.contentType()).isEqualTo("application/octet-stream");
        assertThat(multipart.contentType()).isEqualTo("application/octet-stream");
        assertThat(new MultipartUploadRequest("file", "text/plain", null, null).metadata()).isEmpty();
        assertThatThrownBy(() -> new MultipartUploadRequest(null, null, null, null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new MultipartUploadRequest(" ", null, null, null)).isInstanceOf(IllegalArgumentException.class);
        assertThat(new MultipartPart(1, "etag").partNumber()).isEqualTo(1);
        assertThat(new MultipartPart(10_000, "etag").partNumber()).isEqualTo(10_000);
        assertThatThrownBy(() -> new MultipartPart(0, "etag")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new MultipartPart(10_001, "etag")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void downloadResponseValidatesLengthAndClosesOwnedStream() throws IOException {
        FileMetadata metadata = new FileMetadata("id", "file", 0, "text/plain", "local", "id", null, Instant.now());
        assertThat(metadata.metadata()).isEmpty();
        ByteArrayInputStream stream = new ByteArrayInputStream(new byte[0]);
        assertThatThrownBy(() -> new FileDownloadResponse(null, stream, 0)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new FileDownloadResponse(metadata, null, 0)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new FileDownloadResponse(metadata, stream, -1)).isInstanceOf(IllegalArgumentException.class);
        AtomicBoolean closed = new AtomicBoolean();
        try (FileDownloadResponse response = new FileDownloadResponse(metadata, new ByteArrayInputStream(new byte[0]) {
            @Override public void close() { closed.set(true); }
        }, 0)) {
            assertThat(response.metadata()).isSameAs(metadata);
            assertThat(closed).isFalse();
        }
        assertThat(closed).isTrue();
    }

    @Test
    void facadePreservesDefaultUnsupportedCapabilitiesAndArguments() {
        DfsProvider provider = mock(DfsProvider.class, CALLS_REAL_METHODS);
        when(provider.getBinderType()).thenReturn("minimal");
        DfsService service = new DefaultDfsService(provider);
        assertThatThrownBy(() -> service.generatePresignedDownloadUrl("id", Duration.ofMinutes(1)))
                .isInstanceOf(UnsupportedOperationException.class).hasMessageContaining("minimal");
        assertThatThrownBy(() -> service.initiateMultipartUpload(new MultipartUploadRequest("file", null, null, null)))
                .isInstanceOf(UnsupportedOperationException.class);
        ByteArrayInputStream content = new ByteArrayInputStream(new byte[] {1});
        assertThatThrownBy(() -> service.uploadPart("id", "upload", 1, content, 1))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> service.completeMultipartUpload("id", "upload", List.of(new MultipartPart(1, "etag"))))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> service.abortMultipartUpload("id", "upload"))
                .isInstanceOf(UnsupportedOperationException.class);
        verify(provider).uploadPart("id", "upload", 1, content, 1);
        verify(provider).abortMultipartUpload("id", "upload");
    }

    @Test
    void facadeRequiresASingleProviderAndPreservesCustomServices() {
        ApplicationContextRunner api = new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(DfsAutoConfiguration.class));
        api.run(context -> assertThat(context).hasNotFailed().doesNotHaveBean(DfsService.class));
        api.withBean("first", DfsProvider.class, () -> mock(DfsProvider.class))
                .withBean("second", DfsProvider.class, () -> mock(DfsProvider.class))
                .run(context -> assertThat(context).hasNotFailed().doesNotHaveBean(DfsService.class));
        DfsService custom = mock(DfsService.class);
        api.withBean(DfsProvider.class, () -> mock(DfsProvider.class)).withBean(DfsService.class, () -> custom)
                .run(context -> {
                    assertThat(context).hasNotFailed().hasSingleBean(DfsService.class);
                    assertThat(context.getBean(DfsService.class)).isSameAs(custom);
                });
    }

    @Test
    void localBinderUsesConfiguredDirectoryAndBacksOffForAnotherBinderOrProvider() {
        ApplicationContextRunner local = new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(LocalDfsAutoConfiguration.class, DfsAutoConfiguration.class))
                .withPropertyValues("loadup.dfs.binder.local.base-path=" + directory);
        local.run(context -> {
            assertThat(context).hasNotFailed().hasSingleBean(DfsService.class);
            FileMetadata metadata = context.getBean(DfsService.class)
                    .upload(FileUploadRequest.of("file", new ByteArrayInputStream(new byte[] {1}), 1, null));
            assertThat(directory.resolve("objects").resolve(metadata.fileId())).isRegularFile();
        });
        local.withPropertyValues("loadup.dfs.binder-type=s3")
                .run(context -> assertThat(context).hasNotFailed().doesNotHaveBean(DfsProvider.class));
        DfsProvider custom = mock(DfsProvider.class);
        local.withBean(DfsProvider.class, () -> custom).run(context -> {
            assertThat(context).hasNotFailed().hasSingleBean(DfsProvider.class);
            assertThat(context.getBean(DfsProvider.class)).isSameAs(custom);
        });
    }
}
