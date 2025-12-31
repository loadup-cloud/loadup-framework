package com.github.loadup.commons.result;

/*-
 * #%L
 * loadup-commons-dto
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
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

@Getter
@AllArgsConstructor
public enum ResultStatusEnum {
  SUCCESS("S"),
  FAIL("F"),
  UNKNOWN("U");

  private String code;

  public static ResultStatusEnum getByCode(String code) {
    return Arrays.stream(ResultStatusEnum.values())
        .filter(resultStatusEnum -> StringUtils.equals(resultStatusEnum.getCode(), code))
        .findFirst()
        .orElse(null);
  }
}
