package com.github.loadup.components.scheduler.binder;

import com.github.loadup.components.scheduler.cfg.SchedulerBinderCfg;
import com.github.loadup.components.scheduler.cfg.SchedulerBindingCfg;
import com.github.loadup.framework.api.binder.AbstractBinder;

public abstract class AbstractSchedulerBinder<
        C extends SchedulerBinderCfg, S extends SchedulerBindingCfg>
    extends AbstractBinder<C, S> implements SchedulerBinder<C, S> {
  @Override
  protected void afterConfigInjected(String name, C binderCfg, S bindingCfg) {
    // 自动从容器中根据配置的名称获取序列化器
    // 执行子类特有的初始化逻辑
    onInit();
  }

  protected abstract void onInit();
}
