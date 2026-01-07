package com.github.loadup.components.cache.api;

/*-
 * #%L
 * loadup-components-cache-api
 * %%
 * Copyright (C) 2022 - 2023 loadup_cloud
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

import com.github.loadup.framework.api.binding.Binding;

public interface CacheBinding extends Binding {
  boolean set(String cacheName, String key, Object value);

  Object get(String cacheName, String key);

  <T> T get(String cacheName, String key, Class<T> cls);

  boolean delete(String cacheName, String key);

  boolean deleteAll(String cacheName);
}
