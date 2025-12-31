package com.github.loadup.components.database.id;

/*-
 * #%L
 * loadup-components-database
 * %%
 * Copyright (C) 2025 LoadUp Cloud
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

/** ID Generator Interface */
@FunctionalInterface
public interface IdGenerator {

  /**
   * Generate unique ID
   *
   * @return unique identifier
   */
  String generate();

  /**
   * Get generator name
   *
   * @return generator name
   */
  default String getName() {
    return this.getClass().getSimpleName();
  }
}
