package com.github.loadup.framework.api.binder;

import com.github.loadup.framework.api.cfg.BaseBinderCfg;

public abstract class AbstractBinder<T extends BaseBinderCfg> implements Binder {
  protected T binderCfg;
  protected String name;

  public T getBinderCfg() {
    return binderCfg;
  }

  public void setBinderCfg(T binderCfg) {
    this.binderCfg = binderCfg;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  @SuppressWarnings("unchecked")
  public void injectBinderConfig(Object config) {
    // 在这里完成从 Object 到具体 T 的转型
    this.binderCfg = (T) config;
    this.afterConfigInjected();
  }

  /** 钩子方法：配置注入完成后，子类可以在这里初始化 SDK 客户端 */
  protected void afterConfigInjected() {}

  protected void afterDestroy() {}

  @Override
  public void destroy() throws Exception {
    try {
      this.afterDestroy();
    } catch (Exception e) {
      throw e;
    }
  }
}
