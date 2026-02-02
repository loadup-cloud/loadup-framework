package io.github.loadup.components.dfs.model;

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

import io.github.loadup.components.dfs.enums.FileStatus;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 文件元数据 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileMetadata {

  /** 文件ID */
  private String fileId;

  /** 文件名 */
  private String filename;

  /** 文件大小（字节） */
  private Long size;

  /** 内容类型 */
  private String contentType;

  /** 存储提供者 */
  private String provider;

  /** 存储路径 */
  private String path;

  /** 访问URL */
  private String url;

  /** 文件哈希值（MD5/SHA256） */
  private String hash;

  /** 业务类型 */
  private String bizType;

  /** 业务ID */
  private String bizId;

  /** 文件状态 */
  private FileStatus status;

  /** 是否公开访问 */
  private Boolean publicAccess;

  /** 扩展元数据 */
  private Map<String, String> metadata;

  /** 上传时间 */
  private LocalDateTime uploadTime;

  /** 上传者 */
  private String uploader;

  /** 最后访问时间 */
  private LocalDateTime lastAccessTime;

  /** 访问次数 */
  private Long accessCount;
}
