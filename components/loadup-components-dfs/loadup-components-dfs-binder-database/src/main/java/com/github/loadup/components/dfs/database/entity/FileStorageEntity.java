package com.github.loadup.components.dfs.database.entity;

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

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 文件存储实体 - MyBatis-Flex Entity */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileStorageEntity {

  private Long id;

  private String fileId;

  private String filename;

  private byte[] content;

  private Long size;

  private String contentType;

  private String hash;

  private String bizType;

  private String bizId;

  private Boolean publicAccess;

  private LocalDateTime uploadTime;

  private String metadata;
}
