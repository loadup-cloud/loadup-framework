package com.github.loadup.components.cache.starter.manager;

import com.github.loadup.components.cache.binder.CacheBinder;
import com.github.loadup.components.cache.binding.CacheBinding;
import com.github.loadup.components.cache.starter.properties.CacheGroupProperties;
import com.github.loadup.framework.api.binder.Binder;
import com.github.loadup.framework.api.binding.Binding;
import com.github.loadup.framework.api.cfg.BaseBinderCfg;
import com.github.loadup.framework.api.cfg.BaseBindingCfg;
import com.github.loadup.framework.api.manager.BindingManagerSupport;
import org.springframework.context.ApplicationContext;

import java.util.List;

/** Cache 绑定管理器 继承通用内核，指定驱动类型为 CacheBinder，业务接口为 Binding */
public class CacheBindingManager extends BindingManagerSupport<CacheBinder, CacheBinding> {

  private final CacheGroupProperties groupProps;

  public CacheBindingManager(CacheGroupProperties props, ApplicationContext context) {
    // 传入 Spring 上下文和配置前缀：loadup.cache
    super(context, "loadup.cache");
    this.groupProps = props;
  }

  /** 实现内核要求的钩子：获取默认 Binder 类型 */
  @Override
  protected String getDefaultBinderType() {
    return groupProps.getDefaultBinder().getValue();
  }

  @Override
  public Class<CacheBinding> getBindingInterface() {
    return CacheBinding.class;
  }

  @Override
  public Class<CacheBinder> getBinderInterface() {
    return CacheBinder.class;
  }
}
