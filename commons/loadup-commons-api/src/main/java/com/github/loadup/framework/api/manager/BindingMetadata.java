package com.github.loadup.framework.api.manager;

import com.github.loadup.framework.api.binding.Binding;
import com.github.loadup.framework.api.factory.BindingFactory;

/** 内部类或独立类，用于保存 Binder 类型与具体实现类的映射关系 */
// 扩展后的元数据，持有双配置类型
public class BindingMetadata<B, C_BIND, C_BINDR, T extends Binding> {
  public final String type; // 新增：caffeine / redis
  public final Class<C_BIND> bindingCfgClass;
  public final Class<C_BINDR> binderCfgClass;
  public final Class<? extends B> binderClass;
  public final Class<T> bindingClass;
  public final BindingFactory<T> factory;

  // 构造函数强制要求类型对齐
  public BindingMetadata(
      String type,
      Class<C_BIND> bindingCfgClass,
      Class<C_BINDR> binderCfgClass,
      Class<? extends B> binderClass,
      Class<T> bindingClass,
      BindingFactory<T> factory) {
    this.type = type;
    this.bindingCfgClass = bindingCfgClass;
    this.binderCfgClass = binderCfgClass;
    this.binderClass = binderClass;
    this.bindingClass = bindingClass;
    this.factory = factory;
  }
}
