package com.github.loadup.components.dfs.service;

/*-
 * #%L
 * loadup-components-dfs-api
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
import com.github.loadup.components.dfs.config.DfsProperties;
import com.github.loadup.components.dfs.model.FileDownloadResponse;
import com.github.loadup.components.dfs.model.FileMetadata;
import com.github.loadup.components.dfs.model.FileUploadRequest;
import com.github.loadup.components.extension.core.BizScenario;
import com.github.loadup.components.extension.exector.ExtensionExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** DFS服务 */
@Slf4j
@Service
public class DfsService {

  private final ExtensionExecutor extensionExecutor;
  private final DfsProperties dfsProperties;

  public DfsService(ExtensionExecutor extensionExecutor, DfsProperties dfsProperties) {
    this.extensionExecutor = extensionExecutor;
    this.dfsProperties = dfsProperties;
  }

  /**
   * 上传文件（使用默认提供者）
   *
   * @param request 上传请求
   * @return 文件元数据
   */
  public FileMetadata upload(FileUploadRequest request) {
    return upload(request, dfsProperties.getDefaultProvider());
  }

  /**
   * 上传文件（指定提供者）
   *
   * @param request 上传请求
   * @param providerName 提供者名称
   * @return 文件元数据
   */
  public FileMetadata upload(FileUploadRequest request, String providerName) {
    log.info("Uploading file: {} using provider: {}", request.getFilename(), providerName);
    BizScenario scenario = BizScenario.valueOf("DFS", providerName, "default");
    return extensionExecutor.execute(
        IDfsProvider.class, scenario, extension -> extension.upload(request));
  }

  /**
   * 下载文件
   *
   * @param fileId 文件ID
   * @return 文件下载响应
   */
  public FileDownloadResponse download(String fileId) {
    return download(fileId, dfsProperties.getDefaultProvider());
  }

  /**
   * 下载文件（指定提供者）
   *
   * @param fileId 文件ID
   * @param providerName 提供者名称
   * @return 文件下载响应
   */
  public FileDownloadResponse download(String fileId, String providerName) {
    log.info("Downloading file: {} using provider: {}", fileId, providerName);
    BizScenario scenario = BizScenario.valueOf("DFS", providerName, "default");
    return extensionExecutor.execute(
        IDfsProvider.class, scenario, extension -> extension.download(fileId));
  }

  /**
   * 删除文件
   *
   * @param fileId 文件ID
   * @return 是否删除成功
   */
  public boolean delete(String fileId) {
    return delete(fileId, dfsProperties.getDefaultProvider());
  }

  /**
   * 删除文件（指定提供者）
   *
   * @param fileId 文件ID
   * @param providerName 提供者名称
   * @return 是否删除成功
   */
  public boolean delete(String fileId, String providerName) {
    log.info("Deleting file: {} using provider: {}", fileId, providerName);
    BizScenario scenario = BizScenario.valueOf("DFS", providerName, "default");
    return extensionExecutor.execute(
        IDfsProvider.class, scenario, extension -> extension.delete(fileId));
  }

  /**
   * 检查文件是否存在
   *
   * @param fileId 文件ID
   * @return 是否存在
   */
  public boolean exists(String fileId) {
    return exists(fileId, dfsProperties.getDefaultProvider());
  }

  /**
   * 检查文件是否存在（指定提供者）
   *
   * @param fileId 文件ID
   * @param providerName 提供者名称
   * @return 是否存在
   */
  public boolean exists(String fileId, String providerName) {
    BizScenario scenario = BizScenario.valueOf("DFS", providerName, "default");
    return extensionExecutor.execute(
        IDfsProvider.class, scenario, extension -> extension.exists(fileId));
  }

  /**
   * 获取文件元数据
   *
   * @param fileId 文件ID
   * @return 文件元数据
   */
  public FileMetadata getMetadata(String fileId) {
    return getMetadata(fileId, dfsProperties.getDefaultProvider());
  }

  /**
   * 获取文件元数据（指定提供者）
   *
   * @param fileId 文件ID
   * @param providerName 提供者名称
   * @return 文件元数据
   */
  public FileMetadata getMetadata(String fileId, String providerName) {
    BizScenario scenario = BizScenario.valueOf("DFS", providerName, "default");
    return extensionExecutor.execute(
        IDfsProvider.class, scenario, extension -> extension.getMetadata(fileId));
  }

  /**
   * 生成预签名URL
   *
   * @param fileId 文件ID
   * @param expirationSeconds 过期时间（秒）
   * @return 预签名URL
   */
  public String generatePresignedUrl(String fileId, long expirationSeconds) {
    return generatePresignedUrl(fileId, expirationSeconds, dfsProperties.getDefaultProvider());
  }

  /**
   * 生成预签名URL（指定提供者）
   *
   * @param fileId 文件ID
   * @param expirationSeconds 过期时间（秒）
   * @param providerName 提供者名称
   * @return 预签名URL
   */
  public String generatePresignedUrl(String fileId, long expirationSeconds, String providerName) {
    BizScenario scenario = BizScenario.valueOf("DFS", providerName, "default");
    return extensionExecutor.execute(
        IDfsProvider.class,
        scenario,
        extension -> extension.generatePresignedUrl(fileId, expirationSeconds));
  }
}
