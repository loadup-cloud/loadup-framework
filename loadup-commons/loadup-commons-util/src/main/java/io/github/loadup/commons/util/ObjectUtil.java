package io.github.loadup.commons.util;

/*-
 * #%L
 * loadup-commons-util
 * %%
 * Copyright (C) 2026 LoadUp Cloud
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

import java.util.Objects;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 对象工具类
 *
 * <p>提供常用的对象操作方法：
 *
 * <ul>
 *   <li>空值检查
 *   <li>对象比较
 *   <li>默认值处理
 *   <li>字符串转换
 *   <li>类型转换
 * </ul>
 *
 * @author loadup_cloud
 * @since 1.0.0
 */
public class ObjectUtil {

    private static final Logger log = LoggerFactory.getLogger(ObjectUtil.class);

    /**
     * 检查对象是否为null
     *
     * @param obj 对象
     * @return true-为null，false-不为null
     */
    public static boolean isNull(Object obj) {
        return Objects.isNull(obj);
    }

    /**
     * 检查对象是否不为null
     *
     * @param obj 对象
     * @return true-不为null，false-为null
     */
    public static boolean isNotNull(Object obj) {
        return !Objects.isNull(obj);
    }

    /**
     * 如果对象为null，返回默认值
     *
     * @param obj 对象
     * @param defaultValue 默认值
     * @param <T> 对象类型
     * @return 对象本身或默认值
     */
    public static <T> T defaultIfNull(T obj, T defaultValue) {
        return obj != null ? obj : defaultValue;
    }

    /**
     * 如果对象为null，通过Supplier获取默认值
     *
     * @param obj 对象
     * @param defaultSupplier 默认值提供者
     * @param <T> 对象类型
     * @return 对象本身或默认值
     */
    public static <T> T defaultIfNull(T obj, Supplier<T> defaultSupplier) {
        return obj != null ? obj : (defaultSupplier != null ? defaultSupplier.get() : null);
    }

    /**
     * 比较两个对象是否相等（null安全）
     *
     * @param obj1 对象1
     * @param obj2 对象2
     * @return true-相等，false-不相等
     */
    public static boolean equals(Object obj1, Object obj2) {
        return Objects.equals(obj1, obj2);
    }

    /**
     * 获取对象的哈希码（null安全）
     *
     * @param obj 对象
     * @return 哈希码，对象为null时返回0
     */
    public static int hashCode(Object obj) {
        return Objects.hashCode(obj);
    }

    /**
     * 将对象转换为字符串（null安全）
     *
     * @param obj 对象
     * @return 字符串表示，对象为null时返回"null"
     */
    public static String toString(Object obj) {
        return Objects.toString(obj);
    }

    /**
     * 将对象转换为字符串，null时返回默认值
     *
     * @param obj 对象
     * @param nullDefault null时的默认值
     * @return 字符串表示
     */
    public static String toString(Object obj, String nullDefault) {
        return Objects.toString(obj, nullDefault);
    }

    /**
     * 要求对象不为null，否则抛出异常
     *
     * @param obj 对象
     * @param <T> 对象类型
     * @return 非null对象
     * @throws NullPointerException 如果对象为null
     */
    public static <T> T requireNonNull(T obj) {
        return Objects.requireNonNull(obj);
    }

    /**
     * 要求对象不为null，否则抛出带消息的异常
     *
     * @param obj 对象
     * @param message 异常消息
     * @param <T> 对象类型
     * @return 非null对象
     * @throws NullPointerException 如果对象为null
     */
    public static <T> T requireNonNull(T obj, String message) {
        return Objects.requireNonNull(obj, message);
    }

    /**
     * 检查对象是否为空（null或空字符串）
     *
     * @param obj 对象
     * @return true-为空，false-不为空
     */
    public static boolean isEmpty(Object obj) {
        if (obj == null) {
            return true;
        }
        if (obj instanceof String) {
            return ((String) obj).trim().isEmpty();
        }
        return false;
    }

    /**
     * 检查对象是否不为空
     *
     * @param obj 对象
     * @return true-不为空，false-为空
     */
    public static boolean isNotEmpty(Object obj) {
        return !isEmpty(obj);
    }

    /**
     * 安全地转换对象类型
     *
     * @param obj 对象
     * @param targetClass 目标类型
     * @param <T> 目标类型
     * @return 转换后的对象，失败返回null
     */
    @SuppressWarnings("unchecked")
    public static <T> T cast(Object obj, Class<T> targetClass) {
        if (obj == null || targetClass == null) {
            return null;
        }
        if (targetClass.isInstance(obj)) {
            return (T) obj;
        }
        log.warn("Cannot cast {} to {}", obj.getClass().getName(), targetClass.getName());
        return null;
    }
}
