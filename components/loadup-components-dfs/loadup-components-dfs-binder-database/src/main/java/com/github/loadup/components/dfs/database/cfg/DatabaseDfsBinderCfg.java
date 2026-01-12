package com.github.loadup.components.dfs.database.cfg;

import com.github.loadup.framework.api.cfg.BaseBinderCfg;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DatabaseDfsBinderCfg extends BaseBinderCfg {
  private String tableName = "file_storage";
}
