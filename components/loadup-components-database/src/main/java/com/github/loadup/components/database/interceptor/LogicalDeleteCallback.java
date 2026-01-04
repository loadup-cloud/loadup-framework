package com.github.loadup.components.database.interceptor;

/*-
 * #%L
 * loadup-components-database
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

import com.github.loadup.commons.dataobject.BaseDO;
import com.github.loadup.components.database.config.DatabaseProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.relational.core.conversion.MutableAggregateChange;
import org.springframework.data.relational.core.mapping.event.BeforeSaveCallback;
import org.springframework.stereotype.Component;

/**
 * Logical Delete Callback
 *
 * <p>Automatically handles logical delete field initialization when enabled. When
 * loadup.database.logical-delete.enabled=true, this callback ensures that all entities extending
 * BaseDO have the deleted field properly initialized.
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LogicalDeleteCallback implements BeforeSaveCallback<BaseDO> {

  private final DatabaseProperties databaseProperties;

  @Override
  public BaseDO onBeforeSave(BaseDO entity, MutableAggregateChange<BaseDO> aggregateChange) {
    if (!databaseProperties.getLogicalDelete().isEnabled()) {
      return entity;
    }

    // Initialize deleted field if null
    if (entity.getDeleted() == null) {
      entity.setDeleted(databaseProperties.getLogicalDelete().getNotDeletedValue());
      log.trace(
          "Initialized logical delete field for entity: {} with value: {}",
          entity.getClass().getSimpleName(),
          entity.getDeleted());
    }

    return entity;
  }
}
