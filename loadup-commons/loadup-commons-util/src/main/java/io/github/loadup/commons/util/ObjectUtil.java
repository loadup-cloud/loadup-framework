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
import java.util.Objects;
import java.util.function.Supplier;

/**
 *
 *
 * <ul>
 * </ul>
 *
 * @author loadup_cloud
 * @since 1.0.0
 */
public class ObjectUtil {

    public static boolean isNull(Object obj) {
        return Objects.isNull(obj);
    }

    public static boolean isNotNull(Object obj) {
        return !Objects.isNull(obj);
    }

    public static <T> T defaultIfNull(T obj, T defaultValue) {
        return obj != null ? obj : defaultValue;
    }

    public static <T> T defaultIfNull(T obj, Supplier<T> defaultSupplier) {
        return obj != null ? obj : defaultSupplier != null ? defaultSupplier.get() : null;
    }

    public static boolean equals(Object obj1, Object obj2) {
        return Objects.equals(obj1, obj2);
    }

    public static int hashCode(Object obj) {
        return Objects.hashCode(obj);
    }

    public static String toString(Object obj) {
        return Objects.toString(obj);
    }

    public static String toString(Object obj, String nullDefault) {
        return Objects.toString(obj, nullDefault);
    }

    public static <T> T requireNonNull(T obj) {
        return Objects.requireNonNull(obj);
    }

    public static <T> T requireNonNull(T obj, String message) {
        return Objects.requireNonNull(obj, message);
    }

    public static boolean isEmpty(Object obj) {
        if (obj == null) {
            return true;
        }
        if (obj instanceof String) {
            return ((String) obj).trim().isEmpty();
        }
        return false;
    }

    public static boolean isNotEmpty(Object obj) {
        return !isEmpty(obj);
    }

    @SuppressWarnings("unchecked")
    public static <T> T cast(Object obj, Class<T> targetClass) {
        if (obj == null || targetClass == null) {
            return null;
        }
        if (targetClass.isInstance(obj)) {
            return (T) obj;
        }
        LogUtil.warn(ObjectUtil.class, "Cannot cast {} to {}", obj.getClass().getName(), targetClass.getName());
        return null;
    }
}
