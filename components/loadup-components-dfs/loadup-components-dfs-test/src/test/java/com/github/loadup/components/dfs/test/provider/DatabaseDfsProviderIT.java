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

import static org.assertj.core.api.Assertions.*;

import com.github.loadup.components.dfs.binder.database.DatabaseDfsProvider;
import com.github.loadup.components.dfs.config.DfsProperties;
import com.github.loadup.components.dfs.enums.FileStatus;
import com.github.loadup.components.dfs.model.FileDownloadResponse;
import com.github.loadup.components.dfs.model.FileMetadata;
import com.github.loadup.components.dfs.model.FileUploadRequest;
import com.github.loadup.components.dfs.test.DfsTestApplication;
import com.github.loadup.components.dfs.test.config.TestContainersConfiguration;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/** Test cases for DatabaseDfsProvider with Testcontainers MySQL */
@SpringBootTest(classes = DfsTestApplication.class)
@Import(TestContainersConfiguration.class)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DatabaseDfsProviderIT {

  @Autowired private DatabaseDfsProvider databaseDfsProvider;

  @Autowired private DfsProperties dfsProperties;

  private static final String TEST_CONTENT = "Database storage test content.";
  private static final String TEST_FILENAME = "db-test-file.txt";

  @BeforeEach
  void setUp() {
    DfsProperties.ProviderConfig config = new DfsProperties.ProviderConfig();
    config.setEnabled(true);
  }

  @Test
  @Order(1)
  @DisplayName("Should upload file to database successfully")
  void testUploadToDatabase() throws IOException {
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
    assertThat(metadata).isNotNull();
    assertThat(metadata.getFileId()).isNotNull();
    assertThat(metadata.getFilename()).isEqualTo(TEST_FILENAME);
    assertThat(metadata.getSize()).isEqualTo(TEST_CONTENT.length());
    assertThat(metadata.getProvider()).isEqualTo("database");
    assertThat(metadata.getStatus()).isEqualTo(FileStatus.AVAILABLE);
    assertThat(metadata.getHash()).isNotNull();
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
    assertThat(response).isNotNull();
    assertThat(response.getMetadata()).isNotNull();
    assertThat(response.getMetadata().getFileId()).isEqualTo(uploadedMetadata.getFileId());
    assertThat(response.getInputStream()).isNotNull();

    String content = IOUtils.toString(response.getInputStream(), StandardCharsets.UTF_8);
    assertThat(content).isEqualTo(TEST_CONTENT);
  }

  @Test
  @Order(3)
  @DisplayName("Should delete file from database successfully")
  void testDeleteFromDatabase() throws IOException {
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
    assertThat(deleted).isTrue();
    assertThat(databaseDfsProvider.exists(metadata.getFileId())).isFalse();
  }

  @Test
  @Order(4)
  @DisplayName("Should check file existence correctly")
  void testFileExists() throws IOException {
    // Given - Upload a file
    FileUploadRequest request =
        FileUploadRequest.builder()
            .filename(TEST_FILENAME)
            .inputStream(new ByteArrayInputStream(TEST_CONTENT.getBytes(StandardCharsets.UTF_8)))
            .build();
    FileMetadata metadata = databaseDfsProvider.upload(request);

    // When & Then
    assertThat(databaseDfsProvider.exists(metadata.getFileId())).isTrue();
    assertThat(databaseDfsProvider.exists("non-existent-id")).isFalse();
  }

  @Test
  @Order(5)
  @DisplayName("Should get metadata correctly")
  void testGetMetadata() throws IOException {
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
    assertThat(retrievedMetadata).isNotNull();
    assertThat(retrievedMetadata.getFileId()).isEqualTo(uploadedMetadata.getFileId());
    assertThat(retrievedMetadata.getFilename()).isEqualTo(TEST_FILENAME);
    assertThat(retrievedMetadata.getSize()).isEqualTo(TEST_CONTENT.length());
    assertThat(retrievedMetadata.getContentType()).isEqualTo("text/plain");
  }

  @Test
  @Order(6)
  @DisplayName("Should return provider name")
  void testGetProviderName() {
    // When
    String providerName = databaseDfsProvider.getProviderName();

    // Then
    assertThat(providerName).isEqualTo("database");
  }

  @Test
  @Order(7)
  @DisplayName("Should check availability")
  void testIsAvailable() {
    // When
    boolean available = databaseDfsProvider.isAvailable();

    // Then
    assertThat(available).isTrue();
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
    assertThat(downloadedContent).isEqualTo(binaryContent);
  }

  @Test
  @Order(9)
  @DisplayName("Should handle download of non-existent file")
  void testDownloadNonExistentFile() {
    // When & Then
    assertThatThrownBy(() -> databaseDfsProvider.download("non-existent-id"))
        .isInstanceOf(RuntimeException.class);
  }

  @Test
  @Order(10)
  @DisplayName("Should handle metadata retrieval of non-existent file")
  void testGetMetadataNonExistentFile() {
    // When & Then
    assertThatThrownBy(() -> databaseDfsProvider.getMetadata("non-existent-id"))
        .isInstanceOf(RuntimeException.class);
  }

  @Test
  @Order(11)
  @DisplayName("Should store file hash correctly")
  void testFileHashStorage() throws IOException {
    // Given
    FileUploadRequest request =
        FileUploadRequest.builder()
            .filename(TEST_FILENAME)
            .inputStream(new ByteArrayInputStream(TEST_CONTENT.getBytes(StandardCharsets.UTF_8)))
            .build();

    // When
    FileMetadata metadata = databaseDfsProvider.upload(request);

    // Then
    assertThat(metadata.getHash()).isNotNull();
    assertThat(metadata.getHash()).hasSize(32); // MD5 hash length
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
    assertThat(metadata.getSize()).isEqualTo(1);
    String content = IOUtils.toString(response.getInputStream(), StandardCharsets.UTF_8);
    assertThat(content).isEqualTo(smallContent);
  }
}
