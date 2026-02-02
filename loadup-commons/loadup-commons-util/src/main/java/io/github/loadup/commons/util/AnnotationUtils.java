package io.github.loadup.commons.util;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 注解工具类
 *
 * <p>提供注解查找、处理和扫描功能：
 *
 * <ul>
 *   <li>查找带指定注解的方法
 *   <li>查找带指定注解的字段
 *   <li>检查类、方法、字段是否有指定注解
 *   <li>获取注解实例
 * </ul>
 *
 * @author loadup_cloud
 * @since 1.0.0
 */
public class AnnotationUtils {

  private static final Logger log = LoggerFactory.getLogger(AnnotationUtils.class);

  /**
   * 查找类中所有带指定注解的方法
   *
   * @param clazz 目标类
   * @param annotationClass 注解类型
   * @param <T> 注解类型
   * @return 带指定注解的方法列表
   * @throws IllegalArgumentException 如果参数为null
   */
  public static <T extends Annotation> List<Method> findMethods(
      Class<?> clazz, Class<T> annotationClass) {
    if (clazz == null || annotationClass == null) {
      throw new IllegalArgumentException("Class and annotation class cannot be null");
    }

    List<Method> result = new ArrayList<>();
    for (Method method : clazz.getMethods()) {
      if (method.getAnnotation(annotationClass) != null) {
        result.add(method);
      }
    }

    log.debug(
        "Found {} methods with annotation {} in class {}",
        result.size(),
        annotationClass.getSimpleName(),
        clazz.getSimpleName());
    return result;
  }

  /**
   * 查找类中所有带指定注解的字段
   *
   * @param clazz 目标类
   * @param annotationClass 注解类型
   * @param <T> 注解类型
   * @return 带指定注解的字段列表
   * @throws IllegalArgumentException 如果参数为null
   */
  public static <T extends Annotation> List<Field> findFields(
      Class<?> clazz, Class<T> annotationClass) {
    if (clazz == null || annotationClass == null) {
      throw new IllegalArgumentException("Class and annotation class cannot be null");
    }

    List<Field> result = new ArrayList<>();
    for (Field field : clazz.getDeclaredFields()) {
      if (field.getAnnotation(annotationClass) != null) {
        result.add(field);
      }
    }

    log.debug(
        "Found {} fields with annotation {} in class {}",
        result.size(),
        annotationClass.getSimpleName(),
        clazz.getSimpleName());
    return result;
  }

  /**
   * 检查类是否有指定注解
   *
   * @param clazz 目标类
   * @param annotationClass 注解类型
   * @param <T> 注解类型
   * @return true-有指定注解，false-没有
   */
  public static <T extends Annotation> boolean hasAnnotation(
      Class<?> clazz, Class<T> annotationClass) {
    if (clazz == null || annotationClass == null) {
      return false;
    }
    return clazz.getAnnotation(annotationClass) != null;
  }

  /**
   * 检查方法是否有指定注解
   *
   * @param method 目标方法
   * @param annotationClass 注解类型
   * @param <T> 注解类型
   * @return true-有指定注解，false-没有
   */
  public static <T extends Annotation> boolean hasAnnotation(
      Method method, Class<T> annotationClass) {
    if (method == null || annotationClass == null) {
      return false;
    }
    return method.getAnnotation(annotationClass) != null;
  }

  /**
   * 检查字段是否有指定注解
   *
   * @param field 目标字段
   * @param annotationClass 注解类型
   * @param <T> 注解类型
   * @return true-有指定注解，false-没有
   */
  public static <T extends Annotation> boolean hasAnnotation(
      Field field, Class<T> annotationClass) {
    if (field == null || annotationClass == null) {
      return false;
    }
    return field.getAnnotation(annotationClass) != null;
  }

  /**
   * 获取类上的注解实例
   *
   * @param clazz 目标类
   * @param annotationClass 注解类型
   * @param <T> 注解类型
   * @return 注解实例，不存在返回null
   */
  public static <T extends Annotation> T getAnnotation(Class<?> clazz, Class<T> annotationClass) {
    if (clazz == null || annotationClass == null) {
      return null;
    }
    return clazz.getAnnotation(annotationClass);
  }

  /**
   * 获取方法上的注解实例
   *
   * @param method 目标方法
   * @param annotationClass 注解类型
   * @param <T> 注解类型
   * @return 注解实例，不存在返回null
   */
  public static <T extends Annotation> T getAnnotation(Method method, Class<T> annotationClass) {
    if (method == null || annotationClass == null) {
      return null;
    }
    return method.getAnnotation(annotationClass);
  }

  /**
   * 获取字段上的注解实例
   *
   * @param field 目标字段
   * @param annotationClass 注解类型
   * @param <T> 注解类型
   * @return 注解实例，不存在返回null
   */
  public static <T extends Annotation> T getAnnotation(Field field, Class<T> annotationClass) {
    if (field == null || annotationClass == null) {
      return null;
    }
    return field.getAnnotation(annotationClass);
  }

  /**
   * 获取类上的所有注解
   *
   * @param clazz 目标类
   * @return 注解数组
   */
  public static Annotation[] getAnnotations(Class<?> clazz) {
    if (clazz == null) {
      return new Annotation[0];
    }
    return clazz.getAnnotations();
  }
}
