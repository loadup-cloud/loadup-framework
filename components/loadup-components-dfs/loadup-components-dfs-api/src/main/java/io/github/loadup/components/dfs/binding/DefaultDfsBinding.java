package io.github.loadup.components.dfs.binding;

import io.github.loadup.components.dfs.binder.DfsBinder;
import io.github.loadup.components.dfs.cfg.DfsBindingCfg;
import io.github.loadup.components.dfs.model.FileDownloadResponse;
import io.github.loadup.components.dfs.model.FileMetadata;
import io.github.loadup.components.dfs.model.FileUploadRequest;
import io.github.loadup.framework.api.binding.AbstractBinding;

public class DefaultDfsBinding extends AbstractBinding<DfsBinder<?, DfsBindingCfg>, DfsBindingCfg>
    implements DfsBinding {

  @Override
  public FileMetadata upload(FileUploadRequest request) {
    return getBinder().upload(request);
  }

  @Override
  public FileDownloadResponse download(String fileId) {
    return getBinder().download(fileId);
  }

  @Override
  public boolean delete(String fileId) {
    return getBinder().delete(fileId);
  }

  @Override
  public boolean exists(String fileId) {
    return getBinder().exists(fileId);
  }

  @Override
  public FileMetadata getMetadata(String fileId) {
    return getBinder().getMetadata(fileId);
  }
}
