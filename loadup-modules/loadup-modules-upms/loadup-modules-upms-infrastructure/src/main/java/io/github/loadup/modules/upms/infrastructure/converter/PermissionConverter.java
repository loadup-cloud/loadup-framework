package io.github.loadup.modules.upms.infrastructure.converter;

/*-
 * #%L
 * loadup-modules-upms-infrastructure
 * %%
 * Copyright (C) 2022 - 2026 loadup_cloud
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

import io.github.loadup.modules.upms.domain.entity.Permission;
import io.github.loadup.modules.upms.infrastructure.dataobject.PermissionDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * Permission Converter
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Mapper(
        componentModel = "spring",
        uses = AuditMappingSupport.class,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        unmappedSourcePolicy = ReportingPolicy.WARN)
public interface PermissionConverter {
    @Mapping(source = "createdTime", target = "createdAt")
    @Mapping(source = "updatedTime", target = "updatedAt")
    @Mapping(target = "tenantId", ignore = true)
    PermissionDO toDataObject(Permission permission);

    @Mapping(source = "createdAt", target = "createdTime")
    @Mapping(source = "updatedAt", target = "updatedTime")
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "children", ignore = true)
    Permission toEntity(PermissionDO permissionDO);
}
