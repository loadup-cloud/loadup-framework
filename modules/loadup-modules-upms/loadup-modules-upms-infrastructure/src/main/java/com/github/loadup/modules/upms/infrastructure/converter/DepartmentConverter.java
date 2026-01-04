package com.github.loadup.modules.upms.infrastructure.converter;

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

import com.github.loadup.modules.upms.domain.entity.Department;
import com.github.loadup.modules.upms.infrastructure.dataobject.DepartmentDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * Department Converter
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Mapper(componentModel = "spring")
public interface DepartmentConverter {

  DepartmentConverter INSTANCE = Mappers.getMapper(DepartmentConverter.class);

  DepartmentDO toDataObject(Department department);

  Department toEntity(DepartmentDO departmentDO);
}
