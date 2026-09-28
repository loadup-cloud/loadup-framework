package io.github.loadup.components.dfs.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.loadup.components.dfs.DfsService;
import io.github.loadup.components.dfs.model.FileDownloadResponse;
import io.github.loadup.components.dfs.model.FileMetadata;
import io.github.loadup.components.dfs.model.FileUploadRequest;
import io.github.loadup.components.testcontainers.annotation.ContainerType;
import io.github.loadup.components.testcontainers.annotation.EnableTestContainers;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = TestApplication.class)
@EnableTestContainers(value = ContainerType.MYSQL, reuse = false)
@ActiveProfiles("test")
@TestPropertySource(properties = "loadup.dfs.binder-type=database")
class DatabaseDfsProviderIT {
    private final DfsService service;

    @Autowired
    DatabaseDfsProviderIT(DfsService service) {
        this.service = service;
    }

    @Test
    void supportsTheCommonObjectContractForSmallFiles() throws Exception {
        byte[] bytes = "database-contract".getBytes(StandardCharsets.UTF_8);
        FileMetadata metadata = service.upload(new FileUploadRequest(
                "contract.txt",
                new ByteArrayInputStream(bytes),
                bytes.length,
                "text/plain",
                null,
                Map.of("case", "database")));

        assertTrue(service.exists(metadata.fileId()));
        assertEquals(
                "database", service.getMetadata(metadata.fileId()).metadata().get("case"));
        try (FileDownloadResponse response = service.download(metadata.fileId())) {
            assertEquals("database-contract", new String(response.content().readAllBytes(), StandardCharsets.UTF_8));
        }
        assertTrue(service.delete(metadata.fileId()));
    }
}
