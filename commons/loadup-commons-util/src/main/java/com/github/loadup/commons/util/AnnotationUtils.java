package com.github.loadup.commons.util;

/*-
 * #%L
 * loadup-commons-util
 * %%
 * Copyright (C) 2022 - 2025 loadup_cloud
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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Annotation Utils
 */
public class AnnotationUtils {

    /**
     * Get Specific Methods by Annotations
     *
     * @param clazz
     * @param annotationClass
     * @return
     */
    public static <T extends Annotation> List<Method> findMethods(Class<?> clazz, Class<T> annotationClass) {
        if (clazz == null || annotationClass == null) {
            throw new NullPointerException();
        }
        List<Method> result = new ArrayList<Method>();
        for (Method method : clazz.getMethods()) {
            if (method.getAnnotation(annotationClass) != null) {
                result.add(method);
            }
        }
        return result;
    }

    /**
     * Get Specific Fields by Annotations
     *
     * @param clazz
     * @param annotationClass
     * @return
     */
    public static <T extends Annotation> List<Field> findFields(Class<?> clazz, Class<T> annotationClass) {
        if (clazz == null || annotationClass == null) {
            throw new NullPointerException();
        }
        List<Field> result = new ArrayList<Field>();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getAnnotation(annotationClass) != null) {
                result.add(field);
            }
        }
        return result;
    }
}
