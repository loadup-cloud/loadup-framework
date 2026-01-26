package io.github.loadup.framework.api.cfg;

public class BaseBinderCfg {
  protected String name;
  protected String binder;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getBinder() {
    return binder;
  }

  public void setBinder(String binder) {
    this.binder = binder;
  }
}
