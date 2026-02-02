package io.github.loadup.components.cache.binding;

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

import io.github.loadup.components.cache.binder.CacheBinder;
import io.github.loadup.components.cache.cfg.CacheBindingCfg;
import io.github.loadup.framework.api.binding.Binding;
import java.util.Collection;

/**
 * 缓存领域 Binding 接口 职责：Key 修饰、多级缓存协调、类型转换、反序列化调度
 *
 * @param <B> 驱动类型：必须是 CacheBinder，且其业务配置类型必须与 S 一致
 * @param <S> 业务配置类型：必须是 CacheBindingCfg 或其子类
 */
public interface CacheBinding extends Binding<CacheBinder<?, CacheBindingCfg>, CacheBindingCfg> {
  boolean set(String key, Object value);

  Object get(String key);

  <T> T getObject(String key, Class<T> cls);

  boolean delete(String key);

  boolean deleteAll(Collection<String> keys);

  void cleanUp();
}
