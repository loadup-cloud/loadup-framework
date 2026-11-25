/*
 * Copyright (C) 2025-2026 LoadUp Cloud
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
package io.github.loadup.components.dfs.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.loadup.components.dfs.DfsObjectNotFoundException;
import io.github.loadup.components.dfs.DfsStorageException;
import io.github.loadup.components.dfs.DfsTemplate;
import io.github.loadup.components.dfs.local.LocalDfsProperties;
import io.github.loadup.components.dfs.local.LocalDfsTemplate;
import io.github.loadup.components.dfs.model.FileDownloadResponse;
import io.github.loadup.components.dfs.model.FileMetadata;
import io.github.loadup.components.dfs.model.FileUploadRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LocalDfsTemplateTest {
    @TempDir
    Path tempDirectory;

    private DfsTemplate template;

    @BeforeEach
    void setUp() {
        LocalDfsProperties properties = new LocalDfsProperties();
        properties.setBasePath(tempDirectory.toString());
        template = new LocalDfsTemplate(properties);
    }

    @Test
    void uploadsDownloadsAndPersistsMetadata() throws Exception {
        byte[] bytes = "loadup-dfs".getBytes(StandardCharsets.UTF_8);
        FileMetadata metadata = template.upload(new FileUploadRequest(
                "hello.txt",
                new ByteArrayInputStream(bytes),
                bytes.length,
                "text/plain",
                "documents/2026",
                Map.of("owner", "test")));

        assertTrue(template.exists(metadata.fileId()));
        assertEquals("hello.txt", template.getMetadata(metadata.fileId()).filename());
        assertEquals("test", template.getMetadata(metadata.fileId()).metadata().get("owner"));
        try (FileDownloadResponse response = template.download(metadata.fileId())) {
            assertEquals("loadup-dfs", new String(response.content().readAllBytes(), StandardCharsets.UTF_8));
            assertEquals(bytes.length, response.contentLength());
        }

        LocalDfsProperties samePath = new LocalDfsProperties();
        samePath.setBasePath(tempDirectory.toString());
        DfsTemplate restartedTemplate = new LocalDfsTemplate(samePath);
        assertEquals(
                "hello.txt", restartedTemplate.getMetadata(metadata.fileId()).filename());
        assertTrue(Files.isRegularFile(tempDirectory.resolve("objects").resolve(metadata.fileId())));
    }

    @Test
    void rejectsLengthMismatchAndUnsafePath() {
        assertThrows(
                DfsStorageException.class,
                () -> template.upload(new FileUploadRequest(
                        "hello.txt", new ByteArrayInputStream(new byte[] {1}), 2, "text/plain", null, Map.of())));
        assertThrows(
                IllegalArgumentException.class,
                () -> template.upload(new FileUploadRequest(
                        "hello.txt",
                        new ByteArrayInputStream(new byte[] {1}),
                        1,
                        "text/plain",
                        "../escape",
                        Map.of())));
    }

    @Test
    void defaultTypeBlankPathAndEmptyObjectsRemainRetrievableAndDeletable() {
        FileMetadata metadata = template.upload(
                new FileUploadRequest("empty", new ByteArrayInputStream(new byte[0]), 0, " ", " ", null));
        assertThat(metadata.fileId()).doesNotContain("/");
        assertThat(metadata.contentType()).isEqualTo("application/octet-stream");
        assertThat(metadata.metadata()).isEmpty();
        assertThat(template.delete(metadata.fileId())).isTrue();
        assertThat(template.exists(metadata.fileId())).isFalse();
        assertThat(template.delete(metadata.fileId())).isFalse();
        assertThatThrownBy(() -> template.getMetadata(metadata.fileId()))
                .isInstanceOf(DfsObjectNotFoundException.class);
        assertThatThrownBy(() -> template.download("missing")).isInstanceOf(DfsObjectNotFoundException.class);
    }

    @Test
    void traversalAndAbsolutePathsCannotAccessStorageOutsideRoot() {
        for (String unsafe : new String[] {
            "../escape", tempDirectory.getParent().resolve("escape").toString()
        }) {
            assertThatThrownBy(() -> template.exists(unsafe)).isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> template.delete(unsafe)).isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> template.getMetadata(unsafe)).isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> template.upload(new FileUploadRequest(
                            "file", new ByteArrayInputStream(new byte[0]), 0, null, unsafe, null)))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    void uploadStreamOwnershipStaysWithCallerAndReadFailureCleansTemporaryObjects() throws Exception {
        AtomicBoolean closed = new AtomicBoolean();
        InputStream source = new ByteArrayInputStream(new byte[] {1}) {
            @Override
            public void close() {
                closed.set(true);
            }
        };
        template.upload(FileUploadRequest.of("file", source, 1, null));
        assertThat(closed).isFalse();
        source.close();
        assertThat(closed).isTrue();
        InputStream failing = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("synthetic read failure");
            }
        };
        assertThatThrownBy(() -> template.upload(FileUploadRequest.of("broken", failing, 1, null)))
                .isInstanceOf(DfsStorageException.class);
        try (var files = Files.walk(tempDirectory.resolve("objects"))) {
            assertThat(files.filter(path -> path.getFileName().toString().endsWith(".uploading")))
                    .isEmpty();
        }
    }

    @Test
    void metadataWriteFailureRollsBackStoredObject() throws Exception {
        Path metadataRoot = tempDirectory.resolve("metadata");
        Files.delete(metadataRoot);
        Files.writeString(metadataRoot, "blocked metadata directory");
        assertThatThrownBy(() -> template.upload(
                        FileUploadRequest.of("broken", new ByteArrayInputStream(new byte[] {1}), 1, null)))
                .isInstanceOf(DfsStorageException.class);
        try (var files = Files.list(tempDirectory.resolve("objects"))) {
            assertThat(files).isEmpty();
        }
    }

    @Test
    void missingContentAndNonemptyDirectoryDeletionProduceStorageFailure() throws Exception {
        FileMetadata metadata =
                template.upload(FileUploadRequest.of("file", new ByteArrayInputStream(new byte[] {1}), 1, null));
        Files.delete(tempDirectory.resolve("objects").resolve(metadata.fileId()));
        assertThatThrownBy(() -> template.download(metadata.fileId())).isInstanceOf(DfsStorageException.class);
        Path directory = tempDirectory.resolve("objects/nonempty");
        Files.createDirectory(directory);
        Files.writeString(directory.resolve("child"), "content");
        assertThatThrownBy(() -> template.delete("nonempty")).isInstanceOf(DfsStorageException.class);
    }

    @Test
    void invalidBaseDirectoryFailsProviderConstruction() throws Exception {
        Path blocked = tempDirectory.resolve("blocked");
        Files.writeString(blocked, "not a directory");
        LocalDfsProperties properties = new LocalDfsProperties();
        properties.setBasePath(blocked.toString());
        assertThatThrownBy(() -> new LocalDfsTemplate(properties)).isInstanceOf(DfsStorageException.class);
    }

    @Test
    void reportsUnsupportedAdvancedCapabilities() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> template.generatePresignedDownloadUrl("missing", Duration.ofMinutes(1)));
    }
}
