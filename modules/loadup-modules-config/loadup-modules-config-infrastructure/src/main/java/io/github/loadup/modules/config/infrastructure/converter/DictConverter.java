package io.github.loadup.modules.config.infrastructure.converter;

import io.github.loadup.modules.config.infrastructure.dataobject.DictItemDO;
import io.github.loadup.modules.config.infrastructure.dataobject.DictTypeDO;
import io.github.loadup.modules.config.domain.model.DictItem;
import io.github.loadup.modules.config.domain.model.DictType;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DictConverter {

    DictType toModel(DictTypeDO entity);

    DictTypeDO toEntity(DictType model);

    DictItem toModel(DictItemDO entity);

    DictItemDO toEntity(DictItem model);
}

