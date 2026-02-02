package io.github.loadup.components.cache.test.common;

/*-
 * #%L
 * Loadup Cache Test
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
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

import io.github.loadup.components.cache.binder.CacheTicker;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author lise
 * @version FakeTicker.java, v 0.1 2026年01月14日 11:24 lise
 */
public class FakeTicker implements CacheTicker {
  private final AtomicLong nanos = new AtomicLong();

  public void advance(long duration, TimeUnit unit) {
    nanos.addAndGet(unit.toNanos(duration));
  }

  @Override
  public long read() {
    return nanos.get();
  }
}
