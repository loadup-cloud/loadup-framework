package com.github.loadup.commons.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class BinderEnum {

  /** 所有枚举通过实现该接口，可以统一获取其在配置中的标识 */
  @Getter
  @AllArgsConstructor
  public enum BinderName implements IEnum {
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
    public String getCode() {
      return name().toLowerCase();
    }

    @Override
    public String getDescription() {
      return name().toLowerCase();
    }

    @Override
    public String toString() {
      return getCode();
    }
  }

  @Getter
  @AllArgsConstructor
  public enum CacheBinder implements IEnum {
    REDIS,
    CAFFEINE;

    @Override
    public String getCode() {
      return name().toLowerCase();
    }

    public String getDescription() {
      return name().toLowerCase();
    }

    @Override
    public String toString() {
      return getCode();
    }
  }

  @Getter
  @AllArgsConstructor
  public enum SchedulerBinder implements IEnum {
    POWERJOB,
    QUARTZ,
    SIMPLEJOB,
    XXLJOB;

    @Override
    public String getCode() {
      return name().toLowerCase();
    }

    public String getDescription() {
      return name().toLowerCase();
    }

    @Override
    public String toString() {
      return getCode();
    }
  }

  @Getter
  @AllArgsConstructor
  public enum DfsBinder implements IEnum {
    S3,
    LOCAL,
    DATABASE;

    @Override
    public String getCode() {
      return name().toLowerCase();
    }

    public String getDescription() {
      return name().toLowerCase();
    }

    @Override
    public String toString() {
      return getCode();
    }
  }
}
