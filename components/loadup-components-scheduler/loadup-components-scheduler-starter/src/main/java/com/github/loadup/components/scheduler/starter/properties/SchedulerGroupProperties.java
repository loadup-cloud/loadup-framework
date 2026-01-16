package com.github.loadup.components.scheduler.starter.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "loadup.scheduler")
public class SchedulerGroupProperties {
  private SchedulerBinderType defaultBinder = SchedulerBinderType.SIMPLE_JOB;
}
