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

/** 存储提供者类型 */
@Getter
public enum StorageProvider {
  /** 本地文件系统 */
  LOCAL("local", "本地存储"),

  /** 数据库存储 */
  DATABASE("database", "数据库存储"),

  /** S3对象存储 */
  S3("s3", "S3对象存储"),

  /** MinIO对象存储 */
  MINIO("minio", "MinIO对象存储"),

  /** 阿里云OSS */
  ALIYUN_OSS("aliyun-oss", "阿里云OSS"),

  /** 腾讯云COS */
  TENCENT_COS("tencent-cos", "腾讯云COS");

  private final String code;
  private final String description;

  StorageProvider(String code, String description) {
    this.code = code;
    this.description = description;
  }

  public static StorageProvider fromCode(String code) {
    for (StorageProvider provider : values()) {
      if (provider.code.equals(code)) {
        return provider;
      }
    }
    throw new IllegalArgumentException("Unknown storage provider: " + code);
  }
}
