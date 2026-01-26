package io.github.loadup.framework.api.factory;

import io.github.loadup.framework.api.binding.Binding;
import io.github.loadup.framework.api.context.BindingContext;

/**
 * Binding 工厂：负责将 Binder 和 Config 组装成业务 Binding
 *
 * @param <T> 具体 Binding 的类型
 */
@FunctionalInterface
public interface BindingFactory<T extends Binding> {

  /**
   * 创建 Binding 实例的核心方法
   *
   * @param context 包含配置和驱动的上下文
   * @return 组装好的 Binding 实例
   */
  T create(BindingContext<?, ?> context);
}
