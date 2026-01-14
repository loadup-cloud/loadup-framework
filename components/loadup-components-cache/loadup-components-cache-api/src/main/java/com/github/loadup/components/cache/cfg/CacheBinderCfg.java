
package com.github.loadup.components.cache.cfg;

import com.github.loadup.components.cache.constants.CacheConstants;
import com.github.loadup.framework.api.cfg.BaseBinderCfg;

/**
 * @author lise
 * @version BaseCacheBindingCfg.java, v 0.1 2026年01月13日 18:02 lise
 */
public class CacheBinderCfg extends BaseBinderCfg {
  // 指定序列化器的 Bean 名称，例如 "defaultJsonCacheSerializer" 或 "kryoSerializer"
  private String serializerBeanName = CacheConstants.SERIALIZER_JSON;
  // 默认使用系统时间源
  private String tickerBeanName = CacheConstants.DEFAULT_TICKER;

  public String getSerializerBeanName() {
    return serializerBeanName;
  }

  public void setSerializerBeanName(String name) {
    this.serializerBeanName = name;
  }

  public String getTickerBeanName() {
    return tickerBeanName;
  }

  public void setTickerBeanName(String tickerBeanName) {
    this.tickerBeanName = tickerBeanName;
  }
}
