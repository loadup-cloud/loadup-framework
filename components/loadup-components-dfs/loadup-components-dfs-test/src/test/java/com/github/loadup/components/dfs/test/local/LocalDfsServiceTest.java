package com.github.loadup.components.dfs.test.local;

/*-
 * #%L
 * loadup-components-dfs-binder-local
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

import com.github.loadup.components.dfs.binder.AbstractDfsBinder;
import com.github.loadup.components.dfs.binding.DfsBinding;
import com.github.loadup.components.dfs.manager.DfsBindingManager;
import com.github.loadup.components.dfs.model.*;
import com.github.loadup.components.dfs.test.TestApplication;
import com.github.loadup.framework.api.annotation.BindingClient;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Integration test for DFS Service using LOCAL provider.
 *
 * <p>This test is located in the local binder module, ensuring complete isolation from other
 * providers. No need to exclude any auto-configurations - the classpath only contains the local
 * binder.
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@SpringBootTest(classes = TestApplication.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LocalDfsServiceTest {

  @BindingClient("user-avatar")
  DfsBinding localBinding;

  @BindingClient("order-pdf")
  DfsBinding orderBinding;

  @Autowired private DfsBindingManager manager;

  private static final String TEST_CONTENT = "DFS Service test content.";
  private static final String TEST_FILENAME = "service-test.txt";

  @Test
  @Order(0)
  @DisplayName("Should choose right provider")
  void testDynamicBindingSelection() {
    // 1. 验证通过 Manager 获取到的实例
    DfsBinding binding = manager.getBinding("user-avatar");
    DfsBinding binding2 = manager.getBinding("order-pdf");
    assertNotNull(binding);
    assertNotNull(binding2);

    // 2. 验证注入注解是否生效
    assertNotNull(localBinding);
    assertNotNull(orderBinding);
    assertEquals(binding, localBinding);
    assertEquals(binding2, orderBinding);

    // 3. 验证 Binder 是否被正确装配 (S3 类型)
    // 假设 S3DfsBinding 暴露了获取类型的方法
  }

  @Test
  @Order(1)
  @DisplayName("Should upload file using local provider")
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
    FileMetadata metadata = localBinding.upload(request);

    // Then
    assertNotNull(metadata);
    assertNotNull(metadata.getFileId());
    assertEquals(TEST_FILENAME, metadata.getFilename());
    assertEquals("local", metadata.getProvider());
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
    FileMetadata uploadedMetadata = localBinding.upload(uploadRequest);

    // When
    FileDownloadResponse response = localBinding.download(uploadedMetadata.getFileId());

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
    FileMetadata metadata = localBinding.upload(request);

    // When
    boolean deleted = localBinding.delete(metadata.getFileId());

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
    FileMetadata metadata = localBinding.upload(request);

    // When & Then
    assertTrue(localBinding.exists(metadata.getFileId()));
    assertFalse(localBinding.exists("non-existent"));
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
    FileMetadata uploadedMetadata = localBinding.upload(request);

    // When
    FileMetadata retrievedMetadata = localBinding.getMetadata(uploadedMetadata.getFileId());

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
    FileMetadata metadata1 = localBinding.upload(request1);
    FileMetadata metadata2 = localBinding.upload(request2);
    FileMetadata metadata3 = localBinding.upload(request3);

    // Then
    assertNotEquals(metadata1.getFileId(), metadata2.getFileId());
    assertNotEquals(metadata2.getFileId(), metadata3.getFileId());
    assertTrue(localBinding.exists(metadata1.getFileId()));
    assertTrue(localBinding.exists(metadata2.getFileId()));
    assertTrue(localBinding.exists(metadata3.getFileId()));
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
    FileMetadata metadata1 = localBinding.upload(request1);
    FileMetadata metadata2 = localBinding.upload(request2);

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
    FileMetadata metadata = localBinding.upload(request);

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
    FileMetadata textMetadata = localBinding.upload(textRequest);
    FileMetadata jsonMetadata = localBinding.upload(jsonRequest);
    FileMetadata pdfMetadata = localBinding.upload(pdfRequest);

    // Then
    assertEquals("text/plain", textMetadata.getContentType());
    assertEquals("application/json", jsonMetadata.getContentType());
    assertEquals("application/pdf", pdfMetadata.getContentType());
  }
}
