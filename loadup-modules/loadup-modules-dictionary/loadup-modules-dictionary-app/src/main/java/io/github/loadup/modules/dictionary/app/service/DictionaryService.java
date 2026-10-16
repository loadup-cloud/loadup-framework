/*
 * #%L
 * LoadUp Dictionary App
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
package io.github.loadup.modules.dictionary.app.service;

import io.github.loadup.commons.enums.CommonResultCodeEnum;
import io.github.loadup.commons.error.CommonException;
import io.github.loadup.commons.result.PageDTO;
import io.github.loadup.modules.dictionary.app.converter.DictionaryDTOConverter;
import io.github.loadup.modules.dictionary.client.command.DictionaryItemCreateCommand;
import io.github.loadup.modules.dictionary.client.command.DictionaryItemUpdateCommand;
import io.github.loadup.modules.dictionary.client.command.DictionaryTypeCreateCommand;
import io.github.loadup.modules.dictionary.client.command.DictionaryTypeUpdateCommand;
import io.github.loadup.modules.dictionary.client.dto.DictionaryItemDTO;
import io.github.loadup.modules.dictionary.client.dto.DictionaryTypeDTO;
import io.github.loadup.modules.dictionary.client.error.DictionaryResultCodeEnum;
import io.github.loadup.modules.dictionary.domain.gateway.DictionaryGateway;
import io.github.loadup.modules.dictionary.domain.model.DictionaryItem;
import io.github.loadup.modules.dictionary.domain.model.DictionaryType;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;

/** Dictionary administration and effective-value queries. */
public class DictionaryService implements io.github.loadup.modules.dictionary.client.facade.DictionaryFacade {
    private final Clock clock;
    private static final String DEFAULT_TENANT = "__default__";
    private final DictionaryDTOConverter converter;
    private final DictionaryGateway repository;

    public DictionaryService(
            DictionaryGateway repository, DictionaryDTOConverter converter, @Qualifier("dictionaryClock") Clock clock) {
        this.clock = clock;
        this.converter = converter;
        this.repository = repository;
    }

    @Transactional
    public DictionaryTypeDTO createType(String tenantId, DictionaryTypeCreateCommand command) {
        if (command == null) throw new CommonException(DictionaryResultCodeEnum.TYPE_IS_REQUIRED);
        String tenant = tenant(tenantId);
        String code = code(command.code());
        String name = required(command.name(), 100);
        if (repository.findTypeByCode(tenant, code).isPresent()) {
            throw new CommonException(DictionaryResultCodeEnum.DICTIONARY_TYPE_CODE_ALREADY_EXISTS);
        }
        LocalDateTime now = LocalDateTime.ofInstant(clock.instant(), clock.getZone());
        DictionaryType type = new DictionaryType(
                UUID.randomUUID().toString(),
                tenant,
                code,
                name,
                optional(command.description(), 500),
                command.enabled() == null || command.enabled(),
                now,
                now);
        try {
            repository.insertType(type);
        } catch (DuplicateKeyException duplicate) {
            throw new CommonException(CommonResultCodeEnum.SYS_ERROR, duplicate);
        }
        return converter.toDTO(type);
    }

    @Transactional
    public DictionaryTypeDTO updateType(String tenantId, DictionaryTypeUpdateCommand command) {
        if (command == null || command.enabled() == null) {
            throw new CommonException(DictionaryResultCodeEnum.TYPE_AND_ENABLED_ARE_REQUIRED);
        }
        String tenant = tenant(tenantId);
        DictionaryType previous = requireType(tenant, command.id());
        DictionaryType updated = new DictionaryType(
                previous.id(),
                tenant,
                previous.code(),
                required(command.name(), 100),
                optional(command.description(), 500),
                command.enabled(),
                previous.createdAt(),
                LocalDateTime.ofInstant(clock.instant(), clock.getZone()));
        repository.updateType(updated);
        return converter.toDTO(updated);
    }

    @Transactional
    public void deleteType(String tenantId, String id) {
        String tenant = tenant(tenantId);
        DictionaryType type = requireType(tenant, id);
        if (repository.countItems(tenant, type.id()) != 0) {
            throw new CommonException(DictionaryResultCodeEnum.DICTIONARY_TYPE_NOT_EMPTY);
        }
        repository.deleteType(tenant, type.id());
    }

    public PageDTO<DictionaryTypeDTO> listTypes(String tenantId, int page, int size) {
        validatePage(page, size);
        return converter.toTypePageDTO(repository.listTypes(tenant(tenantId), page, size));
    }

