/*
 * #%L
 * LoadUp Dictionary Client
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
package io.github.loadup.modules.dictionary.client.facade;

import io.github.loadup.modules.dictionary.client.command.DictionaryItemCreateCommand;
import io.github.loadup.modules.dictionary.client.command.DictionaryItemUpdateCommand;
import io.github.loadup.modules.dictionary.client.command.DictionaryTypeCreateCommand;
import io.github.loadup.modules.dictionary.client.command.DictionaryTypeUpdateCommand;
import io.github.loadup.modules.dictionary.client.dto.DictionaryItemDTO;
import io.github.loadup.modules.dictionary.client.dto.DictionaryPageDTO;
import io.github.loadup.modules.dictionary.client.dto.DictionaryTypeDTO;
import java.util.List;

/** Public business contract for DictionaryService. */
public interface DictionaryFacade {
    DictionaryTypeDTO createType(String tenantId, DictionaryTypeCreateCommand command);

    DictionaryTypeDTO updateType(String tenantId, String id, DictionaryTypeUpdateCommand command);

    void deleteType(String tenantId, String id);

    DictionaryPageDTO<DictionaryTypeDTO> listTypes(String tenantId, int page, int size);

    DictionaryItemDTO createItem(String tenantId, String typeCode, DictionaryItemCreateCommand command);

    DictionaryItemDTO updateItem(String tenantId, String id, DictionaryItemUpdateCommand command);

    void deleteItem(String tenantId, String id);

    DictionaryPageDTO<DictionaryItemDTO> listItems(String tenantId, String typeCode, int page, int size);

    List<DictionaryItemDTO> listEnabledItems(String tenantId, String typeCode);
}
