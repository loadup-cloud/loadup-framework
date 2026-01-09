package com.github.loadup.components.dfs.properties;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "loadup.dfs")
public class DfsGroupProperties {
  private String defaultBinder = "local";
  private Map<String, String> mappings = new HashMap<>();
}