    @Transactional
    public DictionaryItemDTO createItem(String tenantId, DictionaryItemCreateCommand command) {
        if (command == null) throw new CommonException(DictionaryResultCodeEnum.ITEM_IS_REQUIRED);
        String tenant = tenant(tenantId);
        DictionaryType type = requireTypeByCode(tenant, command.typeCode());
        LocalDateTime now = LocalDateTime.ofInstant(clock.instant(), clock.getZone());
        DictionaryItem item = new DictionaryItem(
                UUID.randomUUID().toString(),
                tenant,
                type.id(),
                required(command.value(), 128),
                required(command.label(), 200),
                optional(command.description(), 500),
                command.sortOrder() == null ? 0 : command.sortOrder(),
                command.enabled() == null || command.enabled(),
                now,
                now);
        try {
            repository.insertItem(item);
        } catch (DuplicateKeyException duplicate) {
            throw new CommonException(CommonResultCodeEnum.SYS_ERROR, duplicate);
        }
        return converter.toDTO(item);
    }

    @Transactional
    public DictionaryItemDTO updateItem(String tenantId, DictionaryItemUpdateCommand command) {
        if (command == null || command.enabled() == null || command.sortOrder() == null) {
            throw new CommonException(DictionaryResultCodeEnum.ITEM_ENABLED_AND_SORTORDER_ARE_REQUIRED);
        }
        String tenant = tenant(tenantId);
        DictionaryItem previous = requireItem(tenant, command.id());
        DictionaryItem updated = new DictionaryItem(
                previous.id(),
                tenant,
                previous.typeId(),
                previous.value(),
                required(command.label(), 200),
                optional(command.description(), 500),
                command.sortOrder(),
                command.enabled(),
                previous.createdAt(),
                LocalDateTime.ofInstant(clock.instant(), clock.getZone()));
        repository.updateItem(updated);
        return converter.toDTO(updated);
    }

    @Transactional
    public void deleteItem(String tenantId, String id) {
        String tenant = tenant(tenantId);
        repository.deleteItem(tenant, requireItem(tenant, id).id());
    }

    public PageDTO<DictionaryItemDTO> listItems(String tenantId, String typeCode, int page, int size) {
        validatePage(page, size);
        String tenant = tenant(tenantId);
        return converter.toItemPageDTO(
                repository.listItems(tenant, requireTypeByCode(tenant, typeCode).id(), page, size));
    }

    public List<DictionaryItemDTO> listEnabledItems(String tenantId, String typeCode) {
        String tenant = tenant(tenantId);
        DictionaryType type = requireTypeByCode(tenant, typeCode);
        return type.enabled() ? converter.toItemDTOs(repository.listEnabledItems(tenant, type.id())) : List.of();
    }

    private DictionaryType requireType(String tenant, String id) {
        return repository
                .findTypeById(tenant, required(id, 64))
                .orElseThrow(() -> new CommonException(DictionaryResultCodeEnum.DICTIONARY_TYPE_NOT_FOUND));
    }

    private DictionaryType requireTypeByCode(String tenant, String typeCode) {
        return repository
                .findTypeByCode(tenant, code(typeCode))
                .orElseThrow(() -> new CommonException(DictionaryResultCodeEnum.DICTIONARY_TYPE_NOT_FOUND));
    }

    private DictionaryItem requireItem(String tenant, String id) {
        return repository
                .findItemById(tenant, required(id, 64))
                .orElseThrow(() -> new CommonException(DictionaryResultCodeEnum.DICTIONARY_ITEM_NOT_FOUND));
    }

    private static String tenant(String tenantId) {
        return tenantId == null || tenantId.isBlank() ? DEFAULT_TENANT : required(tenantId, 64);
    }

    private static String code(String code) {
        String normalized = required(code, 64).toLowerCase(Locale.ROOT);
        if (!normalized.matches("[a-z][a-z0-9_.-]*")) {
            throw new CommonException(DictionaryResultCodeEnum.INVALID_DICTIONARY_CODE);
        }
        return normalized;
    }

    private static String required(String value, int maxLength) {
        if (value == null || value.isBlank() || value.trim().length() > maxLength) {
            throw new CommonException(CommonResultCodeEnum.PARAM_ILLEGAL);
        }
        return value.trim();
    }

    private static String optional(String value, int maxLength) {
        if (value == null || value.isBlank()) return null;
        if (value.trim().length() > maxLength) {
            throw new CommonException(DictionaryResultCodeEnum.DESCRIPTION_MUST_BE_AT_MOST);
        }
        return value.trim();
    }

    private static void validatePage(int page, int size) {
        io.github.loadup.commons.dto.PageQuery.normalized(page, size, DictionaryResultCodeEnum.INVALID_PAGE);
    }
}
