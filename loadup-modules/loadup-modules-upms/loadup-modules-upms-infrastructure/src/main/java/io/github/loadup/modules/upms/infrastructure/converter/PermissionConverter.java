package io.github.loadup.modules.upms.infrastructure.converter;

import io.github.loadup.commons.mapping.LoadUpMapStructConfig;
import io.github.loadup.modules.upms.domain.entity.Permission;
import io.github.loadup.modules.upms.infrastructure.dataobject.PermissionDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Permission Converter
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Mapper(config = LoadUpMapStructConfig.class, uses = AuditMappingSupport.class)
public interface PermissionConverter {
    @Mapping(target = "tenantId", ignore = true)
    PermissionDO toDataObject(Permission permission);

    Permission toEntity(PermissionDO permissionDO);
}
