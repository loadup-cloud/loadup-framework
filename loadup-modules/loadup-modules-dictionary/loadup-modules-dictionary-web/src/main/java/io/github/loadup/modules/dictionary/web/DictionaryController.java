/*
 * #%L
 * LoadUp Data Dictionary Web Adapter
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
package io.github.loadup.modules.dictionary.web;

import io.github.loadup.commons.result.PageDTO;
import io.github.loadup.commons.result.PageResponse;
import io.github.loadup.commons.result.SuccessResponse;
import io.github.loadup.commons.util.TenantUtil;
import io.github.loadup.modules.dictionary.client.command.DictionaryDeleteCommand;
import io.github.loadup.modules.dictionary.client.command.DictionaryItemCreateCommand;
import io.github.loadup.modules.dictionary.client.command.DictionaryItemUpdateCommand;
import io.github.loadup.modules.dictionary.client.command.DictionaryTypeCreateCommand;
import io.github.loadup.modules.dictionary.client.command.DictionaryTypeUpdateCommand;
import io.github.loadup.modules.dictionary.client.dto.DictionaryItemDTO;
import io.github.loadup.modules.dictionary.client.dto.DictionaryOptionDTO;
import io.github.loadup.modules.dictionary.client.dto.DictionaryTypeDTO;
import io.github.loadup.modules.dictionary.client.facade.DictionaryFacade;
import io.github.loadup.modules.dictionary.client.query.DictionaryItemPageQuery;
import io.github.loadup.modules.dictionary.client.query.DictionaryTypeCodeQuery;
import io.github.loadup.modules.dictionary.client.query.DictionaryTypePageQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** HTTP administration and option lookup for business dictionaries. */
@RestController
@RequestMapping("/dictionaries")
@Tag(name = "Data Dictionaries", description = "Tenant-scoped business dictionary management")
public class DictionaryController {
    private final DictionaryWebConverter converter;
    private final DictionaryFacade service;

    public DictionaryController(DictionaryFacade service, DictionaryWebConverter converter) {
        this.converter = converter;
        this.service = service;
    }

    @PostMapping("/types/create")
    @Operation(summary = "Create a dictionary type")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public SuccessResponse<DictionaryTypeDTO> createType(@RequestBody DictionaryTypeCreateCommand command) {
        return SuccessResponse.of(service.createType(TenantUtil.getTenantId(), command));
    }

    @PostMapping("/types/update")
    @Operation(summary = "Update a dictionary type or its enabled state")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public SuccessResponse<DictionaryTypeDTO> updateType(@RequestBody DictionaryTypeUpdateCommand request) {
        return SuccessResponse.of(service.updateType(TenantUtil.getTenantId(), request));
    }

    @PostMapping("/types/delete")
    @Operation(summary = "Delete an empty dictionary type")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public SuccessResponse<Void> deleteType(@RequestBody DictionaryDeleteCommand request) {
        service.deleteType(TenantUtil.getTenantId(), request.id());
        return SuccessResponse.success();
    }

    @PostMapping("/types/list")
    @Operation(summary = "List dictionary types, including disabled types")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public PageResponse<DictionaryTypeDTO> listTypes(@RequestBody DictionaryTypePageQuery request) {
        PageDTO<DictionaryTypeDTO> result = service.listTypes(
                TenantUtil.getTenantId(),
                request.page() == null ? 1 : request.page(),
                request.size() == null ? 20 : request.size());
        return PageResponse.of(result);
    }

    @PostMapping("/items/create")
    @Operation(summary = "Create a dictionary item")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public SuccessResponse<DictionaryItemDTO> createItem(@RequestBody DictionaryItemCreateCommand request) {
        return SuccessResponse.of(service.createItem(TenantUtil.getTenantId(), request));
    }

    @PostMapping("/items/update")
    @Operation(summary = "Update a dictionary item or its enabled state")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public SuccessResponse<DictionaryItemDTO> updateItem(@RequestBody DictionaryItemUpdateCommand request) {
        return SuccessResponse.of(service.updateItem(TenantUtil.getTenantId(), request));
    }

    @PostMapping("/items/delete")
    @Operation(summary = "Delete a dictionary item")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public SuccessResponse<Void> deleteItem(@RequestBody DictionaryDeleteCommand request) {
        service.deleteItem(TenantUtil.getTenantId(), request.id());
        return SuccessResponse.success();
    }

    @PostMapping("/items/list")
    @Operation(summary = "List dictionary items, including disabled items")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public PageResponse<DictionaryItemDTO> listItems(@RequestBody DictionaryItemPageQuery request) {
        PageDTO<DictionaryItemDTO> result = service.listItems(
                TenantUtil.getTenantId(),
                request.typeCode(),
                request.page() == null ? 1 : request.page(),
                request.size() == null ? 20 : request.size());
        return PageResponse.of(result);
    }

    @PostMapping("/options")
    @Operation(summary = "Get enabled options for an enabled dictionary type")
    public SuccessResponse<List<DictionaryOptionDTO>> options(@RequestBody DictionaryTypeCodeQuery request) {
        return SuccessResponse.of(
                converter.toOptions(service.listEnabledItems(TenantUtil.getTenantId(), request.typeCode())));
    }
}
