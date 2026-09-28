package io.github.loadup.modules.upms.infrastructure.converter;

import io.github.loadup.commons.mapping.LoadUpMapStructConfig;
import io.github.loadup.modules.upms.domain.entity.Role;
import io.github.loadup.modules.upms.infrastructure.dataobject.RoleDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Role Converter
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Mapper(config = LoadUpMapStructConfig.class, uses = AuditMappingSupport.class)
public interface RoleConverter {
    @Mapping(target = "tenantId", ignore = true)
    RoleDO toDataObject(Role role);

    Role toEntity(RoleDO roleDO);
}
