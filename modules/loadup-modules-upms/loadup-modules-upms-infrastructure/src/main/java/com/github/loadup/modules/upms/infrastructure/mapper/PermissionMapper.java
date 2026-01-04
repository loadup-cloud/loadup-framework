package com.github.loadup.modules.upms.infrastructure.mapper;

import com.github.loadup.modules.upms.domain.entity.Permission;
import com.github.loadup.modules.upms.infrastructure.dataobject.PermissionDO;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * Permission Entity <-> PermissionDO Mapper
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Mapper(componentModel = "spring")
public interface PermissionMapper {

  PermissionMapper INSTANCE = Mappers.getMapper(PermissionMapper.class);

  Permission toEntity(PermissionDO permissionDO);

  PermissionDO toDO(Permission permission);

  List<Permission> toEntityList(List<PermissionDO> permissionDOList);

  List<PermissionDO> toDOList(List<Permission> permissionList);
}
