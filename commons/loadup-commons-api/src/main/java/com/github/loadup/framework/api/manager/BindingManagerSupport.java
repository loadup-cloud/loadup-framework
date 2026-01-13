package com.github.loadup.framework.api.manager;

import com.github.loadup.exception.BinderNotFoundException;
import com.github.loadup.framework.api.binder.Binder;
import com.github.loadup.framework.api.binding.Binding;
import com.github.loadup.framework.api.cfg.BaseBindingCfg;
import com.github.loadup.framework.api.context.BindingContext;
import java.util.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.springframework.context.ApplicationContext;

public abstract class BindingManagerSupport<B extends Binder, T extends Binding> {
  protected final ApplicationContext context;
  protected final String configPrefix;

  protected final Map<String, BindingMetadata<?, ?, ?, ?>> registry = new ConcurrentHashMap<>();
  protected final Map<String, T> bindingCache = new ConcurrentHashMap<>();
  protected final Map<String, Object> binderCfgCache = new ConcurrentHashMap<>();

  protected BindingManagerSupport(ApplicationContext context, String configPrefix) {
    this.context = context;
    this.configPrefix = configPrefix;
  }

  public void register(String type, BindingMetadata meta) {
    registry.put(type.toLowerCase(), meta);
  }

  @SuppressWarnings("unchecked")
  public <R extends T> R getBinding(String bizTag) {
    return (R)
        bindingCache.computeIfAbsent(
            bizTag,
            tag -> {
              // 1. 探测类型 (逻辑不变)
              BaseBindingCfg basic =
                  resolveConfig(configPrefix + ".bindings." + tag, BaseBindingCfg.class);
              String type =
                  (basic.getBinderType() != null)
                      ? basic.getBinderType().toLowerCase()
                      : getDefaultBinderType();

              // 2. 获取元数据
              BindingMetadata<?, ?, ?, ?> meta = registry.get(type);
              if (meta == null) {
                throw new BinderNotFoundException("Binder not registered: " + type);
              }

              // 3. 关键：调用泛型捕获方法，将通配符转换为具体的泛型 B, C_BIND, C_BINDR
              return (T) captureAndCreate(tag, type, (BindingMetadata) meta);
            });
  }

  /** 捕获方法：在这个方法内，泛型被重新绑定，编译器不再抱怨 Object 转换问题 */
  @SuppressWarnings("unchecked")
  private <B_SUB extends B, CBIND, CBINDR, T_SUB extends T> T_SUB captureAndCreate(
      String tag, String type, BindingMetadata<B_SUB, CBIND, CBINDR, T_SUB> meta) {

    // 解析配置
    CBIND bindingCfg = resolveConfig(configPrefix + ".bindings." + tag, meta.bindingCfgClass);
    CBINDR binderCfg =
        (CBINDR)
            binderCfgCache.computeIfAbsent(
                type, t -> resolveConfig(configPrefix + ".binders." + t, meta.binderCfgClass));

    // 动态实例化 Binder (注意：B_SUB 是 B 的子类)

    // 准备上下文
    // List<B_SUB> binders = Collections.singletonList(binderInstance);
    List<B_SUB> binders = new ArrayList<>();
    // 如果配置中支持多选，则循环创建
    binders.add(createOne(meta, binderCfg));

    BindingContext<B_SUB, CBIND> ctx =
        new BindingContext<>(tag, type, bindingCfg, binders, context);

    // 调用工厂创建 Binding 并初始化
    T_SUB bindingInstance = meta.factory.create(ctx);
    bindingInstance.init(ctx);
    return bindingInstance;
  }

  private <B_SUB extends B, CBIND, CBINDR, T_SUB extends T> B_SUB createOne(
      BindingMetadata<B_SUB, CBIND, CBINDR, T_SUB> meta, CBINDR binderCfg) {
    B_SUB binderInstance =
        (B_SUB) context.getAutowireCapableBeanFactory().createBean(meta.binderClass);
    binderInstance.injectBinderConfig(binderCfg);
    return binderInstance;
  }

  @SuppressWarnings("unchecked")
  private T doCreateBinding(String tag, String type, BindingMetadata meta) {
    // 解析双层配置
    Object bindingCfg = resolveConfig(configPrefix + ".bindings." + tag, meta.bindingCfgClass);
    Object binderCfg =
        binderCfgCache.computeIfAbsent(
            type, t -> resolveConfig(configPrefix + ".binders." + t, meta.binderCfgClass));

    // 实例化并装配 Binder 列表
    List<B> binders = createAndAssembleBinders(type, binderCfg);

    // 创建上下文并生成 Binding 实例
    BindingContext<B, Object> ctx = new BindingContext<>(tag, type, bindingCfg, binders, context);
    T instance = (T) meta.factory.create(ctx);

    // 统一初始化
    instance.init(ctx);
    return instance;
  }

  private List<B> createAndAssembleBinders(String type, Object binderCfg) {
    return context.getBeansOfType(getBinderInterface()).values().stream()
        .filter(b -> b.getBinderType().equalsIgnoreCase(type))
        .map(
            prototype -> {
              // 创建新实例并注入配置
              B newBinder =
                  (B) context.getAutowireCapableBeanFactory().createBean(prototype.getClass());
              newBinder.injectBinderConfig(binderCfg);
              return newBinder;
            })
        .collect(Collectors.toList());
  }

  protected abstract String getDefaultBinderType();

  protected abstract Class<B> getBinderInterface();

  public abstract Class<T> getBindingInterface();

  protected <C> C resolveConfig(String path, Class<C> configClass) {
    return org.springframework.boot.context.properties.bind.Binder.get(context.getEnvironment())
        .bind(path, configClass)
        .orElseGet(() -> createDefaultConfig(configClass));
  }

  private <C> C createDefaultConfig(Class<C> configClass) {
    // 这里可以根据业务决定是直接报错还是返回空对象
    try {
      return configClass.getDeclaredConstructor().newInstance();
    } catch (Exception e) {
      throw new RuntimeException(e);
    } // 如果是 getBinding 探测，建议报错：bizTag 未在配置文件中定义
  }
}
