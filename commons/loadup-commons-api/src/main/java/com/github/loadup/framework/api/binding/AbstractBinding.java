package com.github.loadup.framework.api.binding;

import com.github.loadup.framework.api.context.BindingContext;
import java.util.List;

public abstract class AbstractBinding<B, C> implements Binding {
  protected C cfg;
  protected List<B> binders;
  protected String name;
  protected String type;
  private BindingContext<B, C> context; // 核心上下文

  /** 获取首选驱动（通常 binders 列表中至少有一个） */
  protected B getBinder() {
    if (binders == null || binders.isEmpty()) {
      throw new IllegalStateException("No binders available for " + name);
    }
    // 默认返回第一个驱动
    return binders.getFirst();
  }

  public C getCfg() {
    return cfg;
  }

  public void setCfg(C cfg) {
    this.cfg = cfg;
  }

  public List<B> getBinders() {
    return binders;
  }

  public void setBinders(List<B> binders) {
    this.binders = binders;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  @Override
  @SuppressWarnings("unchecked")
  public void init(BindingContext<?, ?> context) {
    this.context = (BindingContext<B, C>) context;
    this.name = context.getName();
    this.type = context.getType();
    // 这里的强制转型是安全的，因为 Manager 保证了传入的类型匹配
    this.cfg = (C) context.getConfig();
    this.binders = (List<B>) context.getBinders();

    // 留给子类的钩子
    afterInit();
  }

  /** 子类扩展点：如果 S3Binding 需要在拿到配置后初始化 AmazonS3 客户端，写在这里 */
  protected void afterInit() {
    // 默认空实现
  }

  public final void destroy() {
    afterDestroy();
  }

  protected void afterDestroy() {
    // 默认空实现
  }

  protected BindingContext<B, C> getContext() {
    return this.context;
  }
}
