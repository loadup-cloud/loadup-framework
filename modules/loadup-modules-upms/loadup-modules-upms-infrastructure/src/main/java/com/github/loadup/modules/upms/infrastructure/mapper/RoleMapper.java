package com.github.loadup.modules.upms.infrastructure.mapper;

import com.github.loadup.modules.upms.domain.entity.Role;
import com.github.loadup.modules.upms.infrastructure.dataobject.RoleDO;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * Role Entity <-> RoleDO Mapper
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Mapper(componentModel = "spring")
public interface RoleMapper {

  RoleMapper INSTANCE = Mappers.getMapper(RoleMapper.class);

  Role toEntity(RoleDO roleDO);

  RoleDO toDO(Role role);

  List<Role> toEntityList(List<RoleDO> roleDOList);

  List<RoleDO> toDOList(List<Role> roleList);
}
