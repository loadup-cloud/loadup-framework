package io.github.loadup.modules.upms.infrastructure.converter;

import io.github.loadup.commons.mapping.LoadUpMapStructConfig;
import io.github.loadup.modules.upms.domain.entity.Department;
import io.github.loadup.modules.upms.infrastructure.dataobject.DepartmentDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Department Converter
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Mapper(config = LoadUpMapStructConfig.class, uses = AuditMappingSupport.class)
public interface DepartmentConverter {
    @Mapping(target = "tenantId", ignore = true)
    DepartmentDO toDataObject(Department department);

    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "leader", ignore = true)
    Department toEntity(DepartmentDO departmentDO);
}
