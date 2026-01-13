package com.github.loadup.components.dfs.binding;

import com.github.loadup.components.dfs.api.DfsBinder;
import com.github.loadup.components.dfs.api.DfsBinding;
import com.github.loadup.components.dfs.model.*;
import com.github.loadup.framework.api.binding.AbstractBinding;

// DFS 专属 Binding 抽象
public abstract class AbstractDfsBinding<B extends DfsBinder, C> extends AbstractBinding<B, C>
    implements DfsBinding {

  // 封装领域公共逻辑：如路径规范化、文件校验
  @Override
  public  FileMetadata upload(FileUploadRequest request) {
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

  @Override
  public String generatePresignedUrl(String fileId, long expirationSeconds) {
    return getBinder().generatePresignedUrl(fileId, expirationSeconds);
  }

  @Override
  public FileMetadata copy(String sourceFileId, String targetPath) {
    return getBinder().copy(sourceFileId, targetPath);
  }
}
