package com.github.loadup.components.scheduler.starter.manager;

import com.github.loadup.components.scheduler.binder.SchedulerBinder;
import com.github.loadup.components.scheduler.binding.SchedulerBinding;
import com.github.loadup.components.scheduler.core.SchedulerTaskRegistry;
import com.github.loadup.components.scheduler.starter.properties.SchedulerGroupProperties;
import com.github.loadup.framework.api.manager.BindingManagerSupport;
import org.springframework.context.ApplicationContext;

public class SchedulerBindingManager
    extends BindingManagerSupport<SchedulerBinder, SchedulerBinding> {
  private final SchedulerGroupProperties groupProps;

  public SchedulerBindingManager(SchedulerGroupProperties props, ApplicationContext context) {
    // 传入 Spring 上下文和配置前缀：loadup.scheduler
    super(context, "loadup.scheduler");
    this.groupProps = props;
  }

  @Override
  protected String getDefaultBinderType() {
    return groupProps.getDefaultBinder().getValue();
  }

  @Override
  public Class<SchedulerBinding> getBindingInterface() {
    return SchedulerBinding.class;
  }

  @Override
  public Class<SchedulerBinder> getBinderInterface() {
    return SchedulerBinder.class;
  }
}
