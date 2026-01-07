package com.github.loadup.framework.api.binder;

import com.github.loadup.framework.api.cfg.BaseBinderCfg;

public abstract class AbstractBinder<T extends BaseBinderCfg> implements Binder {
  protected T cfg;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  protected String name;
}
