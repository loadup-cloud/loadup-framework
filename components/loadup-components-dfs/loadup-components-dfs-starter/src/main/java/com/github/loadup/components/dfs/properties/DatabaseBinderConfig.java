package com.github.loadup.components.dfs.properties;

import lombok.*;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class DatabaseBinderConfig extends BinderConfig {

  /** 数据库表名 */
  private String tableName;
}
