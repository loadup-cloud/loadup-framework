package io.github.loadup.modules.config.domain.gateway;

/*-
 * #%L
 * Loadup Modules Config Domain
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
import java.util.List;
import java.util.Optional;

/**
 * Gateway interface for dictionary persistence.
 *
 * @author LoadUp Framework
 */
public interface DictGateway {

    /* ---- DictType ---- */

    List<DictType> findAllTypes();

    Optional<DictType> findTypeByCode(String dictCode);

    void saveType(DictType type);

    void updateType(DictType type);

    void deleteTypeByCode(String dictCode);

    boolean existsTypeByCode(String dictCode);

    /* ---- DictItem ---- */

    List<DictItem> findItemsByCode(String dictCode);

    List<DictItem> findItemsByCodeAndParent(String dictCode, String parentValue);

    void saveItem(DictItem item);

    void updateItem(DictItem item);

    void deleteItemById(String id);

    void deleteItemsByCode(String dictCode);
}
