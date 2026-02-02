package io.github.loadup.components.cache.serializer;

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

import com.fasterxml.jackson.core.type.TypeReference;
import io.github.loadup.commons.util.JsonUtil;
import io.github.loadup.components.cache.model.CacheValueWrapper;
import lombok.extern.slf4j.Slf4j;

/**
 * @author lise
 * @version JsonCacheSerializer.java, v 0.1 2026年01月13日 18:01 lise
 */
@Slf4j
public class JsonCacheSerializer implements CacheSerializer {
  @Override
  public byte[] serialize(CacheValueWrapper<?> value) {
    return JsonUtil.toBytes(value);
  }

  @Override
  public <T> CacheValueWrapper<T> deserialize(byte[] bytes, Class<T> typeRef) {
    if (bytes == null || bytes.length == 0) {
      return new CacheValueWrapper<>("NULL", null, null);
    }
    CacheValueWrapper<T> wrapper = JsonUtil.fromBytes(bytes, new TypeReference<>() {});
    return wrapper;
  }
}
