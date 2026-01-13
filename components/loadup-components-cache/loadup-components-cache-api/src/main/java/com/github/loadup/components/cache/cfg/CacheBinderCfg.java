/*
 * Ant Group
 * Copyright (c) 2004-2026 All Rights Reserved.
 */
package com.github.loadup.components.cache.cfg;

import com.github.loadup.components.cache.constants.CacheConstants;
import com.github.loadup.framework.api.cfg.BaseBinderCfg;
import com.github.loadup.framework.api.cfg.BaseBindingCfg;

/**
 * @author lise
 * @version BaseCacheBindingCfg.java, v 0.1 2026年01月13日 18:02 lise
 */
public class CacheBinderCfg extends BaseBinderCfg {
  // 指定序列化器的 Bean 名称，例如 "defaultJsonCacheSerializer" 或 "kryoSerializer"
  private String serializerBeanName = CacheConstants.JSON;

  public String getSerializerBeanName() {
    return serializerBeanName;
  }

  public void setSerializerBeanName(String name) {
    this.serializerBeanName = name;
  }
}
