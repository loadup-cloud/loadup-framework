package com.github.loadup.components.dfs.binder.local;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.loadup.components.dfs.api.IDfsProvider;
import com.github.loadup.components.dfs.config.DfsProperties;
import com.github.loadup.components.dfs.enums.FileStatus;
import com.github.loadup.components.dfs.model.FileDownloadResponse;
import com.github.loadup.components.dfs.model.FileMetadata;
import com.github.loadup.components.dfs.model.FileUploadRequest;
import com.github.loadup.components.extension.annotation.Extension;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** 本地文件系统存储提供者 */
@Slf4j
@Component
@Extension(bizCode = "DFS", useCase = "local")
@ConditionalOnProperty(
    prefix = "loadup.dfs",
    name = "default-provider",
    havingValue = "local",
    matchIfMissing = true)
public class LocalDfsProvider implements IDfsProvider {

  @Autowired private DfsProperties dfsProperties;

  private static final DateTimeFormatter PATH_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");
  private static final ObjectMapper objectMapper = new ObjectMapper();

  static {
    objectMapper.findAndRegisterModules(); // 注册Java 8时间模块
  }

  @Override
  public FileMetadata upload(FileUploadRequest request) {
    try {
      DfsProperties.ProviderConfig config = dfsProperties.getProviders().get("local");
      if (config == null || !config.isEnabled()) {
        throw new IllegalStateException("Local provider is not enabled");
      }

      String basePath = config.getBasePath();
      if (basePath == null || basePath.isEmpty()) {
        basePath = System.getProperty("java.io.tmpdir") + "/dfs";
      }

      // 生成文件ID
      String fileId = UUID.randomUUID().toString();

      // 生成存储路径: basePath/bizType/yyyy/MM/dd/fileId
      String relativePath = buildRelativePath(request.getBizType(), fileId);
      Path targetPath = Paths.get(basePath, relativePath);

      // 创建目录
      Files.createDirectories(targetPath.getParent());

      // 保存文件并计算哈希
      String hash;
      long size;
      try (InputStream inputStream = request.getInputStream();
          FileOutputStream outputStream = new FileOutputStream(targetPath.toFile())) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int bytesRead;
        size = 0;

        while ((bytesRead = inputStream.read(buffer)) != -1) {
          outputStream.write(buffer, 0, bytesRead);
          baos.write(buffer, 0, bytesRead);
          size += bytesRead;
        }

        // 计算MD5哈希
        MessageDigest md = MessageDigest.getInstance("MD5");
        hash = bytesToHex(md.digest(baos.toByteArray()));
      }

      log.info("File uploaded successfully: {} -> {}", request.getFilename(), targetPath);

      FileMetadata metadata =
          FileMetadata.builder()
              .fileId(fileId)
              .filename(request.getFilename())
              .size(size)
              .contentType(request.getContentType())
              .provider(getProviderName())
              .path(relativePath)
              .hash(hash)
              .bizType(request.getBizType())
              .bizId(request.getBizId())
              .status(FileStatus.AVAILABLE)
              .publicAccess(request.getPublicAccess())
              .metadata(request.getMetadata())
              .uploadTime(LocalDateTime.now())
              .accessCount(0L)
              .build();

      // 保存元数据到.meta文件
      saveMetadata(basePath, fileId, metadata);

      return metadata;

    } catch (Exception e) {
      log.error("Failed to upload file to local storage: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to upload file", e);
    }
  }

  @Override
  public FileDownloadResponse download(String fileId) {
    try {
      FileMetadata metadata = getMetadata(fileId);
      DfsProperties.ProviderConfig config = dfsProperties.getProviders().get("local");
      String basePath = config.getBasePath();

      Path filePath = Paths.get(basePath, metadata.getPath());

      if (!Files.exists(filePath)) {
        throw new FileNotFoundException("File not found: " + fileId);
      }

      InputStream inputStream = new FileInputStream(filePath.toFile());

      return FileDownloadResponse.builder()
          .metadata(metadata)
          .inputStream(inputStream)
          .contentLength(metadata.getSize())
          .build();

    } catch (Exception e) {
      log.error("Failed to download file from local storage: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to download file", e);
    }
  }

  @Override
  public boolean delete(String fileId) {
    try {
      FileMetadata metadata = getMetadata(fileId);
      DfsProperties.ProviderConfig config = dfsProperties.getProviders().get("local");
      String basePath = config.getBasePath();

      Path filePath = Paths.get(basePath, metadata.getPath());

      if (Files.exists(filePath)) {
        Files.delete(filePath);

        // 删除元数据文件
        Path metaPath = Paths.get(basePath, ".meta", fileId + ".json");
        if (Files.exists(metaPath)) {
          Files.delete(metaPath);
        }

        log.info("File deleted successfully: {}", fileId);
        return true;
      }

      return false;

    } catch (Exception e) {
      log.error("Failed to delete file from local storage: {}", e.getMessage(), e);
      return false;
    }
  }

  @Override
  public boolean exists(String fileId) {
    try {
      FileMetadata metadata = getMetadata(fileId);
      DfsProperties.ProviderConfig config = dfsProperties.getProviders().get("local");
      String basePath = config.getBasePath();

      Path filePath = Paths.get(basePath, metadata.getPath());
      return Files.exists(filePath);

    } catch (Exception e) {
      return false;
    }
  }

  @Override
  public FileMetadata getMetadata(String fileId) {
    try {
      DfsProperties.ProviderConfig config = dfsProperties.getProviders().get("local");
      String basePath = config.getBasePath();
      if (basePath == null || basePath.isEmpty()) {
        basePath = System.getProperty("java.io.tmpdir") + "/dfs";
      }

      // 读取.meta文件
      Path metaPath = Paths.get(basePath, ".meta", fileId + ".json");
      if (!Files.exists(metaPath)) {
        throw new FileNotFoundException("Metadata not found for fileId: " + fileId);
      }

      return objectMapper.readValue(metaPath.toFile(), FileMetadata.class);

    } catch (Exception e) {
      log.error("Failed to get metadata for fileId: {}", fileId, e);
      throw new RuntimeException("Failed to get metadata", e);
    }
  }

  @Override
  public String getProviderName() {
    return "local";
  }

  @Override
  public boolean isAvailable() {
    DfsProperties.ProviderConfig config = dfsProperties.getProviders().get("local");
    return config != null && config.isEnabled();
  }

  private String buildRelativePath(String bizType, String fileId) {
    String datePath = LocalDateTime.now().format(PATH_FORMATTER);
    if (bizType != null && !bizType.isEmpty()) {
      return bizType + "/" + datePath + "/" + fileId;
    }
    return datePath + "/" + fileId;
  }

  private static String bytesToHex(byte[] bytes) {
    StringBuilder result = new StringBuilder();
    for (byte b : bytes) {
      result.append(String.format("%02x", b));
    }
    return result.toString();
  }

  private void saveMetadata(String basePath, String fileId, FileMetadata metadata)
      throws IOException {
    // 在basePath/.meta目录下保存元数据为JSON文件
    Path metaDir = Paths.get(basePath, ".meta");
    Files.createDirectories(metaDir);

    Path metaPath = metaDir.resolve(fileId + ".json");
    objectMapper.writeValue(metaPath.toFile(), metadata);
  }
}
