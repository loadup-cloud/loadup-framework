package com.github.loadup.components.dfs.enums;

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

import lombok.Getter;

/** 文件状态 */
@Getter
public enum FileStatus {
  /** 上传中 */
  UPLOADING("uploading", "上传中"),

  /** 可用 */
  AVAILABLE("available", "可用"),

  /** 已删除 */
  DELETED("deleted", "已删除"),

  /** 归档 */
  ARCHIVED("archived", "已归档"),

  /** 错误 */
  ERROR("error", "错误");

  private final String code;
  private final String description;

  FileStatus(String code, String description) {
    this.code = code;
    this.description = description;
  }

  public static FileStatus fromCode(String code) {
    for (FileStatus status : values()) {
      if (status.code.equals(code)) {
        return status;
      }
    }
    throw new IllegalArgumentException("Unknown file status: " + code);
  }
}
