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

import com.github.loadup.components.dfs.binder.local.LocalDfsProvider;
import com.github.loadup.components.dfs.config.DfsProperties;
import com.github.loadup.components.dfs.enums.FileStatus;
import com.github.loadup.components.dfs.model.FileMetadata;
import com.github.loadup.components.dfs.model.FileUploadRequest;
import com.github.loadup.components.dfs.test.DfsTestApplication;
import java.io.ByteArrayInputStream;
import java.io.File;
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

/**
 * Test cases for LocalDfsProvider
 */
@SpringBootTest(classes = DfsTestApplication.class)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LocalDfsProviderTest {

    @Autowired
    private LocalDfsProvider localDfsProvider;

    @Autowired
    private DfsProperties dfsProperties;

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
            Files.walk(path)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }

    @Test
    @Order(1)
    @DisplayName("Should upload file successfully")
    void testUploadFile() throws IOException {
        // Given
        FileUploadRequest request = FileUploadRequest.builder()
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
        assertThat(metadata).isNotNull();
        assertThat(metadata.getFileId()).isNotNull().isNotEmpty();
        assertThat(metadata.getFilename()).isEqualTo(TEST_FILENAME);
        assertThat(metadata.getSize()).isEqualTo(TEST_CONTENT.length());
        assertThat(metadata.getContentType()).isEqualTo("text/plain");
        assertThat(metadata.getProvider()).isEqualTo("local");
        assertThat(metadata.getBizType()).isEqualTo(TEST_BIZ_TYPE);
        assertThat(metadata.getBizId()).isEqualTo("TEST-001");
        assertThat(metadata.getStatus()).isEqualTo(FileStatus.AVAILABLE);
        assertThat(metadata.getHash()).isNotNull().isNotEmpty();
        assertThat(metadata.getUploadTime()).isNotNull();
        assertThat(metadata.getPath()).contains(TEST_BIZ_TYPE);
    }

    @Test
    @Order(2)
    @DisplayName("Should upload file with metadata")
    void testUploadFileWithMetadata() throws IOException {
        // Given
        Map<String, String> customMetadata = new HashMap<>();
        customMetadata.put("author", "test-user");
        customMetadata.put("department", "engineering");

        FileUploadRequest request = FileUploadRequest.builder()
                .filename(TEST_FILENAME)
                .inputStream(new ByteArrayInputStream(TEST_CONTENT.getBytes(StandardCharsets.UTF_8)))
                .contentType("text/plain")
                .bizType(TEST_BIZ_TYPE)
                .metadata(customMetadata)
                .build();

        // When
        FileMetadata metadata = localDfsProvider.upload(request);

        // Then
        assertThat(metadata).isNotNull();
        assertThat(metadata.getMetadata()).isEqualTo(customMetadata);
    }

    @Test
    @Order(3)
    @DisplayName("Should calculate file hash correctly")
    void testFileHashCalculation() throws IOException {
        // Given
        FileUploadRequest request = FileUploadRequest.builder()
                .filename(TEST_FILENAME)
                .inputStream(new ByteArrayInputStream(TEST_CONTENT.getBytes(StandardCharsets.UTF_8)))
                .build();

        // When
        FileMetadata metadata1 = localDfsProvider.upload(request);

        FileUploadRequest request2 = FileUploadRequest.builder()
                .filename("another-file.txt")
                .inputStream(new ByteArrayInputStream(TEST_CONTENT.getBytes(StandardCharsets.UTF_8)))
                .build();
        FileMetadata metadata2 = localDfsProvider.upload(request2);

        // Then - Same content should produce same hash
        assertThat(metadata1.getHash()).isEqualTo(metadata2.getHash());
    }

    @Test
    @Order(4)
    @DisplayName("Should return provider name")
    void testGetProviderName() {
        // When
        String providerName = localDfsProvider.getProviderName();

        // Then
        assertThat(providerName).isEqualTo("local");
    }

    @Test
    @Order(5)
    @DisplayName("Should check availability correctly")
    void testIsAvailable() {
        // When
        boolean available = localDfsProvider.isAvailable();

        // Then
        assertThat(available).isTrue();
    }

    @Test
    @Order(6)
    @DisplayName("Should handle upload failure gracefully")
    void testUploadFailure() {
        // Given - Invalid input stream
        FileUploadRequest request = FileUploadRequest.builder()
                .filename(TEST_FILENAME)
                .inputStream(null)
                .build();

        // When & Then
        assertThatThrownBy(() -> localDfsProvider.upload(request))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @Order(7)
    @DisplayName("Should create directory structure correctly")
    void testDirectoryStructure() throws IOException {
        // Given
        FileUploadRequest request = FileUploadRequest.builder()
                .filename(TEST_FILENAME)
                .inputStream(new ByteArrayInputStream(TEST_CONTENT.getBytes(StandardCharsets.UTF_8)))
                .bizType(TEST_BIZ_TYPE)
                .build();

        // When
        FileMetadata metadata = localDfsProvider.upload(request);

        // Then - Check directory structure
        Path filePath = Paths.get(testBasePath, metadata.getPath());
        assertThat(Files.exists(filePath)).isTrue();
        assertThat(metadata.getPath()).contains(TEST_BIZ_TYPE);
        assertThat(metadata.getPath()).matches(".*\\d{4}/\\d{2}/\\d{2}/.*");
    }

    @Test
    @Order(8)
    @DisplayName("Should handle large files")
    void testLargeFileUpload() throws IOException {
        // Given - 1MB file
        byte[] largeContent = new byte[1024 * 1024];
        for (int i = 0; i < largeContent.length; i++) {
            largeContent[i] = (byte) (i % 256);
        }

        FileUploadRequest request = FileUploadRequest.builder()
                .filename("large-file.bin")
                .inputStream(new ByteArrayInputStream(largeContent))
                .contentType("application/octet-stream")
                .build();

        // When
        FileMetadata metadata = localDfsProvider.upload(request);

        // Then
        assertThat(metadata).isNotNull();
        assertThat(metadata.getSize()).isEqualTo(largeContent.length);
    }

    @Test
    @Order(9)
    @DisplayName("Should handle files without bizType")
    void testUploadWithoutBizType() throws IOException {
        // Given
        FileUploadRequest request = FileUploadRequest.builder()
                .filename(TEST_FILENAME)
                .inputStream(new ByteArrayInputStream(TEST_CONTENT.getBytes(StandardCharsets.UTF_8)))
                .build();

        // When
        FileMetadata metadata = localDfsProvider.upload(request);

        // Then
        assertThat(metadata).isNotNull();
        assertThat(metadata.getFileId()).isNotNull();
        assertThat(metadata.getPath()).matches(".*\\d{4}/\\d{2}/\\d{2}/.*");
    }

    @Test
    @Order(10)
    @DisplayName("Should upload files with public access flag")
    void testPublicAccessFlag() throws IOException {
        // Given
        FileUploadRequest request = FileUploadRequest.builder()
                .filename(TEST_FILENAME)
                .inputStream(new ByteArrayInputStream(TEST_CONTENT.getBytes(StandardCharsets.UTF_8)))
                .publicAccess(true)
                .build();

        // When
        FileMetadata metadata = localDfsProvider.upload(request);

        // Then
        assertThat(metadata.getPublicAccess()).isTrue();
    }
}

