package com.github.loadup.components.cache.redis.binding;

import com.github.loadup.components.cache.binding.AbstractCacheBinding;
import com.github.loadup.components.cache.cfg.CacheBindingCfg;
import com.github.loadup.components.cache.redis.binder.RedisCacheBinder;

/**
 * @author lise
 * @version RedisCacheBinding.java, v 0.1 2026年01月13日 16:46 lise
 */
public class RedisCacheBinding extends AbstractCacheBinding<RedisCacheBinder, CacheBindingCfg> {

  @Override
  public String name() {
    return "redis";
  }
}
