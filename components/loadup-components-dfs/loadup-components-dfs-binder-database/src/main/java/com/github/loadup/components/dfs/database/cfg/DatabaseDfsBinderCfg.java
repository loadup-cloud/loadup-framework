package com.github.loadup.components.dfs.database.cfg;

import com.github.loadup.components.dfs.cfg.DfsBinderCfg;
import com.github.loadup.framework.api.cfg.BaseBinderCfg;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class DatabaseDfsBinderCfg extends DfsBinderCfg {
  private String tableName = "file_storage";
}
