package io.github.loadup.components.dfs.local;

/*-
 * #%L
 * Loadup Dfs Binder Local
 * %%
 * Copyright (C) 2025 - 2026 loadup_cloud
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
        FileMetadata meta = FileMetadata.builder()
                .fileId(fileId).filename(request.getFilename())
                .size(target.length()).contentType(request.getContentType())
                .path(target.getAbsolutePath()).build();
        metadataIndex.put(fileId, meta);
        return meta;
    }

    @Override
    public FileDownloadResponse download(String fileId) {
        FileMetadata meta = metadataIndex.get(fileId);
        if (meta == null) return null;
        try {
            File file = new File(meta.getPath());
            return FileDownloadResponse.builder()
                    .inputStream(new FileInputStream(file))
                    .metadata(meta).build();
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

    @Override public String getBinderType() { return "local"; }
}
