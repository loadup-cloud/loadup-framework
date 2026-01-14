
package com.github.loadup.components.cache.caffeine.binding;

import com.github.loadup.components.cache.binding.AbstractCacheBinding;
import com.github.loadup.components.cache.caffeine.binder.CaffeineCacheBinder;
import com.github.loadup.components.cache.cfg.CacheBindingCfg;
import com.github.loadup.components.cache.serializer.CacheSerializer;
import com.github.loadup.framework.api.context.BindingContext;
import org.springframework.context.ApplicationContext;

/**
 * @author lise
 * @version CaffeineCacheBinding.java, v 0.1 2026年01月13日 16:46 lise
 */
public class CaffeineCacheBinding
    extends AbstractCacheBinding<CaffeineCacheBinder, CacheBindingCfg> {

  @Override
  public String name() {
    return "caffeine";
  }


}
