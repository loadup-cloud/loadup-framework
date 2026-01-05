package com.github.loadup.components.dfs.binder.database.entity;

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

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 文件存储实体 - MyBatis-Flex Entity */
@Data
@Table("dfs_file_storage")
@NoArgsConstructor
@AllArgsConstructor
public class FileStorageEntity {

  @Id(keyType = KeyType.Auto)
  private Long id;

  @Column("file_id")
  private String fileId;

  @Column("filename")
  private String filename;

  @Column("content")
  private byte[] content;

  @Column("size")
  private Long size;

  @Column("content_type")
  private String contentType;

  @Column("hash")
  private String hash;

  @Column("biz_type")
  private String bizType;

  @Column("biz_id")
  private String bizId;

  @Column("public_access")
  private Boolean publicAccess;

  @Column("upload_time")
  private LocalDateTime uploadTime;
}
