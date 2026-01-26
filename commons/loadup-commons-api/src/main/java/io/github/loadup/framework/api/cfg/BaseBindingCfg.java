package io.github.loadup.framework.api.cfg;

public class BaseBindingCfg {

  private String binderType;
  private boolean enabled = true;

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getBinderType() {
    return binderType;
  }

  public void setBinderType(String binderType) {
    this.binderType = binderType;
  }
}
