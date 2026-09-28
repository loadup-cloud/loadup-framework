package io.github.loadup.components.dfs.database.mapper;

import com.mybatisflex.core.BaseMapper;
import io.github.loadup.components.dfs.database.dataobject.FileStorageDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FileStorageMapper extends BaseMapper<FileStorageDO> {}
