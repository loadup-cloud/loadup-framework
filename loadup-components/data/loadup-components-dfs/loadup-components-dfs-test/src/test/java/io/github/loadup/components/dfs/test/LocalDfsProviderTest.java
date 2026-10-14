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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.loadup.components.dfs.DefaultDfsService;
import io.github.loadup.components.dfs.DfsService;
import io.github.loadup.components.dfs.DfsStorageException;
import io.github.loadup.components.dfs.local.LocalDfsProperties;
import io.github.loadup.components.dfs.local.LocalDfsProvider;
import io.github.loadup.components.dfs.model.FileDownloadResponse;
import io.github.loadup.components.dfs.model.FileMetadata;
import io.github.loadup.components.dfs.model.FileUploadRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import io.github.loadup.components.dfs.DfsObjectNotFoundException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LocalDfsProviderTest {
    @TempDir
    Path tempDirectory;

    private DfsService service;

    @BeforeEach
    void setUp() {
        LocalDfsProperties properties = new LocalDfsProperties();
        properties.setBasePath(tempDirectory.toString());
        service = new DefaultDfsService(new LocalDfsProvider(properties));
    }

    @Test
    void uploadsDownloadsAndPersistsMetadata() throws Exception {
        byte[] bytes = "loadup-dfs".getBytes(StandardCharsets.UTF_8);
        FileMetadata metadata = service.upload(new FileUploadRequest(
                "hello.txt",
                new ByteArrayInputStream(bytes),
                bytes.length,
                "text/plain",
                "documents/2026",
                Map.of("owner", "test")));

        assertTrue(service.exists(metadata.fileId()));
        assertEquals("hello.txt", service.getMetadata(metadata.fileId()).filename());
        assertEquals("test", service.getMetadata(metadata.fileId()).metadata().get("owner"));
        try (FileDownloadResponse response = service.download(metadata.fileId())) {
            assertEquals("loadup-dfs", new String(response.content().readAllBytes(), StandardCharsets.UTF_8));
            assertEquals(bytes.length, response.contentLength());
        }

        LocalDfsProperties samePath = new LocalDfsProperties();
        samePath.setBasePath(tempDirectory.toString());
        DfsService restartedService = new DefaultDfsService(new LocalDfsProvider(samePath));
        assertEquals(
                "hello.txt", restartedService.getMetadata(metadata.fileId()).filename());
        assertTrue(Files.isRegularFile(tempDirectory.resolve("objects").resolve(metadata.fileId())));
    }

    @Test
    void rejectsLengthMismatchAndUnsafePath() {
        assertThrows(
                DfsStorageException.class,
                () -> service.upload(new FileUploadRequest(
                        "hello.txt", new ByteArrayInputStream(new byte[] {1}), 2, "text/plain", null, Map.of())));
        assertThrows(
                IllegalArgumentException.class,
                () -> service.upload(new FileUploadRequest(
                        "hello.txt",
                        new ByteArrayInputStream(new byte[] {1}),
                        1,
                        "text/plain",
                        "../escape",
                        Map.of())));
    }

    @Test
    void defaultTypeBlankPathAndEmptyObjectsRemainRetrievableAndDeletable() {
        FileMetadata metadata = service.upload(new FileUploadRequest("empty", new ByteArrayInputStream(new byte[0]),
                0, " ", " ", null));
        assertThat(metadata.fileId()).doesNotContain("/");
        assertThat(metadata.contentType()).isEqualTo("application/octet-stream");
        assertThat(metadata.metadata()).isEmpty();
        assertThat(service.delete(metadata.fileId())).isTrue();
        assertThat(service.exists(metadata.fileId())).isFalse();
        assertThat(service.delete(metadata.fileId())).isFalse();
        assertThatThrownBy(() -> service.getMetadata(metadata.fileId())).isInstanceOf(DfsObjectNotFoundException.class);
        assertThatThrownBy(() -> service.download("missing")).isInstanceOf(DfsObjectNotFoundException.class);
    }

    @Test
    void traversalAndAbsolutePathsCannotAccessStorageOutsideRoot() {
        for (String unsafe : new String[] {"../escape", tempDirectory.getParent().resolve("escape").toString()}) {
            assertThatThrownBy(() -> service.exists(unsafe)).isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> service.delete(unsafe)).isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> service.getMetadata(unsafe)).isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> service.upload(new FileUploadRequest("file", new ByteArrayInputStream(new byte[0]),
                    0, null, unsafe, null))).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    void uploadStreamOwnershipStaysWithCallerAndReadFailureCleansTemporaryObjects() throws Exception {
        AtomicBoolean closed = new AtomicBoolean();
        InputStream source = new ByteArrayInputStream(new byte[] {1}) {
            @Override public void close() { closed.set(true); }
        };
        service.upload(FileUploadRequest.of("file", source, 1, null));
        assertThat(closed).isFalse();
        source.close();
        assertThat(closed).isTrue();
        InputStream failing = new InputStream() {
            @Override public int read() throws IOException { throw new IOException("synthetic read failure"); }
        };
        assertThatThrownBy(() -> service.upload(FileUploadRequest.of("broken", failing, 1, null)))
                .isInstanceOf(DfsStorageException.class);
        try (var files = Files.walk(tempDirectory.resolve("objects"))) {
            assertThat(files.filter(path -> path.getFileName().toString().endsWith(".uploading"))).isEmpty();
        }
    }

    @Test
    void metadataWriteFailureRollsBackStoredObject() throws Exception {
        Path metadataRoot = tempDirectory.resolve("metadata");
        Files.delete(metadataRoot);
        Files.writeString(metadataRoot, "blocked metadata directory");
        assertThatThrownBy(() -> service.upload(FileUploadRequest.of("broken", new ByteArrayInputStream(new byte[] {1}),
                1, null))).isInstanceOf(DfsStorageException.class);
        try (var files = Files.list(tempDirectory.resolve("objects"))) {
            assertThat(files).isEmpty();
        }
    }

    @Test
    void missingContentAndNonemptyDirectoryDeletionProduceStorageFailure() throws Exception {
        FileMetadata metadata = service.upload(FileUploadRequest.of("file", new ByteArrayInputStream(new byte[] {1}), 1, null));
        Files.delete(tempDirectory.resolve("objects").resolve(metadata.fileId()));
        assertThatThrownBy(() -> service.download(metadata.fileId())).isInstanceOf(DfsStorageException.class);
        Path directory = tempDirectory.resolve("objects/nonempty");
        Files.createDirectory(directory);
        Files.writeString(directory.resolve("child"), "content");
        assertThatThrownBy(() -> service.delete("nonempty")).isInstanceOf(DfsStorageException.class);
    }

    @Test
    void invalidBaseDirectoryFailsProviderConstruction() throws Exception {
        Path blocked = tempDirectory.resolve("blocked");
        Files.writeString(blocked, "not a directory");
        LocalDfsProperties properties = new LocalDfsProperties();
        properties.setBasePath(blocked.toString());
        assertThatThrownBy(() -> new LocalDfsProvider(properties)).isInstanceOf(DfsStorageException.class);
    }

    @Test
    void reportsUnsupportedAdvancedCapabilities() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> service.generatePresignedDownloadUrl("missing", Duration.ofMinutes(1)));
    }
}
