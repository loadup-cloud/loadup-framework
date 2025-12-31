package com.github.loadup.components.dfs.test.service;

/*-
 * #%L
 * loadup-components-dfs-test
 * %%
 * Copyright (C) 2026 LoadUp Cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import static org.assertj.core.api.Assertions.*;

import com.github.loadup.components.dfs.model.FileDownloadResponse;
import com.github.loadup.components.dfs.model.FileMetadata;
import com.github.loadup.components.dfs.model.FileUploadRequest;
import com.github.loadup.components.dfs.service.DfsService;
import com.github.loadup.components.dfs.test.DfsTestApplication;
import com.github.loadup.components.dfs.test.config.TestContainersConfiguration;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/** Test cases for DfsService with Testcontainers MySQL */
@SpringBootTest(classes = DfsTestApplication.class)
@Import(TestContainersConfiguration.class)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DfsServiceTest {

    @Autowired
    private DfsService dfsService;

    private static final String TEST_CONTENT = "DFS Service test content.";
    private static final String TEST_FILENAME = "service-test.txt";

    @Test
    @Order(1)
    @DisplayName("Should upload file using default provider")
    void testUploadWithDefaultProvider() throws IOException {
        // Given
        FileUploadRequest request = FileUploadRequest.builder()
                .filename(TEST_FILENAME)
                .inputStream(new ByteArrayInputStream(TEST_CONTENT.getBytes(StandardCharsets.UTF_8)))
                .contentType("text/plain")
                .bizType("service-test")
                .build();

        // When
        FileMetadata metadata = dfsService.upload(request);

        // Then
        assertThat(metadata).isNotNull();
        assertThat(metadata.getFileId()).isNotNull();
        assertThat(metadata.getFilename()).isEqualTo(TEST_FILENAME);
        assertThat(metadata.getProvider()).isEqualTo("local"); // default provider
    }

    @Test
    @Order(2)
    @DisplayName("Should upload file with specific provider")
    void testUploadWithSpecificProvider() throws IOException {
        // Given
        FileUploadRequest request = FileUploadRequest.builder()
                .filename(TEST_FILENAME)
                .inputStream(new ByteArrayInputStream(TEST_CONTENT.getBytes(StandardCharsets.UTF_8)))
                .contentType("text/plain")
                .build();

        // When
        FileMetadata metadata = dfsService.upload(request, "database");

        // Then
        assertThat(metadata).isNotNull();
        assertThat(metadata.getProvider()).isEqualTo("database");
    }

    @Test
    @Order(3)
    @DisplayName("Should download file successfully")
    void testDownload() throws IOException {
        // Given - Upload a file first
        FileUploadRequest uploadRequest = FileUploadRequest.builder()
                .filename(TEST_FILENAME)
                .inputStream(new ByteArrayInputStream(TEST_CONTENT.getBytes(StandardCharsets.UTF_8)))
                .build();
        FileMetadata uploadedMetadata = dfsService.upload(uploadRequest, "database");

        // When
        FileDownloadResponse response = dfsService.download(uploadedMetadata.getFileId(), "database");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getMetadata()).isNotNull();
        String content = IOUtils.toString(response.getInputStream(), StandardCharsets.UTF_8);
        assertThat(content).isEqualTo(TEST_CONTENT);
    }

    @Test
    @Order(4)
    @DisplayName("Should delete file successfully")
    void testDelete() throws IOException {
        // Given - Upload a file first
        FileUploadRequest request = FileUploadRequest.builder()
                .filename(TEST_FILENAME)
                .inputStream(new ByteArrayInputStream(TEST_CONTENT.getBytes(StandardCharsets.UTF_8)))
                .build();
        FileMetadata metadata = dfsService.upload(request, "database");

        // When
        boolean deleted = dfsService.delete(metadata.getFileId(), "database");

        // Then
        assertThat(deleted).isTrue();
    }

    @Test
    @Order(5)
    @DisplayName("Should check file existence")
    void testExists() throws IOException {
        // Given - Upload a file
        FileUploadRequest request = FileUploadRequest.builder()
                .filename(TEST_FILENAME)
                .inputStream(new ByteArrayInputStream(TEST_CONTENT.getBytes(StandardCharsets.UTF_8)))
                .build();
        FileMetadata metadata = dfsService.upload(request, "database");

        // When & Then
        assertThat(dfsService.exists(metadata.getFileId(), "database")).isTrue();
        assertThat(dfsService.exists("non-existent", "database")).isFalse();
    }

    @Test
    @Order(6)
    @DisplayName("Should get file metadata")
    void testGetMetadata() throws IOException {
        // Given - Upload a file
        FileUploadRequest request = FileUploadRequest.builder()
                .filename(TEST_FILENAME)
                .inputStream(new ByteArrayInputStream(TEST_CONTENT.getBytes(StandardCharsets.UTF_8)))
                .contentType("text/plain")
                .bizType("metadata-test")
                .build();
        FileMetadata uploadedMetadata = dfsService.upload(request, "database");

        // When
        FileMetadata retrievedMetadata = dfsService.getMetadata(uploadedMetadata.getFileId(), "database");

        // Then
        assertThat(retrievedMetadata).isNotNull();
        assertThat(retrievedMetadata.getFileId()).isEqualTo(uploadedMetadata.getFileId());
        assertThat(retrievedMetadata.getFilename()).isEqualTo(TEST_FILENAME);
    }

    @Test
    @Order(7)
    @DisplayName("Should handle multiple file uploads")
    void testMultipleUploads() throws IOException {
        // Given
        FileUploadRequest request1 = FileUploadRequest.builder()
                .filename("file1.txt")
                .inputStream(new ByteArrayInputStream("Content 1".getBytes(StandardCharsets.UTF_8)))
                .build();
        FileUploadRequest request2 = FileUploadRequest.builder()
                .filename("file2.txt")
                .inputStream(new ByteArrayInputStream("Content 2".getBytes(StandardCharsets.UTF_8)))
                .build();
        FileUploadRequest request3 = FileUploadRequest.builder()
                .filename("file3.txt")
                .inputStream(new ByteArrayInputStream("Content 3".getBytes(StandardCharsets.UTF_8)))
                .build();

        // When
        FileMetadata metadata1 = dfsService.upload(request1, "database");
        FileMetadata metadata2 = dfsService.upload(request2, "database");
        FileMetadata metadata3 = dfsService.upload(request3, "database");

        // Then
        assertThat(metadata1.getFileId()).isNotEqualTo(metadata2.getFileId());
        assertThat(metadata2.getFileId()).isNotEqualTo(metadata3.getFileId());
        assertThat(dfsService.exists(metadata1.getFileId(), "database")).isTrue();
        assertThat(dfsService.exists(metadata2.getFileId(), "database")).isTrue();
        assertThat(dfsService.exists(metadata3.getFileId(), "database")).isTrue();
    }

    @Test
    @Order(8)
    @DisplayName("Should handle files with same content but different names")
    void testSameContentDifferentNames() throws IOException {
        // Given - Same content, different names
        FileUploadRequest request1 = FileUploadRequest.builder()
                .filename("duplicate1.txt")
                .inputStream(new ByteArrayInputStream(TEST_CONTENT.getBytes(StandardCharsets.UTF_8)))
                .build();
        FileUploadRequest request2 = FileUploadRequest.builder()
                .filename("duplicate2.txt")
                .inputStream(new ByteArrayInputStream(TEST_CONTENT.getBytes(StandardCharsets.UTF_8)))
                .build();

        // When
        FileMetadata metadata1 = dfsService.upload(request1, "database");
        FileMetadata metadata2 = dfsService.upload(request2, "database");

        // Then - Different file IDs but same hash
        assertThat(metadata1.getFileId()).isNotEqualTo(metadata2.getFileId());
        assertThat(metadata1.getHash()).isEqualTo(metadata2.getHash());
    }

    @Test
    @Order(9)
    @DisplayName("Should handle files with metadata")
    void testUploadWithCustomMetadata() throws IOException {
        // Given
        Map<String, String> customMetadata = new HashMap<>();
        customMetadata.put("author", "test-author");
        customMetadata.put("version", "1.0");
        customMetadata.put("tags", "test,service,dfs");

        FileUploadRequest request = FileUploadRequest.builder()
                .filename(TEST_FILENAME)
                .inputStream(new ByteArrayInputStream(TEST_CONTENT.getBytes(StandardCharsets.UTF_8)))
                .metadata(customMetadata)
                .build();

        // When
        FileMetadata metadata = dfsService.upload(request);

        // Then
        assertThat(metadata.getMetadata()).isEqualTo(customMetadata);
    }

    @Test
    @Order(10)
    @DisplayName("Should handle different content types")
    void testDifferentContentTypes() throws IOException {
        // Given
        FileUploadRequest textRequest = FileUploadRequest.builder()
                .filename("text.txt")
                .contentType("text/plain")
                .inputStream(new ByteArrayInputStream("text".getBytes(StandardCharsets.UTF_8)))
                .build();
        FileUploadRequest jsonRequest = FileUploadRequest.builder()
                .filename("data.json")
                .contentType("application/json")
                .inputStream(new ByteArrayInputStream("{\"key\":\"value\"}".getBytes(StandardCharsets.UTF_8)))
                .build();
        FileUploadRequest pdfRequest = FileUploadRequest.builder()
                .filename("document.pdf")
                .contentType("application/pdf")
                .inputStream(new ByteArrayInputStream(new byte[]{0x25, 0x50, 0x44, 0x46}))
                .build();

        // When
        FileMetadata textMetadata = dfsService.upload(textRequest, "database");
        FileMetadata jsonMetadata = dfsService.upload(jsonRequest, "database");
        FileMetadata pdfMetadata = dfsService.upload(pdfRequest, "database");

        // Then
        assertThat(textMetadata.getContentType()).isEqualTo("text/plain");
        assertThat(jsonMetadata.getContentType()).isEqualTo("application/json");
        assertThat(pdfMetadata.getContentType()).isEqualTo("application/pdf");
    }
}

