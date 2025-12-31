package com.github.loadup.commons.util;

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

import java.util.Set;

import jakarta.validation.*;

// @Slf4j
public class ValidateUtils {
  private static final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();

  public static void validate(Object obj) {
    Validator validator = factory.getValidator();
    Set<ConstraintViolation<Object>> constraintViolations = validator.validate(obj);
    if (!constraintViolations.isEmpty()) {
      throw new IllegalArgumentException("Validation failed: " + constraintViolations);
    }
  }

  public static boolean isValid(Object obj) {
    try {
      validate(obj);
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    } catch (RuntimeException e) {
      return false;
    }
  }

  public static String getErrorMessage(ConstraintViolation<?> cv) {
    return cv.getMessage();
  }
}
