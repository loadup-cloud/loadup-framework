package io.github.loadup.modules.config.infrastructure.converter;

/*-
 * #%L
 * Loadup Modules Config Infrastructure
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import io.github.loadup.modules.config.domain.model.DictItem;
import io.github.loadup.modules.config.domain.model.DictType;
import io.github.loadup.modules.config.infrastructure.dataobject.DictItemDO;
import io.github.loadup.modules.config.infrastructure.dataobject.DictTypeDO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DictConverter {

    DictType toModel(DictTypeDO entity);

    DictTypeDO toEntity(DictType model);

    DictItem toModel(DictItemDO entity);

    DictItemDO toEntity(DictItem model);
}
