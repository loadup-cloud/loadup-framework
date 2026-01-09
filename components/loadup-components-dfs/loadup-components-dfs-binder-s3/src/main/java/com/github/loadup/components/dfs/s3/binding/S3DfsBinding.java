package com.github.loadup.components.dfs.s3.binding;

import com.github.loadup.components.dfs.binding.AbstractDfsBinding;
import com.github.loadup.components.dfs.s3.binder.S3DfsBinder;
import com.github.loadup.framework.api.cfg.BaseBindingCfg;

public class S3DfsBinding extends AbstractDfsBinding<S3DfsBinder, BaseBindingCfg> {

  @Override
  public String name() {
    return "";
  }
}
