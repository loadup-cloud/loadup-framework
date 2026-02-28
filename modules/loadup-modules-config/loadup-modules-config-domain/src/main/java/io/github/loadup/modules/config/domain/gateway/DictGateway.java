package io.github.loadup.modules.config.domain.gateway;

/*-
 * #%L
 * Loadup Modules Config Domain
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
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
