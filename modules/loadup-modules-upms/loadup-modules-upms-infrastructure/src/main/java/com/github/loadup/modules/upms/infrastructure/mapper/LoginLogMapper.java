package com.github.loadup.modules.upms.infrastructure.mapper;

import com.github.loadup.modules.upms.domain.entity.LoginLog;
import com.github.loadup.modules.upms.infrastructure.dataobject.LoginLogDO;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * LoginLog Entity <-> LoginLogDO Mapper
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Mapper(componentModel = "spring")
public interface LoginLogMapper {

  LoginLogMapper INSTANCE = Mappers.getMapper(LoginLogMapper.class);

  LoginLog toEntity(LoginLogDO loginLogDO);

  LoginLogDO toDO(LoginLog loginLog);

  List<LoginLog> toEntityList(List<LoginLogDO> loginLogDOList);

  List<LoginLogDO> toDOList(List<LoginLog> loginLogList);
}
