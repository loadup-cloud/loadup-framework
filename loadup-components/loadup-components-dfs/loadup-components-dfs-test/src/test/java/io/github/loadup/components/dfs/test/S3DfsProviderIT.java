/*-
 * #%L
 * LoadUp Components DFS Test
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.github.loadup.components.dfs.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.loadup.components.dfs.DfsService;
import io.github.loadup.components.dfs.model.FileDownloadResponse;
import io.github.loadup.components.dfs.model.FileMetadata;
import io.github.loadup.components.dfs.model.FileUploadRequest;
import io.github.loadup.components.dfs.model.MultipartPart;
import io.github.loadup.components.dfs.model.MultipartUpload;
import io.github.loadup.components.dfs.model.MultipartUploadRequest;
import io.github.loadup.components.testcontainers.annotation.ContainerType;
import io.github.loadup.components.testcontainers.annotation.EnableTestContainers;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = TestApplication.class)
@EnableTestContainers(ContainerType.LOCALSTACK)
@ActiveProfiles("test")
@TestPropertySource(
        properties = {
            "loadup.dfs.binder-type=s3",
            "loadup.dfs.binder.s3.endpoint=${aws.s3.endpoint}",
            "loadup.dfs.binder.s3.access-key=${aws.access-key-id}",
            "loadup.dfs.binder.s3.secret-key=${aws.secret-access-key}",
            "loadup.dfs.binder.s3.region=${aws.region}",
            "loadup.dfs.binder.s3.bucket=loadup-dfs-contract",
            "loadup.dfs.binder.s3.path-style-access-enabled=true",
            "loadup.dfs.binder.s3.create-bucket=true",
            "loadup.flyway.enabled=false",
            "spring.autoconfigure.exclude=org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration"
        })
class S3DfsProviderIT {
    private final DfsService service;

    @Autowired
    S3DfsProviderIT(DfsService service) {
        this.service = service;
    }

    @Test
    void supportsObjectOperationsMetadataAndPresignedUrl() throws Exception {
        byte[] bytes = "s3-contract".getBytes(StandardCharsets.UTF_8);
        FileMetadata metadata = service.upload(new FileUploadRequest(
                "contract.txt",
                new ByteArrayInputStream(bytes),
                bytes.length,
                "text/plain",
                "contracts",
                Map.of("case", "s3")));

        assertTrue(service.exists(metadata.fileId()));
        assertEquals("s3", service.getMetadata(metadata.fileId()).metadata().get("case"));
        assertNotNull(service.generatePresignedDownloadUrl(metadata.fileId(), Duration.ofMinutes(5)));
        try (FileDownloadResponse response = service.download(metadata.fileId())) {
            assertEquals("s3-contract", new String(response.content().readAllBytes(), StandardCharsets.UTF_8));
        }
        assertTrue(service.delete(metadata.fileId()));
    }

    @Test
    void supportsMultipartUpload() throws Exception {
        MultipartUpload upload = service.initiateMultipartUpload(
                new MultipartUploadRequest("multipart.txt", "text/plain", "contracts", Map.of("case", "multipart")));
        byte[] bytes = "multipart-contract".getBytes(StandardCharsets.UTF_8);
        MultipartPart part = service.uploadPart(
                upload.fileId(), upload.uploadId(), 1, new ByteArrayInputStream(bytes), bytes.length);
        FileMetadata metadata = service.completeMultipartUpload(upload.fileId(), upload.uploadId(), List.of(part));

        assertEquals(bytes.length, metadata.size());
        assertEquals("multipart.txt", metadata.filename());
        assertEquals("multipart", metadata.metadata().get("case"));
    }
}
