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
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.loadup.components.dfs.DfsObjectNotFoundException;
import io.github.loadup.components.dfs.DfsService;
import io.github.loadup.components.dfs.DfsStorageException;
import io.github.loadup.components.dfs.model.FileDownloadResponse;
import io.github.loadup.components.dfs.model.FileMetadata;
import io.github.loadup.components.dfs.model.FileUploadRequest;
import io.github.loadup.components.testcontainers.annotation.ContainerType;
import io.github.loadup.components.testcontainers.annotation.EnableTestContainers;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = TestApplication.class)
@EnableTestContainers(value = ContainerType.POSTGRESQL, reuse = false)
@ActiveProfiles("test")
@TestPropertySource(properties = "loadup.dfs.binder-type=database")
class DatabaseDfsProviderIT {
    private final DfsService service;
    private final NamedParameterJdbcTemplate jdbc;

    @Autowired
    DatabaseDfsProviderIT(DfsService service, NamedParameterJdbcTemplate jdbc) {
        this.service = service;
        this.jdbc = jdbc;
    }

    @Test
    void supportsTheCommonObjectContractForSmallFiles() throws Exception {
        byte[] bytes = "database-contract".getBytes(StandardCharsets.UTF_8);
        FileMetadata metadata = service.upload(new FileUploadRequest(
                "contract.txt",
                new ByteArrayInputStream(bytes),
                bytes.length,
                "text/plain",
                null,
                Map.of("case", "database")));

        assertTrue(service.exists(metadata.fileId()));
        assertEquals(
                "database", service.getMetadata(metadata.fileId()).metadata().get("case"));
        try (FileDownloadResponse response = service.download(metadata.fileId())) {
            assertEquals("database-contract", new String(response.content().readAllBytes(), StandardCharsets.UTF_8));
        }
        assertTrue(service.delete(metadata.fileId()));
    }

    @Test
    void missingAndRepeatedDeletePreserveObjectNotFoundContract() {
        String missing = UUID.randomUUID().toString();
        Assertions.assertThat(service.exists(missing)).isFalse();
        Assertions.assertThat(service.delete(missing)).isFalse();
        Assertions.assertThatThrownBy(() -> service.download(missing)).isInstanceOf(DfsObjectNotFoundException.class);
        Assertions.assertThatThrownBy(() -> service.getMetadata(missing))
                .isInstanceOf(DfsObjectNotFoundException.class);
    }

    @Test
    void legacyNullMetadataRemainsReadableAndCorruptedJsonFailsExplicitly() throws Exception {
        String fileId = UUID.randomUUID().toString();
        var now = LocalDateTime.now();
        jdbc.update(
                "INSERT INTO dfs_file (id, filename, file_size, content_type, content, metadata_json, created_at, updated_at, deleted) "
                        + "VALUES (:id, :filename, :fileSize, :contentType, :content, :metadataJson, :createdAt, :updatedAt, FALSE)",
                new MapSqlParameterSource()
                        .addValue("id", fileId)
                        .addValue("filename", "legacy.txt")
                        .addValue("fileSize", 0L)
                        .addValue("contentType", "text/plain")
                        .addValue("content", new byte[0])
                        .addValue("metadataJson", null)
                        .addValue("createdAt", now)
                        .addValue("updatedAt", now));
        try {
            Assertions.assertThat(service.getMetadata(fileId).metadata()).isEmpty();
            jdbc.update(
                    "UPDATE dfs_file SET metadata_json = :metadata WHERE id = :id",
                    new MapSqlParameterSource("id", fileId).addValue("metadata", "not valid json"));
            Assertions.assertThatThrownBy(() -> service.getMetadata(fileId)).isInstanceOf(DfsStorageException.class);
        } finally {
            service.delete(fileId);
        }
    }

    @Test
    void inputLengthMismatchAndStreamFailureDoNotProduceSuccessfulUploads() {
        Assertions.assertThatThrownBy(() -> service.upload(
                        FileUploadRequest.of("bad.txt", new ByteArrayInputStream(new byte[] {1}), 2, "text/plain")))
                .isInstanceOf(DfsStorageException.class);
        var failure = new IOException("synthetic read failure");
        InputStream broken = new InputStream() {
            public int read() throws IOException {
                throw failure;
            }
        };
        Assertions.assertThatThrownBy(() -> service.upload(FileUploadRequest.of("bad.txt", broken, 1, "text/plain")))
                .isInstanceOf(DfsStorageException.class)
                .hasCause(failure);
    }
}
