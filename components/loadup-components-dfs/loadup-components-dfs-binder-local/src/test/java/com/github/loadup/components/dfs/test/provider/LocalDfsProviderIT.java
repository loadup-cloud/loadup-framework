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

import com.github.loadup.components.dfs.binder.local.LocalDfsProvider;
import com.github.loadup.components.dfs.config.DfsProperties;
import com.github.loadup.components.dfs.enums.FileStatus;
import com.github.loadup.components.dfs.model.FileMetadata;
import com.github.loadup.components.dfs.model.FileUploadRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestPropertySource(properties = {"loadup.dfs.default-provider=local"})
class LocalDfsProviderIT {

  @Autowired private LocalDfsProvider localDfsProvider;

  @Autowired private DfsProperties dfsProperties;

  private static final String TEST_CONTENT = "This is test file content for DFS testing.";
  private static final String TEST_FILENAME = "test-file.txt";
  private static final String TEST_BIZ_TYPE = "test-documents";

  private String testBasePath;

  @BeforeEach
  void setUp() {
    testBasePath = System.getProperty("java.io.tmpdir") + "/dfs-test";
    DfsProperties.ProviderConfig config = new DfsProperties.ProviderConfig();
    config.setEnabled(true);
    config.setBasePath(testBasePath);
    dfsProperties.getProviders().put("local", config);
  }

  @AfterEach
  void tearDown() throws IOException {
    // Clean up test files
    Path path = Paths.get(testBasePath);
    if (Files.exists(path)) {
      try (var stream = Files.walk(path)) {
        stream
            .sorted(Comparator.reverseOrder())
            .map(Path::toFile)
            .forEach(
                file -> {
                  if (!file.delete()) {
                    System.err.println("Failed to delete: " + file.getAbsolutePath());
                  }
                });
      }
    }
  }

  @Test
  @Order(1)
  @DisplayName("Should upload file successfully")
  void testUploadFile() {
    // Given
    FileUploadRequest request =
        FileUploadRequest.builder()
            .filename(TEST_FILENAME)
            .inputStream(new ByteArrayInputStream(TEST_CONTENT.getBytes(StandardCharsets.UTF_8)))
            .contentType("text/plain")
            .bizType(TEST_BIZ_TYPE)
            .bizId("TEST-001")
            .publicAccess(false)
            .build();

    // When
    FileMetadata metadata = localDfsProvider.upload(request);

    // Then
    assertNotNull(metadata);
    assertNotNull(metadata.getFileId());
    assertFalse(metadata.getFileId().isEmpty());
    assertEquals(TEST_FILENAME, metadata.getFilename());
    assertEquals(TEST_CONTENT.length(), metadata.getSize());
    assertEquals("text/plain", metadata.getContentType());
    assertEquals("local", metadata.getProvider());
    assertEquals(TEST_BIZ_TYPE, metadata.getBizType());
    assertEquals("TEST-001", metadata.getBizId());
    assertEquals(FileStatus.AVAILABLE, metadata.getStatus());
    assertNotNull(metadata.getHash());
    assertFalse(metadata.getHash().isEmpty());
    assertNotNull(metadata.getUploadTime());
    assertTrue(metadata.getPath().contains(TEST_BIZ_TYPE));
  }

  @Test
  @Order(2)
  @DisplayName("Should upload file with metadata")
  void testUploadFileWithMetadata() {
    // Given
    Map<String, String> customMetadata = new HashMap<>();
    customMetadata.put("author", "test-user");
    customMetadata.put("department", "engineering");

    FileUploadRequest request =
        FileUploadRequest.builder()
            .filename(TEST_FILENAME)
            .inputStream(new ByteArrayInputStream(TEST_CONTENT.getBytes(StandardCharsets.UTF_8)))
            .contentType("text/plain")
            .bizType(TEST_BIZ_TYPE)
            .metadata(customMetadata)
            .build();

    // When
    FileMetadata metadata = localDfsProvider.upload(request);

    // Then
    assertNotNull(metadata);
    assertEquals(customMetadata, metadata.getMetadata());
  }

