package io.github.loadup.components.dfs.database.cfg;

import io.github.loadup.components.dfs.cfg.DfsBinderCfg;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class DatabaseDfsBinderCfg extends DfsBinderCfg {
  private String tableName = "file_storage";
}
