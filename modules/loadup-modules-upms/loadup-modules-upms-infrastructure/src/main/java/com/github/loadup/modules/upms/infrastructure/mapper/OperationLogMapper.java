package com.github.loadup.modules.upms.infrastructure.mapper;

import com.github.loadup.modules.upms.domain.entity.OperationLog;
import com.github.loadup.modules.upms.infrastructure.dataobject.OperationLogDO;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * OperationLog Entity <-> OperationLogDO Mapper
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Mapper(componentModel = "spring")
public interface OperationLogMapper {

  OperationLogMapper INSTANCE = Mappers.getMapper(OperationLogMapper.class);

  OperationLog toEntity(OperationLogDO operationLogDO);

  OperationLogDO toDO(OperationLog operationLog);

  List<OperationLog> toEntityList(List<OperationLogDO> operationLogDOList);

  List<OperationLogDO> toDOList(List<OperationLog> operationLogList);
}
