package io.github.loadup.components.scheduler.simplejob.cfg;

import io.github.loadup.components.scheduler.cfg.SchedulerBinderCfg;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class SimpleJobSchedulerBinderCfg extends SchedulerBinderCfg {
  private int poolSize = 1;
  private String threadNamePrefix = "simple-scheduler-";
}
