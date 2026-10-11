/*
 * #%L
 * Loadup Common Utils
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
package io.github.loadup.commons.util;

import io.github.loadup.commons.log.LogUtil;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 *
 * <ul>
 * </ul>
 *
 * @author loadup_cloud
 * @since 1.0.0
 */
public class ValidateUtils {

    private static final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();

    public static void validate(Object obj) {
        if (obj == null) {
            throw new IllegalArgumentException("Object to validate cannot be null");
        }

        Validator validator = factory.getValidator();
        Set<ConstraintViolation<Object>> violations = validator.validate(obj);

        if (!violations.isEmpty()) {
            String errorMsg = formatViolations(violations);
            LogUtil.warn(
                    ValidateUtils.class,
                    "Validation failed for {}: {}",
                    obj.getClass().getSimpleName(),
                    errorMsg);
            throw new ValidationException(errorMsg);
        }
    }

    public static boolean isValid(Object obj) {
        if (obj == null) {
            LogUtil.debug(ValidateUtils.class, "Object to validate is null, returning false");
            return false;
        }

        try {
            Validator validator = factory.getValidator();
            Set<ConstraintViolation<Object>> violations = validator.validate(obj);
            return violations.isEmpty();
        } catch (Exception e) {
            LogUtil.error(
                    ValidateUtils.class,
                    "Validation error for {}",
                    obj.getClass().getSimpleName(),
                    e);
            return false;
        }
    }

    public static <T> Set<ConstraintViolation<T>> getViolations(T obj) {
        if (obj == null) {
            LogUtil.debug(ValidateUtils.class, "Object to validate is null, returning empty set");
            return Set.of();
        }

        Validator validator = factory.getValidator();
        return validator.validate(obj);
    }

    public static String getErrorMessage(ConstraintViolation<?> violation) {
        if (violation == null) {
            return "";
        }
        return violation.getPropertyPath() + ": " + violation.getMessage();
    }

    public static String formatViolations(Set<? extends ConstraintViolation<?>> violations) {
        if (violations == null || violations.isEmpty()) {
            return "";
        }

        return violations.stream().map(ValidateUtils::getErrorMessage).collect(Collectors.joining("; "));
    }

    public static Validator getValidator() {
        return factory.getValidator();
    }
}
