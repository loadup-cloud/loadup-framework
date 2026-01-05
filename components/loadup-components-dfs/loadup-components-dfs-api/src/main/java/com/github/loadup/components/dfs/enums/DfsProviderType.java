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

/**
 * DFS 存储提供者类型枚举
 *
 * <p>定义所有支持的存储类型，用于配置自动提示
 */
public enum DfsProviderType {

  /** 本地文件系统存储 */
  LOCAL("local"),

  /** 数据库存储 */
  DATABASE("database"),

  /** S3 对象存储（兼容 AWS S3、MinIO、阿里云 OSS 等） */
  S3("s3");

  private final String value;

  DfsProviderType(String value) {
    this.value = value;
  }

  /**
   * 获取枚举值
   *
   * @return 枚举值字符串
   */
  public String getValue() {
    return value;
  }

  /**
   * 从字符串值获取枚举
   *
   * @param value 字符串值
   * @return 对应的枚举，如果不存在返回 null
   */
  public static DfsProviderType fromValue(String value) {
    if (value == null || value.isEmpty()) {
      return null;
    }

    for (DfsProviderType type : values()) {
      if (type.value.equalsIgnoreCase(value)) {
        return type;
      }
    }
    return null;
  }

  @Override
  public String toString() {
    return value;
  }
}
