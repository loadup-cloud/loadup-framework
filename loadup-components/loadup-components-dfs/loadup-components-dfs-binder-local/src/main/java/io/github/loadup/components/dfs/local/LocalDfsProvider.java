/*-
 * #%L
 * Loadup Dfs Binder Local
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
package io.github.loadup.components.dfs.local;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.github.loadup.components.dfs.DfsObjectNotFoundException;
import io.github.loadup.components.dfs.DfsProvider;
import io.github.loadup.components.dfs.DfsStorageException;
import io.github.loadup.components.dfs.model.FileDownloadResponse;
import io.github.loadup.components.dfs.model.FileMetadata;
import io.github.loadup.components.dfs.model.FileUploadRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

/** Local filesystem binder intended for development and single-node deployments. */
public class LocalDfsProvider implements DfsProvider {
    private static final String METADATA_PREFIX = "metadata.";

    private final Path objectRoot;
    private final Path metadataRoot;

    @SuppressFBWarnings(
            value = "CT_CONSTRUCTOR_THROW",
            justification = "Invalid storage paths must fail fast before the provider is published as a bean.")
    public LocalDfsProvider(LocalDfsProperties properties) {
        Path basePath = Path.of(properties.getBasePath()).toAbsolutePath().normalize();
        this.objectRoot = basePath.resolve("objects");
        this.metadataRoot = basePath.resolve("metadata");
        try {
            Files.createDirectories(objectRoot);
            Files.createDirectories(metadataRoot);
        } catch (IOException e) {
            throw new DfsStorageException("Failed to initialize local DFS at " + basePath, e);
        }
    }

    @Override
    public FileMetadata upload(FileUploadRequest request) {
        String fileId = createFileId(request.path());
        Path target = resolve(objectRoot, fileId);
        Path temporary = target.resolveSibling(target.getFileName() + ".uploading");
        Instant uploadedAt = Instant.now();
        try {
            Files.createDirectories(target.getParent());
            Files.copy(request.content(), temporary, StandardCopyOption.REPLACE_EXISTING);
            long actualSize = Files.size(temporary);
            if (actualSize != request.contentLength()) {
                throw new DfsStorageException("Uploaded content length mismatch: expected=" + request.contentLength()
                        + ", actual=" + actualSize);
            }
            try {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
            FileMetadata metadata = new FileMetadata(
                    fileId,
                    request.filename(),
                    actualSize,
                    request.contentType(),
                    getBinderType(),
                    fileId,
                    request.metadata(),
                    uploadedAt);
            writeMetadata(metadata);
            return metadata;
        } catch (IOException | RuntimeException e) {
            deleteQuietly(temporary);
            deleteQuietly(target);
            if (e instanceof DfsStorageException storageException) {
                throw storageException;
            }
            throw new DfsStorageException("Local DFS upload failed for " + fileId, e);
        }
    }

    @Override
    @SuppressFBWarnings(
            value = "OBL_UNSATISFIED_OBLIGATION",
            justification = "The response transfers stream ownership to the DfsService caller.")
    public FileDownloadResponse download(String fileId) {
        FileMetadata metadata = getMetadata(fileId);
        Path object = resolve(objectRoot, fileId);
        try {
            InputStream content = Files.newInputStream(object);
            return new FileDownloadResponse(metadata, content, Files.size(object));
        } catch (IOException e) {
            throw new DfsStorageException("Local DFS download failed for " + fileId, e);
        }
    }

    @Override
    public boolean delete(String fileId) {
        Path object = resolve(objectRoot, fileId);
        Path metadata = metadataPath(fileId);
        try {
            boolean existed = Files.deleteIfExists(object);
            Files.deleteIfExists(metadata);
            return existed;
        } catch (IOException e) {
            throw new DfsStorageException("Local DFS delete failed for " + fileId, e);
        }
    }

    @Override
    public boolean exists(String fileId) {
        return Files.isRegularFile(resolve(objectRoot, fileId));
    }

    @Override
    public FileMetadata getMetadata(String fileId) {
        Path metadataFile = metadataPath(fileId);
        if (!Files.isRegularFile(metadataFile)) {
            throw new DfsObjectNotFoundException(fileId);
        }
        Properties values = new Properties();
        try (InputStream input = Files.newInputStream(metadataFile)) {
            values.load(input);
        } catch (IOException e) {
            throw new DfsStorageException("Failed to read local DFS metadata for " + fileId, e);
        }
        Map<String, String> customMetadata = new HashMap<>();
        values.stringPropertyNames().stream()
                .filter(name -> name.startsWith(METADATA_PREFIX))
                .forEach(
                        name -> customMetadata.put(name.substring(METADATA_PREFIX.length()), values.getProperty(name)));
        return new FileMetadata(
                fileId,
                values.getProperty("filename"),
                Long.parseLong(values.getProperty("size")),
                values.getProperty("contentType"),
                getBinderType(),
                fileId,
                customMetadata,
                Instant.parse(values.getProperty("uploadedAt")));
    }

    @Override
    public String getBinderType() {
        return "local";
    }

    private void writeMetadata(FileMetadata metadata) throws IOException {
        Properties values = new Properties();
        values.setProperty("filename", metadata.filename());
        values.setProperty("size", Long.toString(metadata.size()));
        values.setProperty("contentType", metadata.contentType());
        values.setProperty("uploadedAt", metadata.uploadedAt().toString());
        metadata.metadata().forEach((key, value) -> values.setProperty(METADATA_PREFIX + key, value));
        Path metadataFile = metadataPath(metadata.fileId());
        Files.createDirectories(metadataFile.getParent());
        try (OutputStream output = Files.newOutputStream(metadataFile)) {
            values.store(output, null);
        }
    }

    private String createFileId(String requestedPath) {
        String id = UUID.randomUUID().toString().replace("-", "");
        if (requestedPath == null || requestedPath.isBlank()) {
            return id;
        }
        Path prefix = Path.of(requestedPath).normalize();
        if (prefix.isAbsolute() || prefix.startsWith("..")) {
            throw new IllegalArgumentException("Invalid DFS path: " + requestedPath);
        }
        String normalized = prefix.toString().replace('\\', '/');
        return normalized.isBlank() ? id : normalized + "/" + id;
    }

    private Path metadataPath(String fileId) {
        return resolve(metadataRoot, fileId + ".properties");
    }

    private static Path resolve(Path root, String relativePath) {
        Path resolved = root.resolve(relativePath).normalize();
        if (!resolved.startsWith(root)) {
            throw new IllegalArgumentException("Invalid DFS file identifier: " + relativePath);
        }
        return resolved;
    }

    private static void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
            // Preserve the original storage failure.
        }
    }
}
