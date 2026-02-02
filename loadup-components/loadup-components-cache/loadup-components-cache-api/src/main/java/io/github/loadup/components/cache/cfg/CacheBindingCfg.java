package io.github.loadup.components.cache.cfg;

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

import io.github.loadup.framework.api.cfg.BaseBindingCfg;
import java.time.Duration;

/**
 * @author lise
 * @version BaseCacheBindingCfg.java, v 0.1 2026年01月13日 18:02 lise
 */
public class CacheBindingCfg extends BaseBindingCfg {
  private int database;

  /** 业务 Key 前缀（如 user:） */
  private String keyPrefix;

  /** : 业务级过期时间（覆盖组件默认值）。 */
  private Duration expireAfterWrite = Duration.ZERO;

  private String serializerBeanName;

  public String getKeyPrefix() {
    return keyPrefix;
  }

  public CacheBindingCfg setKeyPrefix(String keyPrefix) {
    this.keyPrefix = keyPrefix;
    return this;
  }

  public Duration getExpireAfterWrite() {
    return expireAfterWrite;
  }

  public CacheBindingCfg setExpireAfterWrite(Duration expireAfterWrite) {
    this.expireAfterWrite = expireAfterWrite;
    return this;
  }

  public String getSerializerBeanName() {
    return serializerBeanName;
  }

  public CacheBindingCfg setSerializerBeanName(String serializerBeanName) {
    this.serializerBeanName = serializerBeanName;
    return this;
  }

  public int getDatabase() {
    return database;
  }

  public void setDatabase(int database) {
    this.database = database;
  }
}
