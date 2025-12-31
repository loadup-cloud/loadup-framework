package com.github.loadup.components.database.id;

/*-
 * #%L
 * loadup-components-database
 * %%
 * Copyright (C) 2022 - 2025 loadup_cloud
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

import java.util.UUID;

/**
 * UUID v4 ID 生成器
 *
 * <p>使用标准的 UUID v4（随机 UUID）生成 ID
 *
 * <p>生成的 ID 格式：xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx
 */
public class UuidV4IdGenerator implements IdGenerator {

  private final boolean withHyphens;

  /**
   * 创建 UUID v4 生成器
   *
   * @param withHyphens 是否保留连字符
   */
  public UuidV4IdGenerator(boolean withHyphens) {
    this.withHyphens = withHyphens;
  }

  /** 创建 UUID v4 生成器（默认不保留连字符） */
  public UuidV4IdGenerator() {
    this(false);
  }

  @Override
  public String generate() {
    String uuid = UUID.randomUUID().toString();
    return withHyphens ? uuid : uuid.replace("-", "");
  }

  @Override
  public String getName() {
    return withHyphens ? "uuid-v4" : "uuid-v4-no-hyphens";
  }
}
