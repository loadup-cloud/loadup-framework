package com.github.loadup.components.cache.starter.manager;

import com.github.loadup.components.cache.binder.CacheBinder;
import com.github.loadup.components.cache.binding.CacheBinding;
import com.github.loadup.components.cache.starter.properties.CacheGroupProperties;
import com.github.loadup.framework.api.manager.BindingManagerSupport;
import org.springframework.context.ApplicationContext;

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

  /** 实现内核要求的钩子：指定驱动接口类型，用于容器查找原型 Bean */
  @Override
  protected Class<CacheBinder> getBinderInterface() {
    return CacheBinder.class;
  }

  @Override
  public Class<CacheBinding> getBindingInterface() {
    return CacheBinding.class;
  }
}
