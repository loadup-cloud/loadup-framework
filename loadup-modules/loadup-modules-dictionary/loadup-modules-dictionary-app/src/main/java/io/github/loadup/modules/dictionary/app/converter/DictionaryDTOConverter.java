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
package io.github.loadup.modules.dictionary.app.converter;

import io.github.loadup.commons.mapping.LoadUpMapStructConfig;
import io.github.loadup.modules.dictionary.client.dto.DictionaryItemDTO;
import io.github.loadup.modules.dictionary.client.dto.DictionaryPageDTO;
import io.github.loadup.modules.dictionary.client.dto.DictionaryTypeDTO;
import io.github.loadup.modules.dictionary.domain.model.DictionaryItem;
import io.github.loadup.modules.dictionary.domain.model.DictionaryPage;
import io.github.loadup.modules.dictionary.domain.model.DictionaryType;
import java.util.List;
import org.mapstruct.Mapper;

/** Maps application results without exposing persistence or domain models. */
@Mapper(config = LoadUpMapStructConfig.class)
public interface DictionaryDTOConverter {
    DictionaryTypeDTO toDTO(DictionaryType value);

    DictionaryItemDTO toDTO(DictionaryItem value);

    List<DictionaryItemDTO> toItemDTOs(List<DictionaryItem> values);

    DictionaryPageDTO<DictionaryTypeDTO> toTypePageDTO(DictionaryPage<DictionaryType> value);

    DictionaryPageDTO<DictionaryItemDTO> toItemPageDTO(DictionaryPage<DictionaryItem> value);
}
