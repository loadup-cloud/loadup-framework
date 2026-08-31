package io.github.loadup.modules.config.infrastructure.converter;

/*-
 * #%L
 * Loadup Modules Config Infrastructure
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
