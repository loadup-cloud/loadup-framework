package com.github.loadup.components.database.sequence;

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

import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import lombok.Getter;

@Getter
public class SequenceRange {
  private final Long min;
  private final Long max;

  private final AtomicLong value;

  private boolean over = false;

  public SequenceRange(Long min, Long max) {
    this.min = min;
    this.max = max;
    this.value = new AtomicLong(min);
  }

  public long getAndIncrement() {
    long currentValue = value.getAndIncrement();
    if (currentValue > max) {
      over = true;
      return -1;
    }
    return currentValue;
  }

  public AtomicLong getValue() {
    return value;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
  }
}
