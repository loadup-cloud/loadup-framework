package io.github.loadup.modules.upms.infrastructure.converter;

import io.github.loadup.commons.mapping.LoadUpMapStructConfig;
import io.github.loadup.modules.upms.domain.entity.LoginLog;
import io.github.loadup.modules.upms.infrastructure.dataobject.LoginLogDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * LoginLog Converter - MapStruct converter between Domain Entity and DataObject
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Mapper(config = LoadUpMapStructConfig.class)
public interface LoginLogConverter {

    /**
     * Convert Domain Entity to DataObject
     *
     * @param loginLog domain entity
     * @return data object
     */
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    LoginLogDO toDataObject(LoginLog loginLog);

    /**
     * Convert DataObject to Domain Entity
     *
     * @param loginLogDO data object
     * @return domain entity
     */
    LoginLog toEntity(LoginLogDO loginLogDO);
}
