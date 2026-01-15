package com.github.loadup.components.dfs.database.binder;

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

import com.github.loadup.commons.util.JsonUtil;
import com.github.loadup.components.dfs.binder.AbstractDfsBinder;
import com.github.loadup.components.dfs.binder.DfsBinder;
import com.github.loadup.components.dfs.cfg.DfsBindingCfg;
import com.github.loadup.components.dfs.database.cfg.DatabaseDfsBinderCfg;
import com.github.loadup.components.dfs.database.entity.FileStorageEntity;
import com.github.loadup.components.dfs.enums.FileStatus;
import com.github.loadup.components.dfs.model.FileDownloadResponse;
import com.github.loadup.components.dfs.model.FileMetadata;
import com.github.loadup.components.dfs.model.FileUploadRequest;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

/** 数据库存储提供者 */
@Slf4j
public class DatabaseDfsBinder extends AbstractDfsBinder<DatabaseDfsBinderCfg, DfsBindingCfg>
    implements DfsBinder<DatabaseDfsBinderCfg, DfsBindingCfg> {

  @Autowired private JdbcTemplate jdbcTemplate;
  private String TABLE_NAME = "file_storage";

  private final Map<String, String> sqlMap = new HashMap<>();
  private final String INSERT_SQL =
      "INSERT INTO %s (file_id, filename, content, size, content_type, hash,biz_type, biz_id, public_access, upload_time, metadata) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
  private final String QUERY_SQL = "SELECT * FROM %s WHERE file_id = ?";
  private final String DELETE_SQL = "DELETE FROM %s WHERE file_id = ?";
  private final String COUNT_SQL = "SELECT COUNT(*) FROM %s WHERE file_id = ?";

  @Override
  public String getBinderType() {
    return "database";
  }

  @Override
  protected void afterConfigInjected(
      String name, DatabaseDfsBinderCfg binderCfg, DfsBindingCfg bindingCfg) {
    String tableName = this.binderCfg.getTableName();
    if (StringUtils.isNotBlank(tableName)) {
      TABLE_NAME = tableName;
    }
    sqlMap.put("INSERT", INSERT_SQL.formatted(TABLE_NAME));
    sqlMap.put("QUERY", QUERY_SQL.formatted(TABLE_NAME));
    sqlMap.put("DELETE", DELETE_SQL.formatted(TABLE_NAME));
    sqlMap.put("COUNT", COUNT_SQL.formatted(TABLE_NAME));
  }

  @Override
  public FileMetadata upload(FileUploadRequest request) {
    try {
      if (jdbcTemplate == null) {
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
      int rowsAffected =
          jdbcTemplate.update(
              sqlMap.get("INSERT"),
              fileId,
              request.getFilename(),
              content,
              size,
              request.getContentType(),
              hash,
              request.getBizType(),
              request.getBizId(),
              request.getPublicAccess(),
              LocalDateTime.now(),
              JsonUtil.toJson(request.getMetadata()));
      if (rowsAffected <= 0) {
        throw new RuntimeException("Failed to insert file record");
      }
      log.info("File uploaded successfully to database: {}", request.getFilename());

      return FileMetadata.builder()
          .fileId(fileId)
          .filename(request.getFilename())
          .size(size)
          .contentType(request.getContentType())
          .provider(getBinderType())
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
      FileStorageEntity entity = getFileStorageEntity(fileId);
      ByteArrayInputStream inputStream = new ByteArrayInputStream(entity.getContent());

      FileMetadata metadata =
          FileMetadata.builder()
              .fileId(entity.getFileId())
              .filename(entity.getFilename())
              .size(entity.getSize())
              .contentType(entity.getContentType())
              .provider(getBinderType())
              .hash(entity.getHash())
              .bizType(entity.getBizType())
              .bizId(entity.getBizId())
              .status(FileStatus.AVAILABLE)
              .publicAccess(entity.getPublicAccess())
              .uploadTime(entity.getUploadTime())
              .metadata(JsonUtil.toStringMap(entity.getMetadata()))
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

      int deleted = jdbcTemplate.update(sqlMap.get("DELETE"), fileId);
      log.info("File deleted successfully from database: {}", fileId);
      return deleted > 0;

    } catch (Exception e) {
      log.error("Failed to delete file from database: {}", e.getMessage(), e);
      return false;
    }
  }

  @Override
  public boolean exists(String fileId) {
    Integer count = jdbcTemplate.queryForObject(sqlMap.get("COUNT"), Integer.class, fileId);
    return count != null && count > 0;
  }

  @Override
  public FileMetadata getMetadata(String fileId) {
    FileStorageEntity entity = getFileStorageEntity(fileId);

    return FileMetadata.builder()
        .fileId(entity.getFileId())
        .filename(entity.getFilename())
        .size(entity.getSize())
        .contentType(entity.getContentType())
        .provider(getBinderType())
        .hash(entity.getHash())
        .bizType(entity.getBizType())
        .bizId(entity.getBizId())
        .status(FileStatus.AVAILABLE)
        .publicAccess(entity.getPublicAccess())
        .uploadTime(entity.getUploadTime())
        .metadata(JsonUtil.toStringMap(entity.getMetadata()))
        .build();
  }

  private FileStorageEntity getFileStorageEntity(String fileId) {
    FileStorageEntity entity;
    try {
      entity =
          jdbcTemplate.queryForObject(
              sqlMap.get("QUERY"), new BeanPropertyRowMapper<>(FileStorageEntity.class), fileId);
    } catch (EmptyResultDataAccessException e) {
      throw new RuntimeException("File not found: " + fileId);
    }

    if (entity == null) {
      throw new RuntimeException("File not found: " + fileId);
    }
    return entity;
  }

  @Override
  public String generatePresignedUrl(String fileId, long expirationSeconds) {
    return super.generatePresignedUrl(fileId, expirationSeconds);
  }

  @Override
  public FileMetadata copy(String sourceFileId, String targetPath) {
    try {
      // 1. 获取源文件信息

      FileStorageEntity sourceEntity = getFileStorageEntity(sourceFileId);

      // 2. 生成新的文件ID和文件名
      String newFileId = UUID.randomUUID().toString();
      String newFilename = "copy of " + sourceEntity.getFilename();

      // 3. 复制记录到数据库
      LocalDateTime now = LocalDateTime.now();

      int rowsAffected =
          jdbcTemplate.update(
              sqlMap.get("INSERT"),
              newFileId,
              newFilename,
              sourceEntity.getContent(),
              sourceEntity.getSize(),
              sourceEntity.getContentType(),
              sourceEntity.getHash(),
              sourceEntity.getBizType(),
              sourceEntity.getBizId(),
              sourceEntity.getPublicAccess(),
              now,
              sourceEntity.getMetadata());

      if (rowsAffected == 0) {
        throw new RuntimeException("Failed to copy file record");
      }

      log.info("File copied successfully: {} -> {}", sourceFileId, newFileId);

      // 4. 构建并返回新文件的元数据
      return FileMetadata.builder()
          .fileId(newFileId)
          .filename(newFilename)
          .size(sourceEntity.getSize())
          .contentType(sourceEntity.getContentType())
          .provider(getBinderType())
          .hash(sourceEntity.getHash())
          .bizType(sourceEntity.getBizType())
          .bizId(sourceEntity.getBizId())
          .status(FileStatus.AVAILABLE)
          .publicAccess(sourceEntity.getPublicAccess())
          .metadata(JsonUtil.toStringMap(sourceEntity.getMetadata()))
          .uploadTime(now)
          .accessCount(0L)
          .build();

    } catch (Exception e) {
      log.error("Failed to copy file: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to copy file", e);
    }
  }

  private static String bytesToHex(byte[] bytes) {
    StringBuilder result = new StringBuilder();
    for (byte b : bytes) {
      result.append(String.format("%02x", b));
    }
    return result.toString();
  }

  public Map<String, String> getSqlMap() {
    return sqlMap;
  }
}
