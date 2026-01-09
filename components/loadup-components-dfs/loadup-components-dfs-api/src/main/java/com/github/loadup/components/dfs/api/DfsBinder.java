package com.github.loadup.components.dfs.api;

/*-
 * #%L
 * loadup-components-cache-api
 * %%
 * Copyright (C) 2022 - 2023 loadup_cloud
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

import com.github.loadup.components.dfs.model.*;
import com.github.loadup.framework.api.binder.Binder;

public interface DfsBinder extends Binder {

  /**
   * 上传文件
   *
   * @param request 上传请求
   * @return 文件元数据
   */
  FileMetadata upload(FileUploadRequest request);

  /**
   * 下载文件
   *
   * @param fileId 文件ID
   * @return 文件下载响应
   */
  FileDownloadResponse download(String fileId);

  /**
   * 删除文件
   *
   * @param fileId 文件ID
   * @return 是否删除成功
   */
  boolean delete(String fileId);

  /**
   * 检查文件是否存在
   *
   * @param fileId 文件ID
   * @return 是否存在
   */
  boolean exists(String fileId);

  /**
   * 获取文件元数据
   *
   * @param fileId 文件ID
   * @return 文件元数据
   */
  FileMetadata getMetadata(String fileId);

  /**
   * 生成预签名URL（用于临时访问）
   *
   * @param fileId 文件ID
   * @param expirationSeconds 过期时间（秒）
   * @return 预签名URL
   */
  default String generatePresignedUrl(String fileId, long expirationSeconds) {
    throw new UnsupportedOperationException("This provider does not support presigned URLs");
  }

  /**
   * 复制文件
   *
   * @param sourceFileId 源文件ID
   * @param targetPath 目标路径
   * @return 新文件元数据
   */
  default FileMetadata copy(String sourceFileId, String targetPath) {
    throw new UnsupportedOperationException("This provider does not support file copy");
  }

  /**
   * 获取提供者名称
   *
   * @return 提供者名称
   */
  String getProviderName();

  /**
   * 检查提供者是否可用
   *
   * @return 是否可用
   */
  boolean isAvailable();
}
