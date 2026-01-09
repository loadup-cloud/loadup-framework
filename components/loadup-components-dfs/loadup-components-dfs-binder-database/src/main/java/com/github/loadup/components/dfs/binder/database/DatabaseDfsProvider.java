package com.github.loadup.components.dfs.binder.database;

/*-
 * #%L
 * loadup-components-dfs-binder-database
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

import com.github.loadup.components.dfs.api.IDfsProvider;
import com.github.loadup.components.dfs.binder.database.entity.FileStorageEntity;
import com.github.loadup.components.dfs.binder.database.entity.table.Tables;
import com.github.loadup.components.dfs.binder.database.mapper.FileStorageEntityMapper;
import com.github.loadup.components.dfs.config.DfsProperties;
import com.github.loadup.components.dfs.enums.DfsProviderType;
import com.github.loadup.components.dfs.enums.FileStatus;
import com.github.loadup.components.dfs.model.FileDownloadResponse;
import com.github.loadup.components.dfs.model.FileMetadata;
import com.github.loadup.components.dfs.model.FileUploadRequest;
import com.mybatisflex.core.query.QueryWrapper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

/** 数据库存储提供者 */
@Slf4j
//@Component
//@Extension(bizCode = DfsConstant.BIZ_CODE, useCase = DfsConstant.DATABASE)
//@ConditionalOnProperty(prefix = "loadup.dfs", name = "provider", havingValue = DfsConstant.DATABASE)
public class DatabaseDfsProvider implements IDfsProvider {

   private DfsProperties dfsProperties;

   private FileStorageEntityMapper fileStorageEntityMapper;

  @Override
  public FileMetadata upload(FileUploadRequest request) {
    try {
      if (fileStorageEntityMapper == null) {
        throw new IllegalStateException("Database provider is not configured");
      }

      // 读取文件内容
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      byte[] buffer = new byte[8192];
      int bytesRead;
      long size = 0;

      try (InputStream inputStream = request.getInputStream()) {
        while ((bytesRead = inputStream.read(buffer)) != -1) {
          baos.write(buffer, 0, bytesRead);
          size += bytesRead;
        }
      }

      byte[] content = baos.toByteArray();

      // 计算MD5哈希
      MessageDigest md = MessageDigest.getInstance("MD5");
      String hash = bytesToHex(md.digest(content));

      // 生成文件ID
      String fileId = UUID.randomUUID().toString();

      // 保存到数据库
      FileStorageEntity entity = new FileStorageEntity();
      entity.setFileId(fileId);
      entity.setFilename(request.getFilename());
      entity.setContent(content);
      entity.setSize(size);
      entity.setContentType(request.getContentType());
      entity.setHash(hash);
      entity.setBizType(request.getBizType());
      entity.setBizId(request.getBizId());
      entity.setPublicAccess(request.getPublicAccess());
      entity.setUploadTime(LocalDateTime.now());

      fileStorageEntityMapper.insert(entity);

      log.info("File uploaded successfully to database: {}", request.getFilename());

      return FileMetadata.builder()
          .fileId(fileId)
          .filename(request.getFilename())
          .size(size)
          .contentType(request.getContentType())
          .provider(getProviderName())
          .hash(hash)
          .bizType(request.getBizType())
          .bizId(request.getBizId())
          .status(FileStatus.AVAILABLE)
          .publicAccess(request.getPublicAccess())
          .metadata(request.getMetadata())
          .uploadTime(LocalDateTime.now())
          .accessCount(0L)
          .build();

    } catch (Exception e) {
      log.error("Failed to upload file to database: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to upload file", e);
    }
  }

  @Override
  public FileDownloadResponse download(String fileId) {
    try {
      QueryWrapper query =
          QueryWrapper.create().where(Tables.FILE_STORAGE_ENTITY.FILE_ID.eq(fileId));

      FileStorageEntity entity = fileStorageEntityMapper.selectOneByQuery(query);
      if (entity == null) {
        throw new RuntimeException("File not found: " + fileId);
      }

      ByteArrayInputStream inputStream = new ByteArrayInputStream(entity.getContent());

      FileMetadata metadata =
          FileMetadata.builder()
              .fileId(entity.getFileId())
              .filename(entity.getFilename())
              .size(entity.getSize())
              .contentType(entity.getContentType())
              .provider(getProviderName())
              .hash(entity.getHash())
              .bizType(entity.getBizType())
              .bizId(entity.getBizId())
              .status(FileStatus.AVAILABLE)
              .publicAccess(entity.getPublicAccess())
              .uploadTime(entity.getUploadTime())
              .build();

      return FileDownloadResponse.builder()
          .metadata(metadata)
          .inputStream(inputStream)
          .contentLength(entity.getSize())
          .build();

    } catch (Exception e) {
      log.error("Failed to download file from database: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to download file", e);
    }
  }

  @Override
  public boolean delete(String fileId) {
    try {
      QueryWrapper query =
          QueryWrapper.create().where(Tables.FILE_STORAGE_ENTITY.FILE_ID.eq(fileId));

      int deleted = fileStorageEntityMapper.deleteByQuery(query);
      log.info("File deleted successfully from database: {}", fileId);
      return deleted > 0;

    } catch (Exception e) {
      log.error("Failed to delete file from database: {}", e.getMessage(), e);
      return false;
    }
  }

  @Override
  public boolean exists(String fileId) {
    QueryWrapper query = QueryWrapper.create().where(Tables.FILE_STORAGE_ENTITY.FILE_ID.eq(fileId));
    return fileStorageEntityMapper.selectCountByQuery(query) > 0;
  }

  @Override
  public FileMetadata getMetadata(String fileId) {
    QueryWrapper query = QueryWrapper.create().where(Tables.FILE_STORAGE_ENTITY.FILE_ID.eq(fileId));

    FileStorageEntity entity = fileStorageEntityMapper.selectOneByQuery(query);
    if (entity == null) {
      throw new RuntimeException("File not found: " + fileId);
    }

    return FileMetadata.builder()
        .fileId(entity.getFileId())
        .filename(entity.getFilename())
        .size(entity.getSize())
        .contentType(entity.getContentType())
        .provider(getProviderName())
        .hash(entity.getHash())
        .bizType(entity.getBizType())
        .bizId(entity.getBizId())
        .status(FileStatus.AVAILABLE)
        .publicAccess(entity.getPublicAccess())
        .uploadTime(entity.getUploadTime())
        .build();
  }

  @Override
  public String getProviderName() {
    return DfsProviderType.DATABASE.getValue();
  }

  @Override
  public boolean isAvailable() {
    return DfsProviderType.DATABASE.equals(dfsProperties.getProvider())
        && fileStorageEntityMapper != null;
  }

  private static String bytesToHex(byte[] bytes) {
    StringBuilder result = new StringBuilder();
    for (byte b : bytes) {
      result.append(String.format("%02x", b));
    }
    return result.toString();
  }
}
