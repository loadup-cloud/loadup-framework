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

import io.github.loadup.components.dfs.DfsProvider;
import io.github.loadup.components.dfs.model.FileDownloadResponse;
import io.github.loadup.components.dfs.model.FileMetadata;
import io.github.loadup.components.dfs.model.FileUploadRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LocalDfsProvider implements DfsProvider {
    private final Path storagePath;
    private final Map<String, FileMetadata> metadataIndex = new ConcurrentHashMap<>();

    public LocalDfsProvider(LocalDfsConfig config) {
        this.storagePath = Path.of(config.getUploadDir());
        this.storagePath.toFile().mkdirs();
    }

    @Override
    public FileMetadata upload(FileUploadRequest request) {
        String fileId = UUID.randomUUID().toString().replace("-", "");
        File target = storagePath.resolve(fileId + "_" + request.getFilename()).toFile();
        try (FileOutputStream fos = new FileOutputStream(target)) {
            request.getInputStream().transferTo(fos);
        } catch (IOException e) {
            throw new RuntimeException("Upload failed", e);
        }

        FileMetadata fileMetadata = new FileMetadata();
        fileMetadata.setFileId(fileId);
        fileMetadata.setFilename(request.getFilename());
        fileMetadata.setSize(target.length());
        fileMetadata.setContentType(request.getContentType());

        metadataIndex.put(fileId, fileMetadata);
        return fileMetadata;
    }

    @Override
    public FileDownloadResponse download(String fileId) {
        FileMetadata meta = metadataIndex.get(fileId);
        if (meta == null) return null;
        try {
            File file = new File(meta.getPath());
            return FileDownloadResponse.builder()
                    .inputStream(new FileInputStream(file))
                    .metadata(meta)
                    .build();
        } catch (IOException e) {
            throw new RuntimeException("Download failed", e);
        }
    }

    @Override
    public boolean delete(String fileId) {
        FileMetadata meta = metadataIndex.remove(fileId);
        if (meta != null) new File(meta.getPath()).delete();
        return meta != null;
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
