package com.github.loadup.commons.enums;

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

import com.github.loadup.commons.util.EnumUtils;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Lise
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum PlatFormEnum implements IEnum {
  /** X86 */
  X86("X86", "X86"),
  /** X86_64 */
  X86_64("X86_64", "X86_64"),
  /** ARM64 */
  ARM64("ARM64", "ARM64"),
  /** SPARC */
  SPARC("SPARC", "SPARC"),
  /** MIPS */
  MIPS("MIPS", "MIPS"),
  ;
  private String code;
  private String description;

  public static PlatFormEnum getByCode(String code) {
    return EnumUtils.getEnumByCode(PlatFormEnum.class, code);
  }
}
