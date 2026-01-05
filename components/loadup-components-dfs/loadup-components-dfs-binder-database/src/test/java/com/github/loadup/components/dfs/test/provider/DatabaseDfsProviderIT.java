package com.github.loadup.components.dfs.test.provider;

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

import com.github.loadup.components.dfs.binder.database.DatabaseDfsProvider;
import com.github.loadup.components.dfs.enums.FileStatus;
import com.github.loadup.components.dfs.model.FileDownloadResponse;
import com.github.loadup.components.dfs.model.FileMetadata;
import com.github.loadup.components.dfs.model.FileUploadRequest;
import com.github.loadup.components.dfs.test.DfsTestApplication;
import com.github.loadup.components.testcontainers.cloud.AbstractMySQLContainerTest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/** Test cases for DatabaseDfsProvider with Testcontainers MySQL */
@SpringBootTest(classes = DfsTestApplication.class)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DatabaseDfsProviderIT extends AbstractMySQLContainerTest {

  @Autowired private DatabaseDfsProvider databaseDfsProvider;

  private static final String TEST_CONTENT = "Database storage test content.";
  private static final String TEST_FILENAME = "db-test-file.txt";

  @Test
  @Order(1)
  @DisplayName("Should upload file to database successfully")
  void testUploadToDatabase() {
    // Given
    FileUploadRequest request =
        FileUploadRequest.builder()
            .filename(TEST_FILENAME)
            .inputStream(new ByteArrayInputStream(TEST_CONTENT.getBytes(StandardCharsets.UTF_8)))
            .contentType("text/plain")
            .bizType("test-docs")
            .bizId("DB-001")
            .publicAccess(false)
            .build();

    // When
    FileMetadata metadata = databaseDfsProvider.upload(request);

    // Then
    assertNotNull(metadata);
    assertNotNull(metadata.getFileId());
    assertEquals(TEST_FILENAME, metadata.getFilename());
    assertEquals(TEST_CONTENT.length(), metadata.getSize());
    assertEquals("database", metadata.getProvider());
    assertEquals(FileStatus.AVAILABLE, metadata.getStatus());
    assertNotNull(metadata.getHash());
  }

  @Test
  @Order(2)
  @DisplayName("Should download file from database successfully")
  void testDownloadFromDatabase() throws IOException {
    // Given - Upload a file first
    FileUploadRequest uploadRequest =
        FileUploadRequest.builder()
            .filename(TEST_FILENAME)
            .inputStream(new ByteArrayInputStream(TEST_CONTENT.getBytes(StandardCharsets.UTF_8)))
            .contentType("text/plain")
            .build();
    FileMetadata uploadedMetadata = databaseDfsProvider.upload(uploadRequest);

    // When
    FileDownloadResponse response = databaseDfsProvider.download(uploadedMetadata.getFileId());

    // Then
    assertNotNull(response);
    assertNotNull(response.getMetadata());
    assertEquals(uploadedMetadata.getFileId(), response.getMetadata().getFileId());
    assertNotNull(response.getInputStream());

    String content = IOUtils.toString(response.getInputStream(), StandardCharsets.UTF_8);
    assertEquals(TEST_CONTENT, content);
  }

  @Test
  @Order(3)
  @DisplayName("Should delete file from database successfully")
  void testDeleteFromDatabase() {
    // Given - Upload a file first
    FileUploadRequest uploadRequest =
        FileUploadRequest.builder()
            .filename(TEST_FILENAME)
            .inputStream(new ByteArrayInputStream(TEST_CONTENT.getBytes(StandardCharsets.UTF_8)))
            .build();
    FileMetadata metadata = databaseDfsProvider.upload(uploadRequest);

    // When
    boolean deleted = databaseDfsProvider.delete(metadata.getFileId());

    // Then
    assertTrue(deleted);
    assertFalse(databaseDfsProvider.exists(metadata.getFileId()));
  }

  @Test
  @Order(4)
  @DisplayName("Should check file existence correctly")
  void testFileExists() {
    // Given - Upload a file
    FileUploadRequest request =
        FileUploadRequest.builder()
            .filename(TEST_FILENAME)
            .inputStream(new ByteArrayInputStream(TEST_CONTENT.getBytes(StandardCharsets.UTF_8)))
            .build();
    FileMetadata metadata = databaseDfsProvider.upload(request);

    // When & Then
    assertTrue(databaseDfsProvider.exists(metadata.getFileId()));
    assertFalse(databaseDfsProvider.exists("non-existent-id"));
  }

  @Test
  @Order(5)
  @DisplayName("Should get metadata correctly")
  void testGetMetadata() {
    // Given - Upload a file
    FileUploadRequest request =
        FileUploadRequest.builder()
            .filename(TEST_FILENAME)
            .inputStream(new ByteArrayInputStream(TEST_CONTENT.getBytes(StandardCharsets.UTF_8)))
            .contentType("text/plain")
            .bizType("test-type")
            .build();
    FileMetadata uploadedMetadata = databaseDfsProvider.upload(request);

    // When
    FileMetadata retrievedMetadata = databaseDfsProvider.getMetadata(uploadedMetadata.getFileId());

    // Then
    assertNotNull(retrievedMetadata);
    assertEquals(uploadedMetadata.getFileId(), retrievedMetadata.getFileId());
    assertEquals(TEST_FILENAME, retrievedMetadata.getFilename());
    assertEquals(TEST_CONTENT.length(), retrievedMetadata.getSize());
    assertEquals("text/plain", retrievedMetadata.getContentType());
  }

  @Test
  @Order(6)
  @DisplayName("Should return provider name")
  void testGetProviderName() {
    // When
    String providerName = databaseDfsProvider.getProviderName();

    // Then
    assertEquals("database", providerName);
  }

  @Test
  @Order(7)
  @DisplayName("Should check availability")
  void testIsAvailable() {
    // When
    boolean available = databaseDfsProvider.isAvailable();

    // Then
    assertTrue(available);
  }

  @Test
  @Order(8)
  @DisplayName("Should handle binary content correctly")
  void testBinaryContent() throws IOException {
    // Given - Binary content
    byte[] binaryContent = new byte[] {0x00, 0x01, 0x02, (byte) 0xFF, (byte) 0xFE};
    FileUploadRequest request =
        FileUploadRequest.builder()
            .filename("binary-file.bin")
            .inputStream(new ByteArrayInputStream(binaryContent))
            .contentType("application/octet-stream")
            .build();

    // When
    FileMetadata metadata = databaseDfsProvider.upload(request);
    FileDownloadResponse response = databaseDfsProvider.download(metadata.getFileId());

    // Then
    byte[] downloadedContent = IOUtils.toByteArray(response.getInputStream());
    assertArrayEquals(binaryContent, downloadedContent);
  }

  @Test
  @Order(9)
  @DisplayName("Should handle download of non-existent file")
  void testDownloadNonExistentFile() {
    // When & Then
    assertThrows(RuntimeException.class, () -> databaseDfsProvider.download("non-existent-id"));
  }

  @Test
  @Order(10)
  @DisplayName("Should handle metadata retrieval of non-existent file")
  void testGetMetadataNonExistentFile() {
    // When & Then
    assertThrows(RuntimeException.class, () -> databaseDfsProvider.getMetadata("non-existent-id"));
  }

  @Test
  @Order(11)
  @DisplayName("Should store file hash correctly")
  void testFileHashStorage() {
    // Given
    FileUploadRequest request =
        FileUploadRequest.builder()
            .filename(TEST_FILENAME)
            .inputStream(new ByteArrayInputStream(TEST_CONTENT.getBytes(StandardCharsets.UTF_8)))
            .build();

    // When
    FileMetadata metadata = databaseDfsProvider.upload(request);

    // Then
    assertNotNull(metadata.getHash());
    assertEquals(32, metadata.getHash().length()); // MD5 hash length
  }

  @Test
  @Order(12)
  @DisplayName("Should handle small files efficiently")
  void testSmallFiles() throws IOException {
    // Given - Very small file
    String smallContent = "x";
    FileUploadRequest request =
        FileUploadRequest.builder()
            .filename("small.txt")
            .inputStream(new ByteArrayInputStream(smallContent.getBytes(StandardCharsets.UTF_8)))
            .build();

    // When
    FileMetadata metadata = databaseDfsProvider.upload(request);
    FileDownloadResponse response = databaseDfsProvider.download(metadata.getFileId());

    // Then
    assertEquals(1, metadata.getSize());
    String content = IOUtils.toString(response.getInputStream(), StandardCharsets.UTF_8);
    assertEquals(smallContent, content);
  }
}
