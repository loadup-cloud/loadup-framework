package com.github.loadup.components.dfs.test;

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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = DfsTestApplication.class)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LocalDfsServiceIT {

  @Autowired private DfsService dfsService;

    private static final String TEST_CONTENT = "DFS Service test content.";
    private static final String TEST_FILENAME = "service-test.txt";

  @Test
  @Order(1)
  @DisplayName("Should upload file using default provider")
  void testUploadWithDefaultProvider() {
    // Given
    FileUploadRequest request =
        FileUploadRequest.builder()
            .filename(TEST_FILENAME)
            .inputStream(new ByteArrayInputStream(TEST_CONTENT.getBytes(StandardCharsets.UTF_8)))
            .contentType("text/plain")
            .bizType("service-test")
            .build();

        // When
        FileMetadata metadata = dfsService.upload(request);

    // Then
    assertNotNull(metadata);
    assertNotNull(metadata.getFileId());
    assertEquals(TEST_FILENAME, metadata.getFilename());
    assertEquals("local", metadata.getProvider()); // default provider from config
    }

    @Test
    @Order(2)
    @DisplayName("Should download file successfully")
    void testDownload() throws IOException {
    // Given - Upload a file first
    FileUploadRequest uploadRequest =
        FileUploadRequest.builder()
            .filename(TEST_FILENAME)
            .inputStream(new ByteArrayInputStream(TEST_CONTENT.getBytes(StandardCharsets.UTF_8)))
            .build();
    FileMetadata uploadedMetadata = dfsService.upload(uploadRequest);

    // When
    FileDownloadResponse response = dfsService.download(uploadedMetadata.getFileId());

    // Then
    assertNotNull(response);
    assertNotNull(response.getMetadata());
        String content = IOUtils.toString(response.getInputStream(), StandardCharsets.UTF_8);
    assertEquals(TEST_CONTENT, content);
    }

  @Test
  @Order(3)
  @DisplayName("Should delete file successfully")
  void testDelete() {
    // Given - Upload a file first
    FileUploadRequest request =
        FileUploadRequest.builder()
            .filename(TEST_FILENAME)
            .inputStream(new ByteArrayInputStream(TEST_CONTENT.getBytes(StandardCharsets.UTF_8)))
            .build();
    FileMetadata metadata = dfsService.upload(request);

    // When
    boolean deleted = dfsService.delete(metadata.getFileId());

    // Then
    assertTrue(deleted);
    }

  @Test
  @Order(4)
  @DisplayName("Should check file existence")
  void testExists() {
    // Given - Upload a file
    FileUploadRequest request =
        FileUploadRequest.builder()
            .filename(TEST_FILENAME)
            .inputStream(new ByteArrayInputStream(TEST_CONTENT.getBytes(StandardCharsets.UTF_8)))
            .build();
    FileMetadata metadata = dfsService.upload(request);

    // When & Then
    assertTrue(dfsService.exists(metadata.getFileId()));
    assertFalse(dfsService.exists("non-existent"));
    }

  @Test
  @Order(5)
  @DisplayName("Should get file metadata")
  void testGetMetadata() {
    // Given - Upload a file
    FileUploadRequest request =
        FileUploadRequest.builder()
            .filename(TEST_FILENAME)
            .inputStream(new ByteArrayInputStream(TEST_CONTENT.getBytes(StandardCharsets.UTF_8)))
            .contentType("text/plain")
            .bizType("metadata-test")
            .build();
    FileMetadata uploadedMetadata = dfsService.upload(request);

    // When
    FileMetadata retrievedMetadata = dfsService.getMetadata(uploadedMetadata.getFileId());

    // Then
    assertNotNull(retrievedMetadata);
    assertEquals(uploadedMetadata.getFileId(), retrievedMetadata.getFileId());
    assertEquals(TEST_FILENAME, retrievedMetadata.getFilename());
    }

  @Test
  @Order(6)
  @DisplayName("Should handle multiple file uploads")
  void testMultipleUploads() {
    // Given
    FileUploadRequest request1 =
        FileUploadRequest.builder()
            .filename("file1.txt")
            .inputStream(new ByteArrayInputStream("Content 1".getBytes(StandardCharsets.UTF_8)))
            .build();
    FileUploadRequest request2 =
        FileUploadRequest.builder()
            .filename("file2.txt")
            .inputStream(new ByteArrayInputStream("Content 2".getBytes(StandardCharsets.UTF_8)))
            .build();
    FileUploadRequest request3 =
        FileUploadRequest.builder()
            .filename("file3.txt")
            .inputStream(new ByteArrayInputStream("Content 3".getBytes(StandardCharsets.UTF_8)))
            .build();

    // When
    FileMetadata metadata1 = dfsService.upload(request1);
    FileMetadata metadata2 = dfsService.upload(request2);
    FileMetadata metadata3 = dfsService.upload(request3);

    // Then
    assertNotEquals(metadata1.getFileId(), metadata2.getFileId());
    assertNotEquals(metadata2.getFileId(), metadata3.getFileId());
    assertTrue(dfsService.exists(metadata1.getFileId()));
    assertTrue(dfsService.exists(metadata2.getFileId()));
    assertTrue(dfsService.exists(metadata3.getFileId()));
    }

  @Test
  @Order(7)
  @DisplayName("Should handle files with same content but different names")
  void testSameContentDifferentNames() {
    // Given - Same content, different names
    FileUploadRequest request1 =
        FileUploadRequest.builder()
            .filename("duplicate1.txt")
            .inputStream(new ByteArrayInputStream(TEST_CONTENT.getBytes(StandardCharsets.UTF_8)))
            .build();
    FileUploadRequest request2 =
        FileUploadRequest.builder()
            .filename("duplicate2.txt")
            .inputStream(new ByteArrayInputStream(TEST_CONTENT.getBytes(StandardCharsets.UTF_8)))
            .build();

    // When
    FileMetadata metadata1 = dfsService.upload(request1);
    FileMetadata metadata2 = dfsService.upload(request2);

    // Then - Different file IDs but same hash
    assertNotEquals(metadata1.getFileId(), metadata2.getFileId());
    assertEquals(metadata1.getHash(), metadata2.getHash());
    }

  @Test
  @Order(8)
  @DisplayName("Should handle files with metadata")
  void testUploadWithCustomMetadata() {
        // Given
        Map<String, String> customMetadata = new HashMap<>();
        customMetadata.put("author", "test-author");
        customMetadata.put("version", "1.0");
        customMetadata.put("tags", "test,service,dfs");

    FileUploadRequest request =
        FileUploadRequest.builder()
            .filename(TEST_FILENAME)
            .inputStream(new ByteArrayInputStream(TEST_CONTENT.getBytes(StandardCharsets.UTF_8)))
            .metadata(customMetadata)
            .build();

        // When
        FileMetadata metadata = dfsService.upload(request);

    // Then
    assertEquals(customMetadata, metadata.getMetadata());
    }

  @Test
  @Order(9)
  @DisplayName("Should handle different content types")
  void testDifferentContentTypes() {
    // Given
    FileUploadRequest textRequest =
        FileUploadRequest.builder()
            .filename("text.txt")
            .contentType("text/plain")
            .inputStream(new ByteArrayInputStream("text".getBytes(StandardCharsets.UTF_8)))
            .build();
    FileUploadRequest jsonRequest =
        FileUploadRequest.builder()
            .filename("data.json")
            .contentType("application/json")
            .inputStream(
                new ByteArrayInputStream("{\"key\":\"value\"}".getBytes(StandardCharsets.UTF_8)))
            .build();
    FileUploadRequest pdfRequest =
        FileUploadRequest.builder()
            .filename("document.pdf")
            .contentType("application/pdf")
            .inputStream(new ByteArrayInputStream(new byte[] {0x25, 0x50, 0x44, 0x46}))
            .build();

    // When
    FileMetadata textMetadata = dfsService.upload(textRequest);
    FileMetadata jsonMetadata = dfsService.upload(jsonRequest);
    FileMetadata pdfMetadata = dfsService.upload(pdfRequest);

    // Then
    assertEquals("text/plain", textMetadata.getContentType());
    assertEquals("application/json", jsonMetadata.getContentType());
    assertEquals("application/pdf", pdfMetadata.getContentType());
    }
}
