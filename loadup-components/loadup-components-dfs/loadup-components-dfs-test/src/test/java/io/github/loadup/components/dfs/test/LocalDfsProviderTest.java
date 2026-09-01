/*-
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
    void reportsUnsupportedAdvancedCapabilities() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> service.generatePresignedDownloadUrl("missing", Duration.ofMinutes(1)));
    }
}
