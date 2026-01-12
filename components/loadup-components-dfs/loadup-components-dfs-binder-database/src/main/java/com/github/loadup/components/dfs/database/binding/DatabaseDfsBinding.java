package com.github.loadup.components.dfs.database.binding;

import com.github.loadup.components.dfs.binding.AbstractDfsBinding;
import com.github.loadup.components.dfs.database.binder.DatabaseDfsBinder;
import com.github.loadup.framework.api.cfg.BaseBindingCfg;

public class DatabaseDfsBinding extends AbstractDfsBinding<DatabaseDfsBinder, BaseBindingCfg> {

  @Override
  public String name() {
    return "database";
  }
}
