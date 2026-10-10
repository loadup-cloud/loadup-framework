/*
 * #%L
 * LoadUp Dictionary Domain
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
package io.github.loadup.modules.dictionary.domain.gateway;

import io.github.loadup.modules.dictionary.domain.model.DictionaryItem;
import io.github.loadup.modules.dictionary.domain.model.DictionaryPage;
import io.github.loadup.modules.dictionary.domain.model.DictionaryType;
import java.util.List;
import java.util.Optional;

/** Persistence contract; tenant scope is mandatory on every operation. */
public interface DictionaryGateway {
    Optional<DictionaryType> findTypeById(String tenantId, String id);

    Optional<DictionaryType> findTypeByCode(String tenantId, String code);

    void insertType(DictionaryType type);

    void updateType(DictionaryType type);

    void deleteType(String tenantId, String id);

    DictionaryPage<DictionaryType> listTypes(String tenantId, int page, int size);

    Optional<DictionaryItem> findItemById(String tenantId, String id);

    void insertItem(DictionaryItem item);

    void updateItem(DictionaryItem item);

    void deleteItem(String tenantId, String id);

    long countItems(String tenantId, String typeId);

    DictionaryPage<DictionaryItem> listItems(String tenantId, String typeId, int page, int size);

    List<DictionaryItem> listEnabledItems(String tenantId, String typeId);
}
