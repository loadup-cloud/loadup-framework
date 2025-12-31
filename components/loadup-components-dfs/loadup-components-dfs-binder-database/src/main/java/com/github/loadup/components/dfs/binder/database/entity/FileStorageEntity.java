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

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 文件存储实体 - JPA Entity */
@Data
@Entity
@Table(name = "dfs_file_storage")
@NoArgsConstructor
@AllArgsConstructor
public class FileStorageEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "file_id", unique = true, nullable = false, length = 64)
  private String fileId;

  @Column(name = "filename", nullable = false, length = 255)
  private String filename;

  @Lob
  @Column(name = "content", nullable = false)
  private byte[] content;

  @Column(name = "size", nullable = false)
  private Long size;

  @Column(name = "content_type", length = 100)
  private String contentType;

  @Column(name = "hash", length = 64)
  private String hash;

  @Column(name = "biz_type", length = 50)
  private String bizType;

  @Column(name = "biz_id", length = 64)
  private String bizId;

  @Column(name = "public_access")
  private Boolean publicAccess;

  @Column(name = "upload_time")
  private LocalDateTime uploadTime;
}
