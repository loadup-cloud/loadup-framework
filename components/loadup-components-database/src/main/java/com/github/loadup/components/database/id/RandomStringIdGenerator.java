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

import com.github.loadup.commons.util.RandomUtil;

/**
 * 随机字符串 ID 生成器
 *
 * <p>生成指定长度的随机字符串作为 ID
 */
public class RandomStringIdGenerator implements IdGenerator {

  private final int length;

  public RandomStringIdGenerator(int length) {
    if (length <= 0) {
      throw new IllegalArgumentException("Length must be positive, got: " + length);
    }
    this.length = length;
  }

  @Override
  public String generate() {
    return RandomUtil.randomString(length);
  }

  @Override
  public String getName() {
    return "random-" + length;
  }
}
