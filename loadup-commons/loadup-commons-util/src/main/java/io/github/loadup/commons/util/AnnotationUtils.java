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
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 *
 *
 * <ul>
 * </ul>
 *
 * @author loadup_cloud
 * @since 1.0.0
 */
public class AnnotationUtils {

    public static <T extends Annotation> List<Method> findMethods(Class<?> clazz, Class<T> annotationClass) {
        if (clazz == null || annotationClass == null) {
            throw new IllegalArgumentException("Class and annotation class cannot be null");
        }

        List<Method> result = new ArrayList<>();
        for (Method method : clazz.getMethods()) {
            if (method.getAnnotation(annotationClass) != null) {
                result.add(method);
            }
        }

        LogUtil.debug(
                AnnotationUtils.class,
                "Found {} methods with annotation {} in class {}",
                result.size(),
                annotationClass.getSimpleName(),
                clazz.getSimpleName());
        return result;
    }

    public static <T extends Annotation> List<Field> findFields(Class<?> clazz, Class<T> annotationClass) {
        if (clazz == null || annotationClass == null) {
            throw new IllegalArgumentException("Class and annotation class cannot be null");
        }

        List<Field> result = new ArrayList<>();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getAnnotation(annotationClass) != null) {
                result.add(field);
            }
        }

        LogUtil.debug(
                AnnotationUtils.class,
                "Found {} fields with annotation {} in class {}",
                result.size(),
                annotationClass.getSimpleName(),
                clazz.getSimpleName());
        return result;
    }

    public static <T extends Annotation> boolean hasAnnotation(Class<?> clazz, Class<T> annotationClass) {
        if (clazz == null || annotationClass == null) {
            return false;
        }
        return clazz.getAnnotation(annotationClass) != null;
    }

    public static <T extends Annotation> boolean hasAnnotation(Method method, Class<T> annotationClass) {
        if (method == null || annotationClass == null) {
            return false;
        }
        return method.getAnnotation(annotationClass) != null;
    }

    public static <T extends Annotation> boolean hasAnnotation(Field field, Class<T> annotationClass) {
        if (field == null || annotationClass == null) {
            return false;
        }
        return field.getAnnotation(annotationClass) != null;
    }

    public static <T extends Annotation> T getAnnotation(Class<?> clazz, Class<T> annotationClass) {
        if (clazz == null || annotationClass == null) {
            return null;
        }
        return clazz.getAnnotation(annotationClass);
    }

    public static <T extends Annotation> T getAnnotation(Method method, Class<T> annotationClass) {
        if (method == null || annotationClass == null) {
            return null;
        }
        return method.getAnnotation(annotationClass);
    }

    public static <T extends Annotation> T getAnnotation(Field field, Class<T> annotationClass) {
        if (field == null || annotationClass == null) {
            return null;
        }
        return field.getAnnotation(annotationClass);
    }

    public static Annotation[] getAnnotations(Class<?> clazz) {
        if (clazz == null) {
            return new Annotation[0];
        }
        return clazz.getAnnotations();
    }
}
