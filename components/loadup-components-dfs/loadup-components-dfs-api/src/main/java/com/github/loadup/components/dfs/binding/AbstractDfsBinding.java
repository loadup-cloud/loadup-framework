package com.github.loadup.components.dfs.binding;

import com.github.loadup.components.dfs.api.DfsBinder;
import com.github.loadup.components.dfs.model.FileMetadata;
import com.github.loadup.components.dfs.model.FileUploadRequest;
import com.github.loadup.framework.api.binding.AbstractBinding;

// DFS 专属 Binding 抽象
public abstract class AbstractDfsBinding<B extends DfsBinder, C> extends AbstractBinding<B, C> {

  // 封装领域公共逻辑：如路径规范化、文件校验
  public final FileMetadata upload(FileUploadRequest request) {
    return getBinder().upload(request);
  }
}
