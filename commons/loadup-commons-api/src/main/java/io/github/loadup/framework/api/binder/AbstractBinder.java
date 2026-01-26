package io.github.loadup.framework.api.binder;

import io.github.loadup.framework.api.cfg.BaseBinderCfg;
import io.github.loadup.framework.api.cfg.BaseBindingCfg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

public abstract class AbstractBinder<C extends BaseBinderCfg, S extends BaseBindingCfg>
    implements Binder<C, S> {
  protected C binderCfg;
  protected S bindingCfg;
  protected String name;
  // 关键：动态创建时，Spring 会自动扫描并注入此字段
  @Autowired protected ApplicationContext context;

  public C getBinderCfg() {
    return binderCfg;
  }

  public void setBinderCfg(C binderCfg) {
    this.binderCfg = binderCfg;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public final void injectConfigs(String name, C binderCfg, S bindingCfg) {
    this.name = name;
    this.binderCfg = binderCfg;
    this.bindingCfg = bindingCfg;

    // 执行初始化前的准备工作
    this.afterConfigInjected(name, binderCfg, bindingCfg);
  }

  /** 钩子方法：配置注入完成后，子类可以在这里初始化 SDK 客户端 */
  protected void afterConfigInjected(String name, C binderCfg, S bindingCfg) {}

  protected void afterDestroy() {}

  public void destroy() throws Exception {
    try {
      this.afterDestroy();
    } catch (Exception e) {
      throw e;
    }
  }
}
