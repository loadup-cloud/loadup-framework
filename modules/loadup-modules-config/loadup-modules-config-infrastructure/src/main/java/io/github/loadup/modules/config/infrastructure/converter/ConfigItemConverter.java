package io.github.loadup.modules.config.infrastructure.converter;

import io.github.loadup.modules.config.infrastructure.dataobject.ConfigItemDO;
import io.github.loadup.modules.config.domain.model.ConfigItem;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ConfigItemConverter {

    ConfigItem toModel(ConfigItemDO entity);

    ConfigItemDO toEntity(ConfigItem model);
}