  @Test
  @Order(3)
  @DisplayName("Should calculate file hash correctly")
  void testFileHashCalculation() {
    // Given
    FileUploadRequest request =
        FileUploadRequest.builder()
            .filename(TEST_FILENAME)
            .inputStream(new ByteArrayInputStream(TEST_CONTENT.getBytes(StandardCharsets.UTF_8)))
            .build();

    // When
    FileMetadata metadata1 = localDfsProvider.upload(request);

    FileUploadRequest request2 =
        FileUploadRequest.builder()
            .filename("another-file.txt")
            .inputStream(new ByteArrayInputStream(TEST_CONTENT.getBytes(StandardCharsets.UTF_8)))
            .build();
    FileMetadata metadata2 = localDfsProvider.upload(request2);

    // Then - Same content should produce same hash
    assertEquals(metadata1.getHash(), metadata2.getHash());
  }

  @Test
  @Order(4)
  @DisplayName("Should return provider name")
  void testGetProviderName() {
    // When
    String providerName = localDfsProvider.getProviderName();

    // Then
    assertEquals("local", providerName);
  }

  @Test
  @Order(5)
  @DisplayName("Should check availability correctly")
  void testIsAvailable() {
    // When
    boolean available = localDfsProvider.isAvailable();

    // Then
    assertTrue(available);
  }

  @Test
  @Order(6)
  @DisplayName("Should handle upload failure gracefully")
  void testUploadFailure() {
    // Given - Invalid input stream
    FileUploadRequest request =
        FileUploadRequest.builder().filename(TEST_FILENAME).inputStream(null).build();

    // When & Then
    assertThrows(RuntimeException.class, () -> localDfsProvider.upload(request));
  }

  @Test
  @Order(7)
  @DisplayName("Should create directory structure correctly")
  void testDirectoryStructure() {
    // Given
    FileUploadRequest request =
        FileUploadRequest.builder()
            .filename(TEST_FILENAME)
            .inputStream(new ByteArrayInputStream(TEST_CONTENT.getBytes(StandardCharsets.UTF_8)))
            .bizType(TEST_BIZ_TYPE)
            .build();

    // When
    FileMetadata metadata = localDfsProvider.upload(request);

    // Then - Check directory structure
    Path filePath = Paths.get(testBasePath, metadata.getPath());
    assertTrue(Files.exists(filePath));
    assertTrue(metadata.getPath().contains(TEST_BIZ_TYPE));
    assertTrue(metadata.getPath().matches(".*\\d{4}/\\d{2}/\\d{2}/.*"));
  }

  @Test
  @Order(8)
  @DisplayName("Should handle large files")
  void testLargeFileUpload() {
    // Given - 1MB file
    byte[] largeContent = new byte[1024 * 1024];
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
    FileMetadata metadata = localDfsProvider.upload(request);

    // Then
    assertNotNull(metadata);
    assertEquals(largeContent.length, metadata.getSize());
  }

  @Test
  @Order(9)
  @DisplayName("Should handle files without bizType")
  void testUploadWithoutBizType() {
    // Given
    FileUploadRequest request =
        FileUploadRequest.builder()
            .filename(TEST_FILENAME)
            .inputStream(new ByteArrayInputStream(TEST_CONTENT.getBytes(StandardCharsets.UTF_8)))
            .build();

    // When
    FileMetadata metadata = localDfsProvider.upload(request);

    // Then
    assertNotNull(metadata);
    assertNotNull(metadata.getFileId());
    assertTrue(metadata.getPath().matches(".*\\d{4}/\\d{2}/\\d{2}/.*"));
  }

  @Test
  @Order(10)
  @DisplayName("Should upload files with public access flag")
  void testPublicAccessFlag() {
    // Given
    FileUploadRequest request =
        FileUploadRequest.builder()
            .filename(TEST_FILENAME)
            .inputStream(new ByteArrayInputStream(TEST_CONTENT.getBytes(StandardCharsets.UTF_8)))
            .publicAccess(true)
            .build();

    // When
    FileMetadata metadata = localDfsProvider.upload(request);

    // Then
    assertTrue(metadata.getPublicAccess());
  }
}
