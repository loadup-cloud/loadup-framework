package com.github.loadup.components.cache.cfg;

import com.github.loadup.framework.api.cfg.BaseBindingCfg;
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
