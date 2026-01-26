package io.github.loadup.commons.enums;

/*-
 * #%L
 * loadup-commons-util
 * %%
 * Copyright (C) 2022 - 2024 loadup_cloud
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

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Lise
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum GenderEnum implements IEnum {
  MALE("MALE", "Male"),
  FEMALE("FEMALE", "Female"),
  UNKNOWN("UNKNOWN", "Unknown"),
  ;

  private final String code;
  private final String description;

  public static GenderEnum getByCode(String code) {
    return IEnum.EnumLookup.fromCode(GenderEnum.class, code);
  }
}
