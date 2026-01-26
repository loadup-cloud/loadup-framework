package io.github.loadup.modules.upms.infrastructure.converter;

/*-
 * #%L
 * loadup-modules-upms-infrastructure
 * %%
 * Copyright (C) 2022 - 2026 loadup_cloud
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

import io.github.loadup.modules.upms.domain.entity.LoginLog;
import io.github.loadup.modules.upms.infrastructure.dataobject.LoginLogDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * LoginLog Converter - MapStruct converter between Domain Entity and DataObject
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Mapper(componentModel = "spring")
public interface LoginLogConverter {

  LoginLogConverter INSTANCE = Mappers.getMapper(LoginLogConverter.class);

  /**
   * Convert Domain Entity to DataObject
   *
   * @param loginLog domain entity
   * @return data object
   */
  LoginLogDO toDataObject(LoginLog loginLog);

  /**
   * Convert DataObject to Domain Entity
   *
   * @param loginLogDO data object
   * @return domain entity
   */
  LoginLog toEntity(LoginLogDO loginLogDO);
}
