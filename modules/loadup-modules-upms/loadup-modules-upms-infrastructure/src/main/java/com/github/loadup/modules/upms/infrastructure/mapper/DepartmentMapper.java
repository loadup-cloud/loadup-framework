package com.github.loadup.modules.upms.infrastructure.mapper;

import com.github.loadup.modules.upms.domain.entity.Department;
import com.github.loadup.modules.upms.infrastructure.dataobject.DepartmentDO;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * Department Entity <-> DepartmentDO Mapper
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Mapper(componentModel = "spring")
public interface DepartmentMapper {

  DepartmentMapper INSTANCE = Mappers.getMapper(DepartmentMapper.class);

  Department toEntity(DepartmentDO departmentDO);

  DepartmentDO toDO(Department department);

  List<Department> toEntityList(List<DepartmentDO> departmentDOList);

  List<DepartmentDO> toDOList(List<Department> departmentList);
}
