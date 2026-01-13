package com.github.loadup.framework.api.context;

import java.util.List;
import org.springframework.context.ApplicationContext;

/** 绑定上下文：包含创建 Binding 所需的所有原材料 */
public class BindingContext<B, C> {
  private final String name; // 业务映射名，如 "xxx"
  private final String type; // Binder类型，如 "s3"
  private final C config; // 具体的配置实例
  private final List<B> binders; // 候选驱动列表
  private final ApplicationContext applicationContext; // 必须持有此引用

  public BindingContext(
      String name, String type, C config, List<B> binders, ApplicationContext applicationContext) {
    this.name = name;
    this.type = type;
    this.config = config;
    this.binders = binders;
    this.applicationContext = applicationContext;
  }

  // Getters...
  public String getName() {
    return name;
  }

  public String getType() {
    return type;
  }

  public C getConfig() {
    return config;
  }

  public List<B> getBinders() {
    return binders;
  }

  public ApplicationContext getApplicationContext() {
    return applicationContext;
  }
}
