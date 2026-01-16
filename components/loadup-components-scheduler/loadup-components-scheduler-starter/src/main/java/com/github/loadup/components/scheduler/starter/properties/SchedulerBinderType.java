package com.github.loadup.components.scheduler.starter.properties;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum SchedulerBinderType {
  SIMPLE_JOB("simplejob"),
  QUARTZ("quartz"),
  XXL_JOB("xxljob"),
  POWER_JOB("powerjob"),
  ;

  private final String value;

  SchedulerBinderType(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  @JsonCreator
  public static SchedulerBinderType fromString(String value) {
    if (value == null) return null;

    for (SchedulerBinderType type : SchedulerBinderType.values()) {
      if (type.value.equalsIgnoreCase(value)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown binder type: " + value);
  }
}
