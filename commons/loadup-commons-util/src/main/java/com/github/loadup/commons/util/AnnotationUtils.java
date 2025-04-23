package com.github.loadup.commons.util;


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
    public static <T extends Annotation> List<Method> findMethods(Class<?> clazz,
                                                                  Class<T> annotationClass) {
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
    public static <T extends Annotation> List<Field> findFields(Class<?> clazz,
                                                                Class<T> annotationClass) {
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
