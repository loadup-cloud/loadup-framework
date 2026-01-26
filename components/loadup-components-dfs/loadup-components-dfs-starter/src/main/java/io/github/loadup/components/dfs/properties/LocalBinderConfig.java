package io.github.loadup.components.dfs.properties;

import lombok.*;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class LocalBinderConfig extends BinderConfig {

  /** 本地存储基础路径 */
  private String basePath;
}
