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

/*
@author Lise
 * @since 1.0.0
 */

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class ToStringUtils {
  static {
    ToStringBuilder.setDefaultStyle(ToStringStyle.JSON_STYLE);
  }

  public static String reflectionToString(Object object) {
    return ToStringBuilder.reflectionToString(object);
  }

  public static String reflectionToString(Object object, ToStringStyle style) {
    return ToStringBuilder.reflectionToString(object, style);
  }
}
