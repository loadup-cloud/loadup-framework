package io.github.loadup.components.dfs.local;

/*-
 * #%L
 * Loadup Dfs Binder Local
 * %%
 * Copyright (C) 2025 - 2026 loadup_cloud
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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.github.loadup.components.dfs.DfsProvider;
import io.github.loadup.components.dfs.model.FileDownloadResponse;
import io.github.loadup.components.dfs.model.FileMetadata;
import io.github.loadup.components.dfs.model.FileUploadRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalDfsProvider implements DfsProvider {
    private static final Logger log = LoggerFactory.getLogger(LocalDfsProvider.class);

    private final Path storagePath;
    private final Map<String, FileMetadata> metadataIndex = new ConcurrentHashMap<>();

    @SuppressFBWarnings(
            value = "CT_CONSTRUCTOR_THROW",
            justification = "Fail-fast construction: throwing when the configured storage directory cannot be created"
                    + " is intentional and the class has no finalizer.")
    public LocalDfsProvider(LocalDfsConfig config) {
        this.storagePath = Path.of(config.getUploadDir());
        if (!this.storagePath.toFile().mkdirs() && !this.storagePath.toFile().isDirectory()) {
            throw new IllegalStateException("Failed to create storage directory: " + storagePath);
        }
    }

    @Override
    public FileMetadata upload(FileUploadRequest request) {
        String fileId = UUID.randomUUID().toString().replace("-", "");
        Path filenamePath = Path.of(request.getFilename());
        Path fileNamePath = filenamePath.getFileName();
        String safeName = fileNamePath != null ? fileNamePath.toString() : filenamePath.toString();
        File target = resolveFile(fileId + "_" + safeName);
        InputStream inputStream = request.getInputStream();
        if (inputStream == null) {
            throw new IllegalArgumentException("Upload input stream is required");
        }
        try (FileOutputStream fos = new FileOutputStream(target)) {
            inputStream.transferTo(fos);
        } catch (IOException e) {
            throw new RuntimeException("Upload failed", e);
        }

        FileMetadata fileMetadata = new FileMetadata();
        fileMetadata.setFileId(fileId);
        fileMetadata.setFilename(request.getFilename());
        fileMetadata.setPath(fileId + "_" + safeName);
        fileMetadata.setSize(target.length());
        fileMetadata.setContentType(request.getContentType());

        metadataIndex.put(fileId, fileMetadata);
        return fileMetadata;
    }

    @Override
    @SuppressFBWarnings(
            value = "OBL_UNSATISFIED_OBLIGATION",
            justification = "InputStream ownership transfers to the caller through FileDownloadResponse; the caller"
                    + " is responsible for closing it.")
    public FileDownloadResponse download(String fileId) {
        FileMetadata meta = metadataIndex.get(fileId);
        if (meta == null) return null;
        File file = resolveFile(meta.getPath());
        try {
            FileInputStream inputStream = new FileInputStream(file);
            return FileDownloadResponse.builder()
                    .inputStream(inputStream)
                    .metadata(meta)
                    .build();
        } catch (IOException e) {
            throw new RuntimeException("Download failed", e);
        }
    }

    @Override
    public boolean delete(String fileId) {
        FileMetadata meta = metadataIndex.remove(fileId);
        if (meta == null) return false;
        boolean deleted = resolveFile(meta.getPath()).delete();
        if (!deleted) {
            log.warn("Physical file delete failed for fileId={}, path={}", fileId, meta.getPath());
        }
        return meta != null;
    }

    private File resolveFile(String relativePath) {
        Path resolved = storagePath.resolve(relativePath).normalize();
        if (!resolved.startsWith(storagePath)) {
            throw new IllegalArgumentException("Invalid storage path: " + relativePath);
        }
        return resolved.toFile();
    }

    @Override
    public boolean exists(String fileId) {
        return metadataIndex.containsKey(fileId);
    }

    @Override
    public FileMetadata getMetadata(String fileId) {
        return metadataIndex.get(fileId);
    }

    @Override
    public String getBinderType() {
        return "local";
    }
}
