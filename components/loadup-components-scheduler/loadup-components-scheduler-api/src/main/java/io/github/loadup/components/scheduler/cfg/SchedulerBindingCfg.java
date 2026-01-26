package io.github.loadup.components.scheduler.cfg;

import io.github.loadup.framework.api.cfg.BaseBindingCfg;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class SchedulerBindingCfg extends BaseBindingCfg {
  private String cron;
  private Long fixedDelay;
  private Long initialDelay;
}
