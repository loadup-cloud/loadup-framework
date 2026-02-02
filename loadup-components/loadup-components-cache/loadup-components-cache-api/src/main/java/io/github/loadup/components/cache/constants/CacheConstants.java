package io.github.loadup.components.cache.constants;

/*-
 * #%L
 * Loadup Cache Components API
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

/**
 * @author lise
 * @version CacheConstants.java, v 0.1 2026年01月13日 18:46 lise
 */
public interface CacheConstants {
  String SERIALIZER_JSON = "defaultJsonCacheSerializer";
  String SERIALIZER_KRYO = "customKryoSerializer";

  String DEFAULT_TICKER = "defaultCacheTicker";
}
