package io.github.loadup.components.cache.binder;

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

import io.github.loadup.components.cache.cfg.CacheBinderCfg;
import io.github.loadup.components.cache.cfg.CacheBindingCfg;
import io.github.loadup.components.cache.model.CacheValueWrapper;
import io.github.loadup.components.cache.serializer.CacheSerializer;
import io.github.loadup.framework.api.binder.Binder;
import java.util.Collection;

public interface CacheBinder<C extends CacheBinderCfg, S extends CacheBindingCfg>
    extends Binder<C, S> {
  boolean set(String key, CacheValueWrapper value);

  CacheValueWrapper get(String key);

  boolean delete(String key);

  boolean deleteAll(Collection<String> keys);

  void cleanUp();

  CacheSerializer getSerializer();
}
