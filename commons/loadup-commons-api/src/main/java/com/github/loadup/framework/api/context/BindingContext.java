package com.github.loadup.framework.api.context;

import com.github.loadup.framework.api.binder.Binder;
import com.github.loadup.framework.api.cfg.BaseBindingCfg;
import java.util.Collections;
import java.util.List;
import org.springframework.context.ApplicationContext;

/** 绑定上下文：包含创建 Binding 所需的所有原材料 */
public class BindingContext<B extends Binder, C extends BaseBindingCfg> {
  /** 业务标识 (对应 YAML 中的 bindings 节点 key，如 userCache) */
  private final String bizTag;

  /** 驱动类型字符串 (对应 YAML 中的 binder-type，如 "caffeine,redis") */
  private final String binderType;

  /** 已经解析完成并完成属性绑定的业务配置对象 */
  private final C bindingCfg;

  /** 已经实例化并注入了配置的驱动列表 (有序，支持多级缓存) */
  private final List<B> binders;

  /** Spring 应用上下文，方便 Binding 内部手动获取其他 Bean */
  private final ApplicationContext applicationContext;

  public BindingContext(
      String bizTag,
      String binderType,
      C bindingCfg,
      List<B> binders,
      ApplicationContext applicationContext) {
    this.bizTag = bizTag;
    this.binderType = binderType;
    this.bindingCfg = bindingCfg;
    this.binders =
        binders != null ? Collections.unmodifiableList(binders) : Collections.emptyList();
    this.applicationContext = applicationContext;
  }

  /** 获取第一个驱动 (快捷方法，适用于单驱动场景) */
  public B getFirstBinder() {
    return binders.isEmpty() ? null : binders.get(0);
  }

  public String getBizTag() {
    return bizTag;
  }

  public String getBinderType() {
    return binderType;
  }

  public C getBindingCfg() {
    return bindingCfg;
  }

  public List<B> getBinders() {
    return binders;
  }

  public ApplicationContext getApplicationContext() {
    return applicationContext;
  }
}
