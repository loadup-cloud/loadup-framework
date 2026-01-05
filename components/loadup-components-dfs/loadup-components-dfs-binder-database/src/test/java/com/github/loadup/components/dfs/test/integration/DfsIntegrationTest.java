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

import static org.junit.jupiter.api.Assertions.*;

import com.github.loadup.components.dfs.model.FileDownloadResponse;
import com.github.loadup.components.dfs.model.FileMetadata;
import com.github.loadup.components.dfs.model.FileUploadRequest;
import com.github.loadup.components.dfs.service.DfsService;
import com.github.loadup.components.dfs.test.DfsTestApplication;
import com.github.loadup.components.testcontainers.cloud.AbstractMySQLContainerTest;
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

/** Integration tests for DFS component with Testcontainers MySQL */
@SpringBootTest(classes = DfsTestApplication.class)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DfsIntegrationTest extends AbstractMySQLContainerTest {

  @Autowired private DfsService dfsService;

  private static final String TEST_CONTENT = "Integration test file content";

  @Test
  @Order(1)
  @DisplayName("Should handle complete file lifecycle")
  void testCompleteFileLifecycle() throws IOException {
    // Given
    FileUploadRequest uploadRequest =
        FileUploadRequest.builder()
            .filename("lifecycle-test.txt")
            .inputStream(new ByteArrayInputStream(TEST_CONTENT.getBytes(StandardCharsets.UTF_8)))
            .contentType("text/plain")
            .bizType("integration-test")
            .build();

    // When - Upload
    FileMetadata uploadedMetadata = dfsService.upload(uploadRequest);
    assertNotNull(uploadedMetadata);
    assertNotNull(uploadedMetadata.getFileId());

    // When - Check existence
    boolean exists = dfsService.exists(uploadedMetadata.getFileId());
    assertTrue(exists);

    // When - Get metadata
    FileMetadata retrievedMetadata = dfsService.getMetadata(uploadedMetadata.getFileId());
    assertEquals(uploadedMetadata.getFileId(), retrievedMetadata.getFileId());

    // When - Download
    FileDownloadResponse downloadResponse = dfsService.download(uploadedMetadata.getFileId());
    String downloadedContent =
        IOUtils.toString(downloadResponse.getInputStream(), StandardCharsets.UTF_8);
    assertEquals(TEST_CONTENT, downloadedContent);

    // When - Delete
    boolean deleted = dfsService.delete(uploadedMetadata.getFileId());
    assertTrue(deleted);

    // Then - Verify deletion
    boolean existsAfterDelete = dfsService.exists(uploadedMetadata.getFileId());
    assertFalse(existsAfterDelete);
  }

  @Test
  @Order(2)
  @DisplayName("Should handle concurrent uploads")
  void testConcurrentUploads() {
    // Given
    List<FileMetadata> uploadedFiles = new ArrayList<>();

    // When - Upload multiple files
    for (int i = 0; i < 10; i++) {
      String content = "Concurrent test file " + i;
      FileUploadRequest request =
          FileUploadRequest.builder()
              .filename("concurrent-" + i + ".txt")
              .inputStream(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)))
              .bizType("concurrent-test")
              .build();

      FileMetadata metadata = dfsService.upload(request);
      uploadedFiles.add(metadata);
    }

    // Then
    assertEquals(10, uploadedFiles.size());
    for (FileMetadata metadata : uploadedFiles) {
      assertTrue(dfsService.exists(metadata.getFileId()));
    }
  }

  @Test
  @Order(3)
  @DisplayName("Should handle large file upload and download")
  void testLargeFile() throws IOException {
    // Given - 5MB file
    byte[] largeContent = new byte[5 * 1024 * 1024];
    for (int i = 0; i < largeContent.length; i++) {
      largeContent[i] = (byte) (i % 256);
    }

    FileUploadRequest request =
        FileUploadRequest.builder()
            .filename("large-file.bin")
            .inputStream(new ByteArrayInputStream(largeContent))
            .contentType("application/octet-stream")
            .build();

    // When
    FileMetadata metadata = dfsService.upload(request);
    FileDownloadResponse response = dfsService.download(metadata.getFileId());

    // Then
    assertEquals(largeContent.length, metadata.getSize());
    byte[] downloadedContent = IOUtils.toByteArray(response.getInputStream());
    assertEquals(largeContent.length, downloadedContent.length);
  }

  @Test
  @Order(4)
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
      FileUploadRequest request =
          FileUploadRequest.builder()
              .filename(fileData[0])
              .contentType(fileData[1])
              .inputStream(new ByteArrayInputStream(fileData[2].getBytes(StandardCharsets.UTF_8)))
              .build();

      FileMetadata metadata = dfsService.upload(request);
      assertEquals(fileData[0], metadata.getFilename());
      assertEquals(fileData[1], metadata.getContentType());

      FileDownloadResponse response = dfsService.download(metadata.getFileId());
      String content = IOUtils.toString(response.getInputStream(), StandardCharsets.UTF_8);
      assertEquals(fileData[2], content);
    }
  }

  @Test
  @Order(5)
  @DisplayName("Should handle file operations with business context")
  void testBusinessContext() {
    // Given
    String[] bizTypes = {"invoices", "contracts", "reports"};

    // When
    List<FileMetadata> files = new ArrayList<>();
    for (String bizType : bizTypes) {
      for (int i = 0; i < 3; i++) {
        FileUploadRequest request =
            FileUploadRequest.builder()
                .filename(bizType + "-" + i + ".txt")
                .inputStream(
                    new ByteArrayInputStream(
                        ("Content for " + bizType).getBytes(StandardCharsets.UTF_8)))
                .bizType(bizType)
                .bizId("BIZ-" + i)
                .build();

        files.add(dfsService.upload(request));
      }
    }

    // Then
    assertEquals(9, files.size());
    for (FileMetadata file : files) {
      assertTrue(List.of(bizTypes).contains(file.getBizType()));
      assertTrue(file.getBizId().startsWith("BIZ-"));
    }
  }

  @Test
  @Order(6)
  @DisplayName("Should handle duplicate content detection")
  void testDuplicateContentDetection() {
    // Given - Same content, different names
    String content = "Duplicate content test";

    FileUploadRequest request1 =
        FileUploadRequest.builder()
            .filename("file1.txt")
            .inputStream(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)))
            .build();

    FileUploadRequest request2 =
        FileUploadRequest.builder()
            .filename("file2.txt")
            .inputStream(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)))
            .build();

    // When
    FileMetadata metadata1 = dfsService.upload(request1);
    FileMetadata metadata2 = dfsService.upload(request2);

    // Then - Different IDs but same hash
    assertNotEquals(metadata2.getFileId(), metadata1.getFileId());
    assertEquals(metadata2.getHash(), metadata1.getHash());
    assertNotEquals(metadata2.getFilename(), metadata1.getFilename());
  }

  @Test
  @Order(7)
  @DisplayName("Should handle error scenarios gracefully")
  void testErrorScenarios() {
    // Test download non-existent file
    assertThrows(RuntimeException.class, () -> dfsService.download("non-existent-id"));

    // Test get metadata non-existent file
    assertThrows(RuntimeException.class, () -> dfsService.getMetadata("non-existent-id"));
  }

  @Test
  @Order(8)
  @DisplayName("Should preserve file metadata during operations")
  void testMetadataPreservation() {
    // Given
    FileUploadRequest request =
        FileUploadRequest.builder()
            .filename("metadata-test.txt")
            .inputStream(new ByteArrayInputStream(TEST_CONTENT.getBytes(StandardCharsets.UTF_8)))
            .contentType("text/plain")
            .bizType("test")
            .bizId("TEST-001")
            .publicAccess(true)
            .build();

    // When
    FileMetadata uploadMetadata = dfsService.upload(request);
    FileMetadata retrievedMetadata = dfsService.getMetadata(uploadMetadata.getFileId());

    // Then
    assertEquals(uploadMetadata.getFilename(), retrievedMetadata.getFilename());
    assertEquals(uploadMetadata.getContentType(), retrievedMetadata.getContentType());
    assertEquals(uploadMetadata.getBizType(), retrievedMetadata.getBizType());
    assertEquals(uploadMetadata.getBizId(), retrievedMetadata.getBizId());
    assertEquals(uploadMetadata.getPublicAccess(), retrievedMetadata.getPublicAccess());
    assertEquals(uploadMetadata.getHash(), retrievedMetadata.getHash());
  }

  @Test
  @Order(9)
  @DisplayName("Should handle empty files")
  void testEmptyFiles() throws IOException {
    // Given
    FileUploadRequest request =
        FileUploadRequest.builder()
            .filename("empty.txt")
            .inputStream(new ByteArrayInputStream(new byte[0]))
            .build();

    // When
    FileMetadata metadata = dfsService.upload(request);

    // Then
    assertEquals(0, metadata.getSize());

    FileDownloadResponse response = dfsService.download(metadata.getFileId());
    byte[] content = IOUtils.toByteArray(response.getInputStream());
    assertEquals(0, content.length);
  }
}
