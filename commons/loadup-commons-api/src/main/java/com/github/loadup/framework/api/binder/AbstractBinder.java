package com.github.loadup.framework.api.binder;

import com.github.loadup.framework.api.cfg.BaseBinderCfg;
import lombok.Data;

@Data
public abstract class AbstractBinder<T extends BaseBinderCfg> implements Binder {
  protected T cfg;

  protected String name;
}
