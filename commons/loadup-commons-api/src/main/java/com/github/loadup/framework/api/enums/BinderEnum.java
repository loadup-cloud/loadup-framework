package com.github.loadup.framework.api.enums;

import com.github.loadup.commons.enums.ValueEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class BinderEnum {

  /** 所有枚举通过实现该接口，可以统一获取其在配置中的标识 */
  @Getter
  @AllArgsConstructor
  public enum BinderName implements ValueEnum {
    CACHE,
    DFS,
    SCHEDULER,
    CONFIG,
    RPC,
    BIGTABLE,
    FLOW,
    KMS,
    SEARCH,
    STREAM,
    RDBMS;

    @Override
    public String getValue() {
      return name().toLowerCase();
    }

    @Override
    public String toString() {
      return getValue();
    }
  }

  @Getter
  @AllArgsConstructor
  public enum CacheBinder implements ValueEnum {
    REDIS,
    CAFFEINE;

    @Override
    public String getValue() {
      return name().toLowerCase();
    }

    @Override
    public String toString() {
      return getValue();
    }
  }

  @Getter
  @AllArgsConstructor
  public enum SchedulerBinder implements ValueEnum {
    POWERJOB,
    QUARTZ,
    SIMPLEJOB,
    XXLJOB;

    @Override
    public String getValue() {
      return name().toLowerCase();
    }

    @Override
    public String toString() {
      return getValue();
    }
  }

  @Getter
  @AllArgsConstructor
  public enum DfsBinder implements ValueEnum {
    S3,
    LOCAL,
    DATABASE;

    @Override
    public String getValue() {
      return name().toLowerCase();
    }

    @Override
    public String toString() {
      return getValue();
    }
  }
}
