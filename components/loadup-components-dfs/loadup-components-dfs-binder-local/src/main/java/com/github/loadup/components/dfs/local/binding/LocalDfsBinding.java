package com.github.loadup.components.dfs.local.binding;

import com.github.loadup.components.dfs.binding.AbstractDfsBinding;
import com.github.loadup.components.dfs.local.binder.LocalDfsBinder;
import com.github.loadup.framework.api.cfg.BaseBindingCfg;

public class LocalDfsBinding extends AbstractDfsBinding<LocalDfsBinder, BaseBindingCfg> {

  @Override
  public String name() {
    return "local";
  }
}
