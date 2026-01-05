package com.github.loadup.components.dfs.test.binder.s3;

/*-
 * #%L
 * loadup-components-dfs-binder-s3
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
import com.github.loadup.components.testcontainers.cloud.AbstractLocalStackContainerTest;
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

/**
 * Integration test for DFS Service using S3 provider.
 *
 * <p>This test is located in the S3 binder module, ensuring complete isolation from other
 * providers. No need to exclude any auto-configurations - the classpath only contains the S3
 * binder.
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@SpringBootTest(classes = DfsTestApplication.class)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class S3DfsServiceIT extends AbstractLocalStackContainerTest {

  @Autowired private DfsService dfsService;

  private static final String TEST_CONTENT = "DFS Service test content.";
  private static final String TEST_FILENAME = "service-test.txt";

  @Test
  @Order(1)
  @DisplayName("Should upload file using s3 provider")
  public void testUploadWithDefaultProvider() {
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
    assertEquals("s3", metadata.getProvider());
  }

  @Test
  @Order(2)
  @DisplayName("Should download file successfully")
  public void testDownload() throws IOException {
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
  public void testDelete() {
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
  public void testExists() {
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
  @DisplayName("Should get file metadata with complete information")
  public void testGetMetadata() {
    // Given - Upload a file with complete metadata
    FileUploadRequest request =
        FileUploadRequest.builder()
            .filename(TEST_FILENAME)
            .inputStream(new ByteArrayInputStream(TEST_CONTENT.getBytes(StandardCharsets.UTF_8)))
            .contentType("text/plain")
            .bizType("metadata-test")
            .bizId("test-biz-123")
            .publicAccess(true)
            .build();
    FileMetadata uploadedMetadata = dfsService.upload(request);

    // When
    FileMetadata retrievedMetadata = dfsService.getMetadata(uploadedMetadata.getFileId());

    // Then - Verify all metadata fields
    assertNotNull(retrievedMetadata, "Retrieved metadata should not be null");
    assertEquals(
        uploadedMetadata.getFileId(), retrievedMetadata.getFileId(), "File ID should match");
    assertEquals(TEST_FILENAME, retrievedMetadata.getFilename(), "Filename should match");
    assertEquals("text/plain", retrievedMetadata.getContentType(), "Content type should match");
    assertEquals("metadata-test", retrievedMetadata.getBizType(), "Business type should match");
    assertEquals("test-biz-123", retrievedMetadata.getBizId(), "Business ID should match");
    assertEquals("s3", retrievedMetadata.getProvider(), "Provider should be s3");
    assertTrue(retrievedMetadata.getPublicAccess(), "Public access should be true");
    assertNotNull(retrievedMetadata.getPath(), "Path should not be null");
    assertNotNull(retrievedMetadata.getHash(), "Hash should not be null");
    assertNotNull(retrievedMetadata.getUploadTime(), "Upload time should not be null");
    assertEquals(
        Long.valueOf(TEST_CONTENT.length()), retrievedMetadata.getSize(), "File size should match");
    assertNotNull(retrievedMetadata.getStatus(), "Status should not be null");
  }

  @Test
  @Order(6)
  @DisplayName("Should handle multiple file uploads")
  public void testMultipleUploads() {
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
  public void testSameContentDifferentNames() {
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
  @DisplayName("Should handle files with custom metadata and retrieve it correctly")
  public void testUploadWithCustomMetadata() {
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
    FileMetadata uploadedMetadata = dfsService.upload(request);

    // Then - Verify custom metadata in upload response
    assertNotNull(uploadedMetadata.getMetadata(), "Custom metadata should not be null");
    assertEquals(customMetadata, uploadedMetadata.getMetadata(), "Custom metadata should match");

    // Also verify by retrieving metadata
    FileMetadata retrievedMetadata = dfsService.getMetadata(uploadedMetadata.getFileId());
    assertNotNull(retrievedMetadata.getMetadata(), "Retrieved custom metadata should not be null");
    assertEquals(
        "test-author", retrievedMetadata.getMetadata().get("author"), "Author should match");
    assertEquals("1.0", retrievedMetadata.getMetadata().get("version"), "Version should match");
    assertEquals(
        "test,service,dfs", retrievedMetadata.getMetadata().get("tags"), "Tags should match");
  }

  @Test
  @Order(9)
  @DisplayName("Should handle different content types")
  public void testDifferentContentTypes() {
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

  @Test
  @Order(10)
  @DisplayName("Should persist metadata correctly - verify .meta file and S3 object metadata")
  public void testMetadataPersistence() {
    // Given - Upload file with complete metadata
    Map<String, String> customMetadata = new HashMap<>();
    customMetadata.put("project", "dfs-framework");
    customMetadata.put("environment", "test");

    FileUploadRequest request =
        FileUploadRequest.builder()
            .filename("persistence-test.txt")
            .inputStream(new ByteArrayInputStream("test content".getBytes(StandardCharsets.UTF_8)))
            .contentType("text/plain")
            .bizType("persistence")
            .bizId("persist-123")
            .publicAccess(false)
            .metadata(customMetadata)
            .build();

    // When - Upload and retrieve multiple times
    FileMetadata uploadedMetadata = dfsService.upload(request);
    FileMetadata retrieved1 = dfsService.getMetadata(uploadedMetadata.getFileId());
    FileMetadata retrieved2 = dfsService.getMetadata(uploadedMetadata.getFileId());

    // Then - All retrievals should return consistent metadata
    assertEquals(uploadedMetadata.getFileId(), retrieved1.getFileId());
    assertEquals(uploadedMetadata.getFileId(), retrieved2.getFileId());
    assertEquals(uploadedMetadata.getFilename(), retrieved1.getFilename());
    assertEquals(uploadedMetadata.getFilename(), retrieved2.getFilename());
    assertEquals(uploadedMetadata.getBizType(), retrieved1.getBizType());
    assertEquals(uploadedMetadata.getBizType(), retrieved2.getBizType());
    assertEquals(uploadedMetadata.getHash(), retrieved1.getHash());
    assertEquals(uploadedMetadata.getHash(), retrieved2.getHash());
    assertFalse(retrieved1.getPublicAccess(), "Public access should be false");
    assertFalse(retrieved2.getPublicAccess(), "Public access should be false");

    // Verify custom metadata persists
    assertNotNull(retrieved1.getMetadata());
    assertNotNull(retrieved2.getMetadata());
    assertEquals("dfs-framework", retrieved1.getMetadata().get("project"));
    assertEquals("test", retrieved1.getMetadata().get("environment"));
  }

  @Test
  @Order(11)
  @DisplayName("Should handle metadata with special characters and Unicode")
  public void testMetadataWithSpecialCharacters() {
    // Given - Metadata with special characters
    Map<String, String> customMetadata = new HashMap<>();
    customMetadata.put("description", "文件描述：测试特殊字符 & symbols!");
    customMetadata.put("tags", "中文,English,日本語");
    customMetadata.put("author", "张三 (Zhang San)");

    FileUploadRequest request =
        FileUploadRequest.builder()
            .filename("unicode-test-文件.txt")
            .inputStream(
                new ByteArrayInputStream("Unicode content 中文内容".getBytes(StandardCharsets.UTF_8)))
            .metadata(customMetadata)
            .build();

    // When
    FileMetadata uploadedMetadata = dfsService.upload(request);
    FileMetadata retrievedMetadata = dfsService.getMetadata(uploadedMetadata.getFileId());

    // Then - Special characters should be preserved
    assertNotNull(retrievedMetadata.getMetadata());
    assertEquals("文件描述：测试特殊字符 & symbols!", retrievedMetadata.getMetadata().get("description"));
    assertEquals("中文,English,日本語", retrievedMetadata.getMetadata().get("tags"));
    assertEquals("张三 (Zhang San)", retrievedMetadata.getMetadata().get("author"));
    assertEquals("unicode-test-文件.txt", retrievedMetadata.getFilename());
  }

  @Test
  @Order(12)
  @DisplayName("Should update metadata fields correctly")
  public void testMetadataFieldUpdates() {
    // Given - Upload file without bizId and publicAccess
    FileUploadRequest request1 =
        FileUploadRequest.builder()
            .filename("update-test.txt")
            .inputStream(new ByteArrayInputStream("content".getBytes(StandardCharsets.UTF_8)))
            .bizType("test")
            .build();

    FileMetadata metadata1 = dfsService.upload(request1);

    // Then - Verify initial state
    assertNull(metadata1.getBizId(), "BizId should be null initially");
    assertFalse(metadata1.getPublicAccess(), "PublicAccess should be null initially");

    // Given - Upload another file with all fields
    FileUploadRequest request2 =
        FileUploadRequest.builder()
            .filename("update-test-2.txt")
            .inputStream(new ByteArrayInputStream("content".getBytes(StandardCharsets.UTF_8)))
            .bizType("test")
            .bizId("test-123")
            .publicAccess(true)
            .build();

    FileMetadata metadata2 = dfsService.upload(request2);

    // Then - Verify all fields are set
    assertEquals("test-123", metadata2.getBizId(), "BizId should be set");
    assertTrue(metadata2.getPublicAccess(), "PublicAccess should be true");

    // Verify metadata retrieval
    FileMetadata retrieved2 = dfsService.getMetadata(metadata2.getFileId());
    assertEquals("test-123", retrieved2.getBizId(), "Retrieved BizId should match");
    assertTrue(retrieved2.getPublicAccess(), "Retrieved PublicAccess should be true");
  }

  @Test
  @Order(13)
  @DisplayName("Should handle empty and null metadata correctly")
  public void testEmptyAndNullMetadata() {
    // Given - File with null custom metadata
    FileUploadRequest request1 =
        FileUploadRequest.builder()
            .filename("null-metadata.txt")
            .inputStream(new ByteArrayInputStream("content".getBytes(StandardCharsets.UTF_8)))
            .metadata(null)
            .build();

    // When
    FileMetadata metadata1 = dfsService.upload(request1);
    FileMetadata retrieved1 = dfsService.getMetadata(metadata1.getFileId());

    // Then
    assertNotNull(metadata1, "Uploaded metadata should not be null");
    assertNotNull(retrieved1, "Retrieved metadata should not be null");
    assertEquals(metadata1.getFileId(), retrieved1.getFileId(), "File IDs should match");
    // Custom metadata can be null (this is valid)

    // Given - File with empty custom metadata
    FileUploadRequest request2 =
        FileUploadRequest.builder()
            .filename("empty-metadata.txt")
            .inputStream(new ByteArrayInputStream("content".getBytes(StandardCharsets.UTF_8)))
            .metadata(new HashMap<>())
            .build();

    // When
    FileMetadata metadata2 = dfsService.upload(request2);
    FileMetadata retrieved2 = dfsService.getMetadata(metadata2.getFileId());

    // Then - Should handle empty metadata gracefully
    assertNotNull(metadata2);
    assertNotNull(retrieved2);
  }

  @Test
  @Order(14)
  @DisplayName("Should verify metadata after download operation")
  public void testMetadataAfterDownload() throws IOException {
    // Given
    Map<String, String> customMetadata = new HashMap<>();
    customMetadata.put("download-test", "true");

    FileUploadRequest request =
        FileUploadRequest.builder()
            .filename("download-metadata-test.txt")
            .inputStream(new ByteArrayInputStream(TEST_CONTENT.getBytes(StandardCharsets.UTF_8)))
            .contentType("text/plain")
            .metadata(customMetadata)
            .build();

    FileMetadata uploadedMetadata = dfsService.upload(request);

    // When - Download the file
    FileDownloadResponse downloadResponse = dfsService.download(uploadedMetadata.getFileId());

    // Then - Verify metadata in download response
    assertNotNull(
        downloadResponse.getMetadata(), "Metadata in download response should not be null");
    assertEquals(uploadedMetadata.getFileId(), downloadResponse.getMetadata().getFileId());
    assertEquals(uploadedMetadata.getFilename(), downloadResponse.getMetadata().getFilename());
    assertEquals(
        uploadedMetadata.getContentType(), downloadResponse.getMetadata().getContentType());
    assertNotNull(downloadResponse.getMetadata().getMetadata());
    assertEquals("true", downloadResponse.getMetadata().getMetadata().get("download-test"));

    // Verify file content
    String content = IOUtils.toString(downloadResponse.getInputStream(), StandardCharsets.UTF_8);
    assertEquals(TEST_CONTENT, content);
  }
}
