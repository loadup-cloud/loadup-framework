package com.github.loadup.framework.api.binding;

import com.github.loadup.framework.api.binder.Binder;
import com.github.loadup.framework.api.cfg.BaseBindingCfg;
import java.util.List;
import lombok.Data;

@Data
public abstract class AbstractBinding<B extends Binder, C extends BaseBindingCfg>
    implements Binding {
  protected C cfg;
  protected B binder;

  protected List<B> binderList;

  protected String name;

  protected String type;
}
