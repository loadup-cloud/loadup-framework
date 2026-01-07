package com.github.loadup.framework.api.binding;

import com.github.loadup.framework.api.binder.Binder;
import com.github.loadup.framework.api.cfg.BaseBindingCfg;
import java.util.List;

public abstract class AbstractBinding<B extends Binder, C extends BaseBindingCfg>
    implements Binding {
  public C getCfg() {
    return cfg;
  }

  public void setCfg(C cfg) {
    this.cfg = cfg;
  }

  public B getBinder() {
    return binder;
  }

  public void setBinder(B binder) {
    this.binder = binder;
  }

  public List<B> getBinderList() {
    return binderList;
  }

  public void setBinderList(List<B> binderList) {
    this.binderList = binderList;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  protected C cfg;
  protected B binder;

  protected List<B> binderList;

  protected String name;

  protected String type;
}
