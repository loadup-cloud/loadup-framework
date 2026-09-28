package io.github.loadup.modules.upms.infrastructure.converter;

import io.github.loadup.commons.mapping.LoadUpMapStructConfig;
import io.github.loadup.modules.upms.domain.entity.User;
import io.github.loadup.modules.upms.infrastructure.dataobject.UserDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * User Converter - MapStruct converter between Domain Entity and DataObject
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Mapper(config = LoadUpMapStructConfig.class, uses = AuditMappingSupport.class)
public interface UserConverter {

    /**
     * Convert Domain Entity to DataObject
     *
     * @param user domain entity
     * @return data object
     */
    @Mapping(target = "tenantId", ignore = true)
    UserDO toDataObject(User user);

    /**
     * Convert DataObject to Domain Entity
     *
     * @param userDO data object
     * @return domain entity
     */
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "department", ignore = true)
    User toEntity(UserDO userDO);
}
