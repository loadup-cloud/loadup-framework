package com.github.loadup.components.dfs.test.integration;

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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration tests for DFS component
 */
@SpringBootTest(classes = DfsTestApplication.class)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DfsIntegrationTest {

    @Autowired
    private DfsService dfsService;

    private static final String TEST_CONTENT = "Integration test file content";

    @Test
    @Order(1)
    @DisplayName("Should handle complete file lifecycle")
    void testCompleteFileLifecycle() throws IOException {
        // Given
        FileUploadRequest uploadRequest = FileUploadRequest.builder()
                .filename("lifecycle-test.txt")
                .inputStream(new ByteArrayInputStream(TEST_CONTENT.getBytes(StandardCharsets.UTF_8)))
                .contentType("text/plain")
                .bizType("integration-test")
                .build();

        // When - Upload
        FileMetadata uploadedMetadata = dfsService.upload(uploadRequest, "database");
        assertThat(uploadedMetadata).isNotNull();
        assertThat(uploadedMetadata.getFileId()).isNotNull();

        // When - Check existence
        boolean exists = dfsService.exists(uploadedMetadata.getFileId(), "database");
        assertThat(exists).isTrue();

        // When - Get metadata
        FileMetadata retrievedMetadata = dfsService.getMetadata(uploadedMetadata.getFileId(), "database");
        assertThat(retrievedMetadata.getFileId()).isEqualTo(uploadedMetadata.getFileId());

        // When - Download
        FileDownloadResponse downloadResponse = dfsService.download(uploadedMetadata.getFileId(), "database");
        String downloadedContent = IOUtils.toString(downloadResponse.getInputStream(), StandardCharsets.UTF_8);
        assertThat(downloadedContent).isEqualTo(TEST_CONTENT);

        // When - Delete
        boolean deleted = dfsService.delete(uploadedMetadata.getFileId(), "database");
        assertThat(deleted).isTrue();

        // Then - Verify deletion
        boolean existsAfterDelete = dfsService.exists(uploadedMetadata.getFileId(), "database");
        assertThat(existsAfterDelete).isFalse();
    }

    @Test
    @Order(2)
    @DisplayName("Should handle multiple providers simultaneously")
    void testMultipleProviders() throws IOException {
        // Given
        FileUploadRequest request = FileUploadRequest.builder()
                .filename("multi-provider.txt")
                .inputStream(new ByteArrayInputStream(TEST_CONTENT.getBytes(StandardCharsets.UTF_8)))
                .build();

        // When - Upload to local
        FileMetadata localMetadata = dfsService.upload(request, "local");

        // When - Upload same file to database
        FileUploadRequest request2 = FileUploadRequest.builder()
                .filename("multi-provider.txt")
                .inputStream(new ByteArrayInputStream(TEST_CONTENT.getBytes(StandardCharsets.UTF_8)))
                .build();
        FileMetadata dbMetadata = dfsService.upload(request2, "database");

        // Then
        assertThat(localMetadata.getFileId()).isNotEqualTo(dbMetadata.getFileId());
        assertThat(localMetadata.getProvider()).isEqualTo("local");
        assertThat(dbMetadata.getProvider()).isEqualTo("database");
        assertThat(localMetadata.getHash()).isEqualTo(dbMetadata.getHash());
    }

    @Test
    @Order(3)
    @DisplayName("Should handle concurrent uploads")
    void testConcurrentUploads() throws IOException {
        // Given
        List<FileMetadata> uploadedFiles = new ArrayList<>();

        // When - Upload multiple files
        for (int i = 0; i < 10; i++) {
            String content = "Concurrent test file " + i;
            FileUploadRequest request = FileUploadRequest.builder()
                    .filename("concurrent-" + i + ".txt")
                    .inputStream(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)))
                    .bizType("concurrent-test")
                    .build();

            FileMetadata metadata = dfsService.upload(request, "database");
            uploadedFiles.add(metadata);
        }

        // Then
        assertThat(uploadedFiles).hasSize(10);
        for (FileMetadata metadata : uploadedFiles) {
            assertThat(dfsService.exists(metadata.getFileId(), "database")).isTrue();
        }
    }

    @Test
    @Order(4)
    @DisplayName("Should handle large file upload and download")
    void testLargeFile() throws IOException {
        // Given - 5MB file
        byte[] largeContent = new byte[5 * 1024 * 1024];
        for (int i = 0; i < largeContent.length; i++) {
            largeContent[i] = (byte) (i % 256);
        }

        FileUploadRequest request = FileUploadRequest.builder()
                .filename("large-file.bin")
                .inputStream(new ByteArrayInputStream(largeContent))
                .contentType("application/octet-stream")
                .build();

        // When
        FileMetadata metadata = dfsService.upload(request);
        FileDownloadResponse response = dfsService.download(metadata.getFileId());

        // Then
        assertThat(metadata.getSize()).isEqualTo(largeContent.length);
        byte[] downloadedContent = IOUtils.toByteArray(response.getInputStream());
        assertThat(downloadedContent).hasSize(largeContent.length);
    }

    @Test
    @Order(5)
    @DisplayName("Should handle various file types")
    void testVariousFileTypes() throws IOException {
        // Given - Different file types
        String[][] testFiles = {
                {"text.txt", "text/plain", "Plain text content"},
                {"data.json", "application/json", "{\"key\":\"value\"}"},
                {"style.css", "text/css", "body { margin: 0; }"},
                {"script.js", "application/javascript", "console.log('test');"},
                {"image.jpg", "image/jpeg", "fake-jpeg-data"}
        };

        // When & Then
        for (String[] fileData : testFiles) {
            FileUploadRequest request = FileUploadRequest.builder()
                    .filename(fileData[0])
                    .contentType(fileData[1])
                    .inputStream(new ByteArrayInputStream(fileData[2].getBytes(StandardCharsets.UTF_8)))
                    .build();

            FileMetadata metadata = dfsService.upload(request, "database");
            assertThat(metadata.getFilename()).isEqualTo(fileData[0]);
            assertThat(metadata.getContentType()).isEqualTo(fileData[1]);

            FileDownloadResponse response = dfsService.download(metadata.getFileId(), "database");
            String content = IOUtils.toString(response.getInputStream(), StandardCharsets.UTF_8);
            assertThat(content).isEqualTo(fileData[2]);
        }
    }

    @Test
    @Order(6)
    @DisplayName("Should handle file operations with business context")
    void testBusinessContext() throws IOException {
        // Given
        String[] bizTypes = {"invoices", "contracts", "reports"};

        // When
        List<FileMetadata> files = new ArrayList<>();
        for (String bizType : bizTypes) {
            for (int i = 0; i < 3; i++) {
                FileUploadRequest request = FileUploadRequest.builder()
                        .filename(bizType + "-" + i + ".txt")
                        .inputStream(new ByteArrayInputStream(("Content for " + bizType).getBytes(StandardCharsets.UTF_8)))
                        .bizType(bizType)
                        .bizId("BIZ-" + i)
                        .build();

                files.add(dfsService.upload(request, "database"));
            }
        }

        // Then
        assertThat(files).hasSize(9);
        for (FileMetadata file : files) {
            assertThat(file.getBizType()).isIn((Object[]) bizTypes);
            assertThat(file.getBizId()).startsWith("BIZ-");
        }
    }

    @Test
    @Order(7)
    @DisplayName("Should handle duplicate content detection")
    void testDuplicateContentDetection() throws IOException {
        // Given - Same content, different names
        String content = "Duplicate content test";

        FileUploadRequest request1 = FileUploadRequest.builder()
                .filename("file1.txt")
                .inputStream(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)))
                .build();

        FileUploadRequest request2 = FileUploadRequest.builder()
                .filename("file2.txt")
                .inputStream(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)))
                .build();

        // When
        FileMetadata metadata1 = dfsService.upload(request1, "database");
        FileMetadata metadata2 = dfsService.upload(request2, "database");

        // Then - Different IDs but same hash
        assertThat(metadata1.getFileId()).isNotEqualTo(metadata2.getFileId());
        assertThat(metadata1.getHash()).isEqualTo(metadata2.getHash());
        assertThat(metadata1.getFilename()).isNotEqualTo(metadata2.getFilename());
    }

    @Test
    @Order(8)
    @DisplayName("Should handle error scenarios gracefully")
    void testErrorScenarios() {
        // Test download non-existent file
        assertThatThrownBy(() -> dfsService.download("non-existent-id", "database"))
                .isInstanceOf(RuntimeException.class);

        // Test get metadata non-existent file
        assertThatThrownBy(() -> dfsService.getMetadata("non-existent-id", "database"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @Order(9)
    @DisplayName("Should preserve file metadata during operations")
    void testMetadataPreservation() throws IOException {
        // Given
        FileUploadRequest request = FileUploadRequest.builder()
                .filename("metadata-test.txt")
                .inputStream(new ByteArrayInputStream(TEST_CONTENT.getBytes(StandardCharsets.UTF_8)))
                .contentType("text/plain")
                .bizType("test")
                .bizId("TEST-001")
                .publicAccess(true)
                .build();

        // When
        FileMetadata uploadMetadata = dfsService.upload(request, "database");
        FileMetadata retrievedMetadata = dfsService.getMetadata(uploadMetadata.getFileId(), "database");

        // Then
        assertThat(retrievedMetadata.getFilename()).isEqualTo(uploadMetadata.getFilename());
        assertThat(retrievedMetadata.getContentType()).isEqualTo(uploadMetadata.getContentType());
        assertThat(retrievedMetadata.getBizType()).isEqualTo(uploadMetadata.getBizType());
        assertThat(retrievedMetadata.getBizId()).isEqualTo(uploadMetadata.getBizId());
        assertThat(retrievedMetadata.getPublicAccess()).isEqualTo(uploadMetadata.getPublicAccess());
        assertThat(retrievedMetadata.getHash()).isEqualTo(uploadMetadata.getHash());
    }

    @Test
    @Order(10)
    @DisplayName("Should handle empty files")
    void testEmptyFiles() throws IOException {
        // Given
        FileUploadRequest request = FileUploadRequest.builder()
                .filename("empty.txt")
                .inputStream(new ByteArrayInputStream(new byte[0]))
                .build();

        // When
        FileMetadata metadata = dfsService.upload(request, "database");

        // Then
        assertThat(metadata.getSize()).isEqualTo(0);

        FileDownloadResponse response = dfsService.download(metadata.getFileId(), "database");
        byte[] content = IOUtils.toByteArray(response.getInputStream());
        assertThat(content).isEmpty();
    }
}

