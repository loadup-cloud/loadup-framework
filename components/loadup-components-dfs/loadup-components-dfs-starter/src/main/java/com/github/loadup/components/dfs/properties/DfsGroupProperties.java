package com.github.loadup.components.dfs.properties;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.List;
import java.util.Map;
import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@Getter
@Setter
@ConfigurationProperties(prefix = "loadup.dfs")
public class DfsGroupProperties {

  /** 绑定的存储类型 */
  private BinderType defaultBinder = BinderType.LOCAL;

  /** 启用的绑定器列表 */
  private List<BinderType> enabledBinders;

  /** 文件类型与存储绑定的映射 */
  private Map<String, BindingConfig> bindings;

  /** 各种存储类型的配置 */
  @NestedConfigurationProperty
  @JsonDeserialize(using = MapBinderConverter.class)
  private Map<BinderType, BinderConfig> binders;
}
