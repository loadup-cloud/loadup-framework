package com.github.loadup.components.dfs.s3.binder;

import com.github.loadup.components.dfs.api.DfsBinder;
import com.github.loadup.components.dfs.model.*;
import com.github.loadup.components.dfs.s3.cfg.S3DfsBinderCfg;
import com.github.loadup.framework.api.binder.AbstractBinder;

public class S3DfsBinder extends AbstractBinder<S3DfsBinderCfg> implements DfsBinder {

  @Override
  public String getBinderType() {
    return "";
  }

  @Override
  public FileMetadata upload(FileUploadRequest request) {
    return null;
  }

  @Override
  public FileDownloadResponse download(String fileId) {
    return null;
  }

  @Override
  public boolean delete(String fileId) {
    return false;
  }

  @Override
  public boolean exists(String fileId) {
    return false;
  }

  @Override
  public FileMetadata getMetadata(String fileId) {
    return null;
  }

  @Override
  public String getProviderName() {
    return "";
  }

  @Override
  public boolean isAvailable() {
    return false;
  }
}
