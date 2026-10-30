/*
 * #%L
 * Loadup Modules UPMS Infrastructure Layer
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
package io.github.loadup.modules.upms.infrastructure.converter;

import io.github.loadup.commons.mapping.LoadUpMapStructConfig;
import io.github.loadup.modules.upms.domain.model.LoginLog;
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
