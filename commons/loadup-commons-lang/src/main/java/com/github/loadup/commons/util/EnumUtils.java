package com.github.loadup.commons.util;

/*-
 * #%L
 * loadup-commons-lang
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

import java.util.Arrays;

import com.github.loadup.commons.enums.IEnum;

/**
 * @author Lise
 */
public class EnumUtils {
  /** getEnumByCode */
  public static <T extends Enum<T> & IEnum> T getEnumByCode(Class<T> enumClass, String code) {
    return Arrays.stream(enumClass.getEnumConstants())
        .filter(enumItem -> enumItem.getCode().equals(code))
        .findFirst()
        .orElse(null);
  }
}
